/*******************************************************************************
 Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 28-jan-2015   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.controller.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.adfinternal.model.adapter.url.URLUtil;

import oracle.ateam.sample.mobile.dt.exception.ParseException;
import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DCMethodParameter;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.HeaderParam;
import oracle.ateam.sample.mobile.dt.util.StringUtils;

import oracle.ide.net.URLFileSystem;

import org.raml.model.Action;
import org.raml.model.ActionType;
import org.raml.model.MimeType;
import org.raml.model.ParamType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.raml.model.parameter.Header;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;

import org.yaml.snakeyaml.scanner.ScannerException;

public class RAMLParser
{

  private List<DataObjectInfo> dataObjectInfos = new ArrayList<DataObjectInfo>();

  private List<HeaderParam> headerParams;
  private String connectionName;
  private String connectionUri;
  private String ramlFile;
  private boolean flattenNestedObjects;
  private List<Map<String, String>> schemas;

  public RAMLParser(String ramlFile, String connectionName, String connectionUri,
                              List<HeaderParam> headerParams, boolean flattenNestedObjects)
  {
    super();
    this.connectionName = connectionName;
    this.connectionUri = connectionUri;
    this.headerParams = headerParams;
    this.ramlFile = ramlFile;
    this.flattenNestedObjects = flattenNestedObjects;
  }

  public List<DataObjectInfo> run()
  {
    //        String newFileContent = fileContent.replaceAll("\t", "  ");
    //        is = new ByteArrayInputStream(newFileContent.getBytes());
    Raml raml = getRamlDocument();
    String contentType = raml.getMediaType();
    HeaderParam contentTypeParam = new HeaderParam();
    contentTypeParam.setName("Content-Type");
    contentTypeParam.setValue(contentType);
    if (!headerParams.contains(contentTypeParam))
    {
      headerParams.add(contentTypeParam);
    }
    schemas = raml.getSchemas();
    Map<String, Resource> resources = raml.getResources();
    for (Resource resource: resources.values())
    {
      createDataObject(resource, null, null);
    }
    // clean up attr names
    for (DataObjectInfo doi: dataObjectInfos)
    {
      doi.cleanUpAttrNames();
    }
    return dataObjectInfos;
  }

  private String findSchema(String name)
  {
    String schemaFound = null;
    for (Map<String,String> schema : schemas)
    {
      if (schema.get(name)!=null)
      {
        schemaFound = schema.get(name);
        break;
      }
    }
    return schemaFound;
  }

  protected Raml getRamlDocument()
  {
    InputStream is = null;
    try
    {
      File file = new File(ramlFile);
      is = new FileInputStream(file);
      String resourceLocation = "file:///"+file.getParent()+"/";
      Raml raml = new RamlDocumentBuilder().build(is, resourceLocation);
      return raml;
    }
    catch (ScannerException e)
    {
      // something is wrong, validate the raml and report back the errors
      StringBuffer errors = new StringBuffer("");
      List<ValidationResult> results = RamlValidationService.createDefault().validate(is);
      for (ValidationResult result: results)
      {
        errors.append("line" + result.getLine() + ", column " + result.getStartColumn() + ": " + result.getMessage() +
                      "\n");
      }
      if (errors.toString().startsWith("line-1, column -1: Invalid RAML"))
      {
        // report the execption problem, says more than validation in this case
        if (e instanceof ScannerException)
        {
          throw new ParseException("RAML is invalid: \n" + ((ScannerException)e).getProblem());                  
        }
      }
      throw new ParseException("RAML is invalid: \n" + errors);
    }
    catch (RuntimeException e2)
    {
      // TODO: Add catch code
      throw new ParseException("Cannot read RAML: \n" + e2.getLocalizedMessage());
    }
    catch (FileNotFoundException e)
    {
      throw new ParseException("Cannot find RAML file: \n" + e.getLocalizedMessage());
    }
  }


  private static Map<String, Class> typeMapping = new HashMap<String, Class>();
  static
  {
    // Mapping based on http://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html#1034737
    typeMapping.put("string", String.class);
    typeMapping.put("integer", Integer.class);
    typeMapping.put("number", BigDecimal.class);
    typeMapping.put("date", Date.class);
  }

  private void createDataObject(Resource resource, DataObjectInfo parentDataObject, Resource parentResource)
  {
    String name = extractResourceNameFromUriPath(resource);
    // name can be null when it is  top-level resource consisting of only a slask, or only uri params
    // which should be very unlikely
    name = name==null ? "Root" : name;
    DataObjectInfo doi = new DataObjectInfo(name, name);
    doi.setXmlPayload(false);
    dataObjectInfos.add(doi);

    // loop over the various actions (HTTP Verbs) of the resource
    // if the action is GET , we process the REQUEST body and add the attributes
    // found in the request example or request schema reference. The GET resource is also set as
    // findALl method/ If a nested GET resource is found with the path consisting entirely of a path param,
    // we assume this is the "getCanonical" resource. The sample/schema attributes of this nested GET resource
    // will also be added to the data object.
    // if it is a non-GET action, we process the RESPONSE with code 200 or 201 and add the attributes found
    // in the response example or response schema reference
    processResourceActions(resource, doi, parentDataObject, false);
    // loop over nested resources. If the whole nested resource path just consist of uri parameters, and
    // no additional naming, we treat this as additional resources for the current data object, otherwise
    // we treat it as a child resource and we make a recursive call to create new child data object
    processNestedResources(resource, parentDataObject, doi); 
  }

  private void processNestedResources(Resource resource, DataObjectInfo parentDataObject, DataObjectInfo doi)
  {
    Map<String, Resource> nestedResources = resource.getResources();
    for (Resource nestedResource : nestedResources.values())
    {
      boolean pathOnlyContainsParams = checkPathOnlyContainsParams(nestedResource);
      if (pathOnlyContainsParams)
      {
        processResourceActions(nestedResource, doi, parentDataObject, true);
        // make recursive call to walk down resource tree
        processNestedResources(nestedResource, parentDataObject, doi);
      }
      else
      {
        // child resource, recursive call to processNestedResources is made inside createDataObject
        createDataObject(nestedResource, doi, resource);
      }
    }
  }

  private void processResourceActions(Resource resource, DataObjectInfo doi, DataObjectInfo parentDataObject, boolean isNestedResource)
  {
    Map<ActionType, Action> actions = resource.getActions();
    for (ActionType actionType: actions.keySet())
    {
      Action action = actions.get(actionType);
      if ("GET".equalsIgnoreCase(actionType.name()))
      {
        processGETResponse(resource, doi, parentDataObject, action, isNestedResource);
      }
      else
      {
        processNonGETRequest(resource, doi, parentDataObject, action);
      }
    }
  }

  private void processGETResponse(Resource resource, DataObjectInfo doi, DataObjectInfo parentDataObject, Action action, boolean isCanonical)
  {
    Map<String, Response> responses = action.getResponses();
    Response responseToProcess = null;
    for (String resp: responses.keySet())
    {
      int responseCode = new Integer(resp).intValue();
      if (responseCode >= 200 && responseCode <= 202)
      {
        responseToProcess = responses.get(resp);
        ;
        break;
      }
    }
    if (responseToProcess != null)
    {
      if (responseToProcess.hasBody())
      {
        Map<String, MimeType> body = responseToProcess.getBody();
        if (body.size() > 0)
        {
          // process the first one, not sure if there can ever be more than one entry, probably not
          MimeType mimeType = body.values().iterator().next();
          if (mimeType.getSchema()!=null)
          {
            String schema = findSchema(mimeType.getSchema());
            if (schema!=null && schema!=doi.getJsonSchema())
            {
              // set the JSON schema so we only parse it once for this data object
              // note that in theory we might parse different schema for same data object
              // when mutiple actions of same resource use different schemas
              // the parser will then merge the results
              doi.setJsonSchema(schema);
              JSONSchemaDataObjectParser parser = new JSONSchemaDataObjectParser();
              parser.parse(schema, doi,flattenNestedObjects, dataObjectInfos);          
            }
          }
          else if (mimeType.getExample() != null)
          {
            // set payload list elem to null so it will be derived correctly when using example payload
            doi.setPayloadListElementName(null);
            JSONExampleDataObjectParser parser = new JSONExampleDataObjectParser();
            parser.parse(mimeType.getExample(), doi,flattenNestedObjects, dataObjectInfos);
          }
        }
      }
      DCMethod getMethod = new DCMethod(this.connectionName, resource.getUri(), "GET");
      addQueryParameters(action, getMethod);
      addHeaders(action, getMethod);
          
      // if example payload or schema is parsed to set attributes, than the data object payload list element and
      // payload row element attributes have the correct values for this method.
      // we cannot set the list attribute with a canonical method because then the value is unreliable: if the canonical
      // payload starts with a child collection, than the name of that child collection is set as payloadListElementName
      if (!isCanonical)
      {
        getMethod.setPayloadElementName(doi.getPayloadListElementName());
      }
      getMethod.setPayloadRowElementName(doi.getPayloadRowElementName());        
      // set existing to true, to prevent that we override the values of PayloadElementName and PayloadRowElementName
      /// later on in the wizard
      getMethod.setExisting(true);
      if (isCanonical)
      {
        doi.setGetCanonicalMethod(getMethod);
        getMethod.setParameterValueProviderDataObject(doi);
      }
      else
      {
        if (parentDataObject==null)
        {
          doi.setFindAllMethod(getMethod);          
        }
        else
        {
          AccessorInfo childAccessor = new AccessorInfo(parentDataObject, doi,false);
          doi.addFindAllInParentMethod(getMethod);
          getMethod.setAccessorAttribute(extractResourceNameFromUriPath(resource));
          getMethod.setParameterValueProviderDataObject(parentDataObject);
          childAccessor.setChildAccessorMethod(getMethod);
          childAccessor.setChildAccessorName(doi.getName());  
          parentDataObject.addChild(childAccessor);          
        }
      }
    }
  }

  private void addQueryParameters(Action action, DCMethod method)
  {
    Map<String, QueryParameter> params = action.getQueryParameters();
    for (String paramName : params.keySet())
    {
      QueryParameter param = params.get(paramName);
      DCMethodParameter methodParam = new DCMethodParameter();
      methodParam.setPathParam(false);
      methodParam.setName(paramName);
      ParamType paramType = param.getType();
      String type = paramType != null? paramType.name(): "string";
      Class javaType = typeMapping.get(type);
      if (javaType==null)
      {
        javaType = String.class;
        methodParam.setJavaType(javaType.getName());
      }
      method.addParam(methodParam);        
    }
  }

  private void addHeaders(Action action, DCMethod method)
  {
    Map<String, Header> headers = action.getHeaders();
    for (String headerName : headers.keySet())
    {
      Header header = headers.get(headerName);
      HeaderParam param = new HeaderParam();
      param.setName(headerName);
      if (header.getDefaultValue()!=null)
      {
        param.setValue(header.getDefaultValue());        
      }
      method.addHeaderParam(param);        
    }
  }

  private void processNonGETRequest(Resource resource, DataObjectInfo doi, DataObjectInfo parentDataObject, Action action)
  {
    if (action.hasBody())
    {
      Map<String, MimeType> reqBody = action.getBody();
      MimeType mimeType = reqBody.values().iterator().next();
      if (mimeType.getSchema()!=null)
      {
        String schema = findSchema(mimeType.getSchema());
        if (schema!=null && schema!=doi.getJsonSchema())
        {
          // set the JSON schema so we only parse it once for this data object
          // note that in theory we might parse different schema for same data object
          // when mutiple actions of same resource use different schemas
          // the parser will then merge the results
          doi.setJsonSchema(schema);
          JSONSchemaDataObjectParser parser = new JSONSchemaDataObjectParser();
          parser.parse(schema, doi,flattenNestedObjects, dataObjectInfos);          
        }
      }
      else if (mimeType.getExample() != null)
      {
        // set payload list elem to null so it will be derived correctly when using example payload
        doi.setPayloadListElementName(null);
        JSONExampleDataObjectParser parser = new JSONExampleDataObjectParser();
        parser.parse(mimeType.getExample(), doi,flattenNestedObjects, dataObjectInfos);
      }
    }
    String requestType = action.getType().name().toUpperCase();
    DCMethod method = new DCMethod(this.connectionName, resource.getUri(), requestType);
    addQueryParameters(action, method);
    addHeaders(action, method);

    // if example payload or schema is parsed to set attributes, than the data object payload list element and
    // payload row element attributes have the correct values for this method.
    method.setPayloadElementName(doi.getPayloadListElementName());
    method.setPayloadRowElementName(doi.getPayloadRowElementName());
    method.setSendSerializedDataObjectAsPayload(true);
    // set existing to true, to prevent that we override the values of PayloadElementName and PayloadRowElementName
    /// later on in the wizard
    method.setExisting(true);
    // we set the crud methods based on most commonly used conventions of request type
    // user can always change this in crud resources wizard page
    if (requestType.equals("POST"))
    {
      doi.setCreateMethod(method);
    }
    else if (requestType.equals("PUT") || requestType.equals("PATCH"))
    {
      doi.setUpdateMethod(method);
    }
    else if (requestType.equals("DELETE"))
    {
      doi.setDeleteMethod(method);
    }
    // need to set ParameterValueProviderDataObject after we set it as ome of the CRUD methods on data object
    // otherwise param value defaulting logic does not work correctly
    method.setParameterValueProviderDataObject((parentDataObject!=null ? parentDataObject :doi));
  }

  private String extractResourceNameFromUriPath(Resource childResource)
  {
    String uri = childResource.getRelativeUri();
    // replace all params and their enclosing curly brackets with emmpty string
    Map<String, UriParameter> params = childResource.getUriParameters();
    for (String paramName : params.keySet())
    {
      uri = StringUtils.substitute(uri, "{"+paramName+"}", "");
    }
    // find a string after a slash, starting with the last slash. Whena struing is found, this
    // is the candidate bresource name
    String resourceName = null;
    while (uri.lastIndexOf("/")>-1)
    {
      int lastSlashPos = uri.lastIndexOf("/");
      if (uri.length()>lastSlashPos+1)
      {
        resourceName = uri.substring(lastSlashPos+1);
        break;
      }
      else if (lastSlashPos>0)
      {
        // reduce uri to part before this slash
        uri = uri.substring(0,lastSlashPos);
      }
      else
      {
        break;
      }
      
    }    
    return resourceName;
  }

  private boolean checkPathOnlyContainsParams(Resource childResource)
  {
    String uri = childResource.getRelativeUri();
    // replace all params and their enclosing curly brackets with emmpty string
    // also replace "/" with empty string, the remainder is the name, which can be 
    // an empty string if path consists entirely of uri params
    uri = StringUtils.substitute(uri, "/", "");
    Map<String, UriParameter> params = childResource.getUriParameters();
    for (String paramName : params.keySet())
    {
      uri = StringUtils.substitute(uri, "{"+paramName+"}", "");
    }
    if (uri.trim().equals(""))
    {
      uri=null;
    }
    return uri==null;
  }
}

