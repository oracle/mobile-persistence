package oracle.ateam.sample.mobile.dt.model;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;

import oracle.adf.model.adapter.dataformat.MethodDef;
import oracle.adf.model.adapter.dataformat.MethodReturnDef;

import oracle.ateam.sample.mobile.dt.util.StringUtils;

import oracle.binding.meta.AccessorDefinition;
import oracle.binding.meta.NamedDefinition;
import oracle.binding.meta.OperationDefinition;
import oracle.binding.meta.OperationReturnDefinition;
import oracle.binding.meta.ParameterDefinition;
import oracle.binding.meta.StructureDefinition;

/**
 * This class holds metata of crud methods that can be invoked through either
 * a web service data control method, or through a RESTful resource
 */
public class DCMethod
{
  
  // holds data control method name in case of WS Data Control, holds
  // request URI in case of RESTful WS
  private String name;
  private String accessorAttribute;
  private OperationDefinition method;
  private AccessorDefinition accessor;
  private List<DCMethodParameter> params = new ArrayList<DCMethodParameter>(); 
  private DataObjectInfo dataObject;
  private DataObjectInfo parameterValueProviderDataObject;
  private String payloadElementName;
  private String payloadRowElementName;
  private String requestType;
  private String fullUri;
  private String connectionName;
  private boolean isFindAllInParentMethod = false;
  private boolean isGetAsParentMethod = false;
  private boolean isGetCanonicalMethod = false;
  private boolean isFindAllMethod = false;
  private boolean isFindMethod = false;
  private boolean isWriteMethod = false;
  private boolean isSecured = false;
  private boolean sendSerializedDataObjectAsPayload = false;
  private List<HeaderParam> headerParams = new ArrayList<HeaderParam>();
  private String samplePayload;
  
  public DCMethod(OperationDefinition method)
  {
    this.method = method;
    this.name = method.getName();
    initParams();
    derivePayloadReturnElementName();
  }

  public DCMethod(AccessorDefinition accessor)
  {
    this.accessor = accessor;
    this.name = accessor.getName();
    setPayloadElementName(accessor.getName());
  }

  public DCMethod(String connectionName)
  {
    this.connectionName = connectionName;
  }

  public DCMethod(String connectionName, String fullUri, String requestType)
  {
    this.connectionName = connectionName;
    setFullUri(fullUri);
    this.requestType = requestType;
  }

  public DCMethod(String accessorAttribute, String connectionName, String fullUri, String requestType)
  {
    this.accessorAttribute = accessorAttribute;
    this.connectionName = connectionName;
    setFullUri(fullUri);
    this.requestType = requestType;
  }

  public void setIsGetAsParentMethod(boolean isGetAsParentMethod)
  {
    this.isGetAsParentMethod = isGetAsParentMethod;
  }

  public boolean isIsGetAsParentMethod()
  {
    return isGetAsParentMethod;
  }

  public void setSendSerializedDataObjectAsPayload(boolean sendSerializedDataObjectAsPayload)
  {
    this.sendSerializedDataObjectAsPayload = sendSerializedDataObjectAsPayload;
  }

  public boolean isSendSerializedDataObjectAsPayload()
  {
    return sendSerializedDataObjectAsPayload;
  }

  private void initParams()
  {
    Iterator iterator = method.getOperationParameters().iterator();
    ParameterDefinition param = null;
    while (iterator.hasNext())
    {
      param = (ParameterDefinition) iterator.next();
      addParam(new DCMethodParameter(param));
    }    
  }
  
  public String getFirstParamName()
  {
    if (getParams().size()>0)
    {
      return getParams().get(0).getName();      
    }
    return null;
  }

  public void addParam(DCMethodParameter param)
  {
    params.add(param);
  }

  public List<DCMethodParameter> getParams()
  {
    return params;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setDataObject(DataObjectInfo dataObject)
  {
    this.dataObject = dataObject;
    if (getParameterValueProviderDataObject()==null)
    {
      setParameterValueProviderDataObject(dataObject);
    }
  }

  public DataObjectInfo getDataObject()
  {
    return dataObject;
  }

  public String getDataControlName()
  {
    if (method!=null)
    {
      return ((NamedDefinition)method.getDefinitionParent()).getName();            
    }
    else if (accessor!=null)
    {
      return ((NamedDefinition)accessor.getDefinitionParent()).getName();            
    }
    return null;
  }

  private void derivePayloadReturnElementName()
  {
    OperationReturnDefinition returnType = (OperationReturnDefinition) method.getOperationReturnType();
    if (returnType!=null ) // && returnType.isAccessor() ) //&& accessor.isCollectionType())
    {
    //          StructureDefinition accBean = (StructureDefinition) returnType.getDefinitionParent();
      MethodDef methodDef = (MethodDef) returnType.getDefinitionParent();   
      MethodReturnDef methodreturnDef = (MethodReturnDef) methodDef.getOperationReturnType();
    //          if (methodreturnDef!=null && methodreturnDef.isCollection() && !methodreturnDef.isScalarCollection() ) //&& accessor.isCollectionType())
      if (methodreturnDef!=null ) // && methodreturnDef.isCollection() && !methodreturnDef.isScalarCollection() ) //&& accessor.isCollectionType())
      {
        StructureDefinition accBean = methodreturnDef.getStructure();    
        if (accBean!=null)
        {          
          List beansProcessed = new ArrayList<StructureDefinition>();
          getAccessorBean(accBean,beansProcessed);
        }
      }
    }
  }

  private void getAccessorBean(StructureDefinition accBean, List beansProcessed)
  {
    if (beansProcessed.contains(accBean))
    {
      return;
    }
    else
    {
      beansProcessed.add(accBean);
    }
    boolean hasAttrs = accBean.getAttributeDefinitions().iterator().hasNext();
    if (hasAttrs)
    {
      setPayloadElementName(accBean.getName());
    }
    else
    {
      // recursive call if there is a child bean

      Iterator iterator = accBean.getAccessorDefinitions().iterator();
      if (iterator.hasNext())
      {
        // bean has no attrs
        // Return type is wrapped in "Result" accessor, then get the bean of the accessor
        // if we have two sibling accessors, we return the last one. This is kind of a hack for AuraPlayer: AuraPlyer always
        // retrns the messages element as the first one, and then the actual content data as the next. It has to be in that order because
        // othewise ADF Mobile is somehow not able to 
        AccessorDefinition accessor = null;
        while (iterator.hasNext())
        {
          accessor = (AccessorDefinition) iterator.next();          
        }
        accBean = accessor.getStructure();                
        getAccessorBean(accBean, beansProcessed);
      }
    }
  }

  public void setPayloadElementName(String payloadReturnElementName)
  {
    this.payloadElementName = payloadReturnElementName;
  }

  public String getPayloadElementName()
  {
    return payloadElementName;
  }

  public void setParameterValueProviderDataObject(DataObjectInfo parameterValueProviderDataObject)
  {    
    this.parameterValueProviderDataObject = parameterValueProviderDataObject;    
    if (parameterValueProviderDataObject!=null)
    {
      for(DCMethodParameter param : getParams())
      {
        String payloadName = param.getName();
        AttributeInfo attr = parameterValueProviderDataObject.getAttributeDefByPayloadName(payloadName);
        if (attr!=null && (isIsWriteMethod() || isIsFindAllInParentMethod()))
        {
          // we have a match, set this attribute as value provider
          param.setValueProvider(DCMethodParameter.DATA_OBJECT_ATTRIBUTE);
          param.setDataObjectAttribute(attr.getAttrName());
        }
        else if (payloadName.toUpperCase().contains("USERNAME") || payloadName.toUpperCase().contains("USER_NAME"))
        {
          param.setValueProvider(DCMethodParameter.EL_EXPRESSION);
          param.setValue("#{applicationScope.UserContext.userName}");
        }
        else if (payloadName.toUpperCase().contains("PASSWORD") || payloadName.toUpperCase().contains("PASS_WORD"))
        {
          param.setValueProvider(DCMethodParameter.EL_EXPRESSION);
          param.setValue("#{applicationScope.UserContext.password}");
        }
        else if (isIsFindMethod())
        {
          param.setValueProvider(DCMethodParameter.SEARCH_VALUE);
        }
      }  
      // if write method and only one param, then we assume this is the serialized entity that must be passed in
      // This is reasonable default when using ADF BC SDO services
      // if it is a rest uri param, we default it to the primary key attribute of the value provider 
      if (getParams().size()==1)
      {
        DCMethodParameter param = getParams().get(0);
        if (param.isPathParam())
        {
          param.setValueProvider(DCMethodParameter.DATA_OBJECT_ATTRIBUTE);
          List<AttributeInfo> keyAttrs = parameterValueProviderDataObject.getKeyAttributes();
          if (keyAttrs.size()>0)
          {
            param.setDataObjectAttribute(keyAttrs.get(0).getAttrName());            
          }
        }
        else if (isIsWriteMethod())
        {
          param.setValueProvider(DCMethodParameter.SERIALIZED_DATA_OBJECT);          
        }
      }
      else if (isIsWriteMethod() && getParams().size()==0)
      {
        // no params, so we can assume the serialized dataobject will be s
        setSendSerializedDataObjectAsPayload(true);
      }
    }
  }

  public DataObjectInfo getParameterValueProviderDataObject()
  {
    return parameterValueProviderDataObject;
  }

  public void setIsFindAllMethod(boolean isFindAllMethod)
  {
    this.isFindAllMethod = isFindAllMethod;
  }

  public boolean isIsFindAllMethod()
  {
    return isFindAllMethod;
  }

  public void setIsFindMethod(boolean isFindMethod)
  {
    this.isFindMethod = isFindMethod;
  }

  public boolean isIsFindMethod()
  {
    return isFindMethod;
  }

  public void setIsWriteMethod(boolean isWriteMethod)
  {
    this.isWriteMethod = isWriteMethod;
  }

  public boolean isIsWriteMethod()
  {
    return isWriteMethod;
  }

  public void setIsFindAllInParentMethod(boolean isFindAllInParentMethod)
  {
    this.isFindAllInParentMethod = isFindAllInParentMethod;
  }

  public boolean isIsFindAllInParentMethod()
  {
    return isFindAllInParentMethod;
  }

  public void setRequestType(String requestType)
  {
    this.requestType = requestType;
  }

  public String getRequestType()
  {
    return requestType;
  }

  public void setConnectionName(String connectionName)
  {
    this.connectionName = connectionName;
  }

  public String getConnectionName()
  {
    return connectionName;
  }

  /**
   * This methods sets the full uri including parameter string that can be used
   * to derive parameters. Also sets the "short" uri without parameters in query string
   * @param fullUri
   */
  public void setFullUri(String fullUri)
  {
    this.fullUri = fullUri;
    String uri = fullUri;
    int questionPos = fullUri.indexOf("?");
    // clear any exist params
    params.clear();
    String params = null;
    if (questionPos > -1)
    {
      uri = fullUri.substring(0, questionPos);
      params = fullUri.substring(questionPos+1);
    }
    uri = derivePathParams(uri);
    setName(uri);
    if (params!=null)
    {
      deriveQueryParams(params);
    }      
  }

  /**
   * derive path params. If there are path params with duplicate names, we suffix the second name with "1".
   * This can happen with adf bc rest services
   * @param name
   * @return uri with possibly modified path params
   */
  private String derivePathParams(String name)
  {
    String returnUri = name;
    String uri = name;
    int paramPos = uri.indexOf("{");
    int startPosInReturnUri = paramPos;
    while (paramPos>-1)
    {
      int endPos = uri.indexOf("}",paramPos);
      int endPosInReturnUri = returnUri.indexOf("}",startPosInReturnUri);
      if (endPos>-1)
      {
        String paramName = uri.substring(paramPos+1,endPos);
        int suffix = 1;
        boolean nameModified = false;
        String newName = paramName;
        while (paramExists(newName))
        {
          // duplicate params in uri, rename
          newName = paramName+suffix;
          nameModified = true;
          suffix++;
        }
        if (nameModified)
        {
          paramName = newName;
          // update return uri with new param name
          returnUri = returnUri.substring(0,startPosInReturnUri+1)+paramName+returnUri.substring(endPosInReturnUri);          
        }

        DCMethodParameter methodParam = new DCMethodParameter();
        methodParam.setName(paramName);
        methodParam.setPathParam(true);
        addParam(methodParam);            
        if (uri.length()>endPos+1)
        {
          uri = uri.substring(endPos+1);        
          paramPos = uri.indexOf("{");
          startPosInReturnUri = returnUri.indexOf("{", endPosInReturnUri);
        }
        else
        {
          paramPos = -1;
        }        
      }
      else
      {
        break;
      }
    }
    return returnUri;
  }

  private void deriveQueryParams(String params)
  {
    String[] paramKeyValuePairs = StringUtils.stringToStringArray(params, "&");
    for( String paramKeyValuePair : paramKeyValuePairs)
    {
      int ispos = paramKeyValuePair.indexOf("=");
      if (ispos>0)
      {
        String paramName = paramKeyValuePair.substring(0, ispos);
        if (!paramExists(paramName))
        {
          DCMethodParameter methodParam = new DCMethodParameter();
          methodParam.setName(paramName);
          addParam(methodParam);            
        }
      }
    }
  }

  public boolean paramExists(String name)
  {
    boolean exists = false;
    for(DCMethodParameter param : getParams())
    {
      if (name.equals(param.getName()))
      {
        exists = true;
        break;
      }
    }
    return exists;
  }

  public String getFullUri()
  {
    return fullUri;
  }

  public void setPayloadRowElementName(String payloadRowElementName)
  {
    this.payloadRowElementName = payloadRowElementName;
  }

  public String getPayloadRowElementName()
  {
    return payloadRowElementName;
  }

  public void setIsSecured(boolean isSecured)
  {
    this.isSecured = isSecured;
  }

  public boolean isIsSecured()
  {
    return isSecured;
  }

  public void setHeaderParams(List<HeaderParam> headerParams)
  {
    this.headerParams = headerParams;
  }

  public List<HeaderParam> getHeaderParams()
  {
    return headerParams;
  }

  public void setSamplePayload(String samplePayload)
  {
    this.samplePayload = samplePayload;
  }

  public String getSamplePayload()
  {
    return samplePayload;
  }

  public void setIsGetCanonicalMethod(boolean isGetCanonicalMethod)
  {
    this.isGetCanonicalMethod = isGetCanonicalMethod;
  }

  public boolean isIsGetCanonicalMethod()
  {
    return isGetCanonicalMethod;
  }

  public void setAccessorAttribute(String accessorAttribute)
  {
    this.accessorAttribute = accessorAttribute;
  }

  public String getAccessorAttribute()
  {
    return accessorAttribute;
  }
}
