package oracle.ateam.sample.mobile.dt.controller;

import java.io.ByteArrayInputStream;

import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import oracle.adfmf.common.util.McAppUtils;

import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DCMethodParameter;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.HeaderParam;
import oracle.ateam.sample.mobile.dt.model.jaxb.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.dt.model.jaxb.DirectMapping;
import oracle.ateam.sample.mobile.dt.model.jaxb.ForeignKeyColumnReference;
import oracle.ateam.sample.mobile.dt.model.jaxb.HeaderParameter;
import oracle.ateam.sample.mobile.dt.model.jaxb.Method;
import oracle.ateam.sample.mobile.dt.model.jaxb.Methods;
import oracle.ateam.sample.mobile.dt.model.jaxb.MobileObjectPersistence;
import oracle.ateam.sample.mobile.dt.model.jaxb.ObjectFactory;
import oracle.ateam.sample.mobile.dt.model.jaxb.OneToManyMapping;
import oracle.ateam.sample.mobile.dt.model.jaxb.OneToOneMapping;
import oracle.ateam.sample.mobile.dt.model.jaxb.Parameter;
import oracle.ateam.sample.mobile.dt.util.FileUtils;

import oracle.ateam.sample.mobile.dt.util.StringUtils;

import oracle.ide.Ide;
import oracle.ide.model.Project;

public class PersistenceMappingLoader
{
  
//  private List<DataObjectInfo> dataObjects  = new ArrayList<DataObjectInfo>();
  public static final String DATA_SYNC_ACTION_CLASS = "oracle.ateam.sample.mobile.v2.persistence.service.DataSynchAction";
  private Map<ClassMappingDescriptor,DataObjectInfo> dataObjects  = new HashMap<ClassMappingDescriptor,DataObjectInfo>();
  private Map<AttributeInfo,DirectMapping> parentPopulatedAttrs  = new HashMap<AttributeInfo,DirectMapping>();

  public PersistenceMappingLoader()
  {
    super();
  }

  public URL getPersistenceMappingFileUrl()
  {
    String fileName = "persistence-mapping.xml";
    Project appControllerProject = McAppUtils.getApplicationControllerProject(Ide.getActiveWorkspace()
                                                                                      , null);

    URL sourceURL = FileUtils.getSourceURL(appControllerProject, "META-INF", fileName);
    return sourceURL;
  }
  
  public MobileObjectPersistence loadJaxbModel()
  {
    return loadJaxbModel(getPersistenceMappingFileUrl());
  }
  
  public MobileObjectPersistence loadJaxbModel(URL sourceURL)
  {
    InputStream is =FileUtils.getInputStream(sourceURL);
    MobileObjectPersistence persistenceModel = null;
    if (is != null)
    {
      try
      {
        String instancePath = ObjectFactory.class.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(instancePath,ObjectFactory.class.getClassLoader());
        Unmarshaller u = jc.createUnmarshaller();
        // set validator which causes error to throw Exception which is probably caused by the fact the
        // application structure file is of an older version and needs to be migrated.
        u.setValidating(false);
        persistenceModel = (MobileObjectPersistence) u.unmarshal(is);
      }
      catch (Exception exc)
      {
        exc.printStackTrace();
      }
    }
    return persistenceModel;
  }

  public Collection<DataObjectInfo> run(MobileObjectPersistence mop)
  {
    if (mop!=null)
    {
      // first create all data objects
      for (ClassMappingDescriptor descriptor : mop.getClassMappingDescriptor())
      {
        if (!DATA_SYNC_ACTION_CLASS.equals(descriptor.getClassName()))
        {
          DataObjectInfo doi = addDataObject(descriptor);        
          dataObjects.put(descriptor,doi);          
        }
      }
      // now we can process accessors because all data objects are present
      for (ClassMappingDescriptor descriptor : mop.getClassMappingDescriptor())
      {
        if (!DATA_SYNC_ACTION_CLASS.equals(descriptor.getClassName()))
        {
          addChildAccessors(descriptor, dataObjects.get(descriptor));        
          addParentAccessors(descriptor, dataObjects.get(descriptor));        
        }
      }
      completeParentPopulatedAttrs();
    }
    return dataObjects.values();
  }



  private DataObjectInfo addDataObject(ClassMappingDescriptor descriptor)
  {
    String className = descriptor.getClassName();
    int lastDotPos = className.lastIndexOf(".");
    String name = className.substring(lastDotPos+1);
    String packageName = className.substring(0,lastDotPos);
    DataObjectInfo doi = new DataObjectInfo(name,null);
    doi.setExisting(true);
    doi.setClassName(name);
    doi.setPackageName(packageName);
    if (descriptor.getCrudServiceClass()!=null)
    {
      String serviceClass = descriptor.getCrudServiceClass().getClassName();
      lastDotPos = serviceClass.lastIndexOf(".");
      doi.setServicePackageName(serviceClass.substring(0,lastDotPos));
      doi.setServiceClassName(serviceClass.substring(lastDotPos+1));      
    }
    doi.setPayloadDateFormat(descriptor.getDateFormat());
    doi.setPayloadDateTimeFormat(descriptor.getDateTimeFormat());
    doi.setPersisted(descriptor.isPersisted());
    doi.setGenerate(true);
    String manager = descriptor.getCrudServiceClass().getRemotePersistenceManager();
    if (manager!=null)
    {
      doi.setXmlPayload(manager.toUpperCase().indexOf("JSON")==-1);      
    }
    doi.setCollection(true);
    for (DirectMapping dm : descriptor.getAttributeMappings().getDirectMapping())
    {
      addAttribute(doi,dm);
    }      
    // set order by after adding attributes because we need to lookup the attribute name based
    // on column name
    doi.setOrderBy(descriptor.getOrderBy());
    doi.setSortOrder(convertToAttributeSortOrder(doi, descriptor.getOrderBy()));

    // first create methods so we can lookup find-all-in-parent-method when
    // create one-to-many mappings
    Methods methods = descriptor.getMethods();
    DCMethod method = createMethod(doi,methods.getFindAllMethod());
    if (method!=null)
    {
      method.setIsFindAllMethod(true);
      doi.setFindAllMethod(method);
      doi.setDeleteLocalRows(methods.getFindAllMethod().isDeleteLocalRows());
      doi.setPayloadListElementName(method.getPayloadElementName());
      doi.setPayloadRowElementName(method.getPayloadRowElementName());
    }
    method = createMethod(doi,methods.getFindMethod());
    if (method!=null)
    {
      method.setIsFindMethod(true);
      doi.setFindMethod(method);
    }
    method = createMethod(doi,methods.getGetCanonicalMethod());
    if (method!=null)
    {
      method.setIsGetCanonicalMethod(true);
      doi.setGetCanonicalMethod(method);
    }
    for (Method findAllInParentMethod : methods.getFindAllInParentMethod())
    {
        method = createMethod(doi,findAllInParentMethod);
        method.setIsFindAllInParentMethod(true);
        method.setAccessorAttribute(findAllInParentMethod.getName());
        doi.getFindAllInParentMethods().add(method);    
    }
    for (Method getAsParentMethod : methods.getGetAsParentMethod())
    {
        method = createMethod(doi,getAsParentMethod);
        method.setIsGetAsParentMethod(true);
        method.setAccessorAttribute(getAsParentMethod.getName());
        doi.getGetAsParentMethods().add(method);    
    }

    method = createMethod(doi,methods.getCreateMethod());
    doi.setCreateMethod(method);
    method = createMethod(doi,methods.getUpdateMethod());
    doi.setUpdateMethod(method);
    method = createMethod(doi,methods.getMergeMethod());
    doi.setMergeMethod(method);
    method = createMethod(doi,methods.getRemoveMethod());
    doi.setDeleteMethod(method);
    doi.setCrudMethodsInitialized(true);
    return doi;
  }

  private void addAttribute(DataObjectInfo doi, DirectMapping dm)
  {
    AttributeInfo attr = new AttributeInfo(dm.getAttributeName(),dm.getJavaType());
    doi.addAttribute(attr);
    attr.setColumnType(dm.getColumnDataType());
    attr.setKeyAttribute(dm.isKeyAttribute());
    attr.setPayloadName(dm.getPayloadAttributeName());
    attr.setPersisted(dm.isPersisted());
    attr.setRequired(dm.isRequired());
    if (dm.getParentAttributeName()!=null)
    {
      // add to list of parentPopulated attrs so we can add parenet references after all dataObjects have been created
      parentPopulatedAttrs.put(attr,dm);
    }
  }

  private void completeParentPopulatedAttrs()
  {
    for (AttributeInfo attr : parentPopulatedAttrs.keySet())
    {
      DirectMapping dm = parentPopulatedAttrs.get(attr);
      DataObjectInfo parent = findDataObject(dm.getParentClass());
      AttributeInfo parentAttr = parent.getAttributeDef(dm.getParentAttributeName());
      attr.setParentDataObject(parent);
      attr.setParentReferenceAttribute(parentAttr);
    }
  }

  private DataObjectInfo findDataObject(String className)
  {
    DataObjectInfo found = null;
    for(DataObjectInfo doi : dataObjects.values())
    {
      if (doi.getFullyQualifiedClassName().equals(className))
      {
        found = doi;
        break;
      }
    }        
    return found;
  }

  private void addChildAccessors(ClassMappingDescriptor descriptor, DataObjectInfo dataObjectInfo)
  {
    for(OneToManyMapping mapping : descriptor.getAttributeMappings().getOneToManyMapping())    
    {
      DataObjectInfo child = findDataObject(mapping.getReferenceClassName());
      AccessorInfo ai = new AccessorInfo(dataObjectInfo,child,false);
      ai.setChildAccessorName(mapping.getAttributeName());
      dataObjectInfo.addChild(ai);
      if (mapping.getAccessorMethod()!=null)
      {
         DCMethod childAccessorMethod = findChildAccessorMethod(child, mapping.getAttributeName());
         ai.setChildAccessorMethod(childAccessorMethod);         
      }
      else
      {
        // set parent data object in child so this child does not show up on CRUD methods panel, because
        // it has no service class
        child.setParent(dataObjectInfo);
        ai.setChildAccessorPayloadName(mapping.getPayloadAttributeName());
      }
      for (ForeignKeyColumnReference fkref : mapping.getForeignKeyColumnReference())
      {
        AttributeInfo parentAttr = dataObjectInfo.getAttributeDefByColumnName(fkref.getTargetColumn());
        AttributeInfo childAttr = child.getAttributeDefByColumnName(fkref.getSourceColumn());
        ai.addAttributeMapping(parentAttr, childAttr);
      }
    }
    
  }

  private void addParentAccessors(ClassMappingDescriptor descriptor, DataObjectInfo dataObjectInfo)
  {
    for(OneToOneMapping mapping : descriptor.getAttributeMappings().getOneToOneMapping())    
    {
      DataObjectInfo parent = findDataObject(mapping.getReferenceClassName());
      // we first check whether there is already a child accessor in the other direction,
      // if so, we add the parent accessor info to this existing AccessorInfo
      
      AccessorInfo ai = findAccessorInfo(parent, dataObjectInfo);
      boolean existingAi = ai!=null;
      if (!existingAi)
      {
        ai = new AccessorInfo(parent,dataObjectInfo,false);
      }
      ai.setParentAccessorName(mapping.getAttributeName());
      dataObjectInfo.addParent(ai);
      if (mapping.getAccessorMethod()!=null)
      {
         DCMethod parentAccessorMethod = findParentAccessorMethod(parent, mapping.getAttributeName());
         ai.setParentAccessorMethod(parentAccessorMethod);         
      }
      if (!existingAi)
      {
        // on;y add mappings when accessor is new 
        for (ForeignKeyColumnReference fkref : mapping.getForeignKeyColumnReference())
        {
          AttributeInfo parentAttr = parent.getAttributeDefByColumnName(fkref.getTargetColumn());
          AttributeInfo childAttr = dataObjectInfo.getAttributeDefByColumnName(fkref.getSourceColumn());
          ai.addAttributeMapping(parentAttr, childAttr);
        }        
      }
    }
    
  }

  private DCMethod createMethod(DataObjectInfo doi,Method method)
  {
    if (method==null)
    {
      return null;
    }
    DCMethod dcMethod = new DCMethod(method.getConnectionName());
    dcMethod.setExisting(true);
    dcMethod.setDataObject(doi);
    dcMethod.setIsSecured(method.isSecured());
    dcMethod.setPayloadElementName(method.getPayloadElementName());
    dcMethod.setPayloadRowElementName(method.getPayloadRowElementName());
//    dcMethod.setRequestType(method.getRequestType().value());
    dcMethod.setRequestType(method.getRequestType());
    dcMethod.setSendSerializedDataObjectAsPayload(method.isSendDataObjectAsPayload());
    if (method.getUri()!=null)
    {
      // REST service, we set the uri in name field, and name in accessorName
      dcMethod.setName(method.getUri());
      dcMethod.setAccessorAttribute(method.getName());
    }
    else
    {
      // SOAP Data control, set name to name
      dcMethod.setDataControlName(method.getDataControlName());
      dcMethod.setName(method.getName());
    }
    List<HeaderParam> headers = new ArrayList<HeaderParam>();
    for (HeaderParameter param : method.getHeaderParameter())
    {
      HeaderParam headerParam = new HeaderParam();
      headerParam.setName(param.getName());
      headerParam.setValue(param.getValue());
      headers.add(headerParam);
    }
    dcMethod.setHeaderParams(headers);

    for (Parameter param : method.getParameter())
    {
      DCMethodParameter mParam = new DCMethodParameter();
      mParam.setName(param.getName());
      mParam.setValue(param.getValue());
      mParam.setJavaType(param.getJavaType());
      mParam.setPathParam(param.isPathParam());
      mParam.setDataObjectAttribute(param.getDataObjectAttribute());
      mParam.setValueProvider(param.getValueProvider());
      dcMethod.addParam(mParam);
    }    
    return dcMethod;
  }

  private DCMethod findChildAccessorMethod(DataObjectInfo child, String accessorName)
  {
    DCMethod found = null;
    for (DCMethod method : child.getFindAllInParentMethods())
    {
      if (accessorName.equals(method.getAccessorAttribute()))
      {
        found = method;
        break;
      }
    }
    return found;
  }

  private DCMethod findParentAccessorMethod(DataObjectInfo parent, String accessorName)
  {
    DCMethod found = null;
    for (DCMethod method : parent.getGetAsParentMethods())
    {
      if (accessorName.equals(method.getAccessorAttribute()))
      {
        found = method;
        break;
      }
    }
    return found;
  }

  private AccessorInfo findAccessorInfo(DataObjectInfo parent, DataObjectInfo child)
  {
    AccessorInfo found = null;
    for (AccessorInfo ai : parent.getChildren())
    {
      if (ai.getChildDataObject()==child)
      {
        found = ai;
        break;
      }
    }
    return found;
  }
  
  private String convertToAttributeSortOrder(DataObjectInfo doi, String sqlOrderBy)
  {
    try
    {
     if (sqlOrderBy == null || "".equals(sqlOrderBy.trim()))
     {
       return null;
     }
     StringBuffer orderBy = new StringBuffer();
     String[] attrs = StringUtils.stringToStringArray(sqlOrderBy, ",");
     boolean firstAttr = true;
     for (int i = 0; i < attrs.length; i++)
     {
       String orderAttr = attrs[i].trim();
       String columnName = orderAttr;
       String ascDesc = null;
       int spacePos = orderAttr.lastIndexOf(" ");
       if (spacePos > -1)
       {
         columnName = orderAttr.substring(0, spacePos);
         ascDesc = orderAttr.substring(spacePos + 1);
         ascDesc = ascDesc.trim();
         if (!"desc".equalsIgnoreCase(ascDesc) && !"asc".equalsIgnoreCase(ascDesc))
         {
           continue;
           //          throw new RuntimeException("Sort Order is invalid, you cannot use "+ascDesc);
         }
       }
       AttributeInfo attributeDef = doi.getAttributeDefByColumnName(columnName.toUpperCase());
       if (attributeDef == null)
       {
         continue;
         //        throw new RuntimeException("Attribute name "+attr+" in Sort Order is invalid");
       }
       if (firstAttr)
       {
         firstAttr = false;
       }
       else
       {
         orderBy.append(",");
       }
       orderBy.append(attributeDef.getAttrName());
       if (ascDesc != null)
       {
         orderBy.append(" ");
         orderBy.append(ascDesc);
       }
     }
     return orderBy.toString();
   }
    catch (Exception e)
    {
      // TODO: Add catch code
      System.err.println("Erro converting sql order by to attribute sort order: "+e.getLocalizedMessage());
      e.printStackTrace();
    }
    return null;
  }
  
}
