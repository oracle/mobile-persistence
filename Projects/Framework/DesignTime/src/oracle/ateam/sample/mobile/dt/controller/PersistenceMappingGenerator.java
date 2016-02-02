package oracle.ateam.sample.mobile.dt.controller;

import java.io.InputStream;
import java.io.StringWriter;

import java.net.URL;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import oracle.adfmf.common.util.McAppUtils;

import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DCMethodParameter;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.HeaderParam;
import oracle.ateam.sample.mobile.dt.model.jaxb.AttributeMappings;
import oracle.ateam.sample.mobile.dt.model.jaxb.ClassMappingDescriptor;
import oracle.ateam.sample.mobile.dt.model.jaxb.CrudServiceClass;
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
import oracle.ateam.sample.mobile.dt.model.jaxb.PrimaryKeyColumn;
import oracle.ateam.sample.mobile.dt.model.jaxb.RequestType;
import oracle.ateam.sample.mobile.dt.model.jaxb.Table;
import oracle.ateam.sample.mobile.dt.util.FileUtils;

import oracle.ide.Ide;
import oracle.ide.model.Project;
import oracle.ide.net.URLFactory;

public class PersistenceMappingGenerator
{
  ObjectFactory objectFactory = new ObjectFactory();
  List<DataObjectInfo> dataObjects;
  MobileObjectPersistence mop;
  BusinessObjectGeneratorModel model;

  public PersistenceMappingGenerator(BusinessObjectGeneratorModel model)
  {
    super();
    this.model = model;
    this.dataObjects = model.getSelectedDataObjects();
  }

  public String run()
  {
    mop = model.getExistingPersistenceMappingModel();
    if (mop == null)
    {
      // create default model with dataSyncAction and wevServiceCall classmappingDescriptors using xml file in teplates dir
      String newPersistenceMappingFile =
        Ide.getOracleHomeDirectory() +
        "/jdev/extensions/oracle.ateam.mobile.persistence/templates/new-persistence-mapping.xml";
      final URL url = URLFactory.newFileURL(newPersistenceMappingFile);
      mop = new PersistenceMappingLoader().loadJaxbModel(url);

      if (model.isUseMCS())
      {
        // Find the MCS STORAGE_OBJECT mapping, we need to set the connection name to MCS connection
        ClassMappingDescriptor found = null;
        for (ClassMappingDescriptor descriptor: mop.getClassMappingDescriptor())
        {
          if (descriptor.getClassName().equals(PersistenceMappingLoader.STORAGE_OBJECT_CLASS))
          {
            found = descriptor;
            break;
          }
        }
        //      if (!model.isUseMCS())
        //      {
        //        // remove the STORAGE_OBJECT
        //        mop.getClassMappingDescriptor().remove(found);
        //      }
        //      else
        //      {
        // update connectionName on methods to name set in wizard
        found.getMethods().getFindMethod().setConnectionName(model.getConnectionName());
        //      }
      }
    }
    for (DataObjectInfo dataObject: dataObjects)
    {
      createDescriptor(dataObject);
    }
    return writePersistenceMapping();
  }


  private ClassMappingDescriptor findDescriptor(String className)
  {
    ClassMappingDescriptor found = null;
    for (ClassMappingDescriptor descriptor: mop.getClassMappingDescriptor())
    {
      if (descriptor.getClassName().equals(className))
      {
        found = descriptor;
        break;
      }
    }
    return found;
  }

  //  private DirectMapping findDirectMapping(ClassMappingDescriptor descriptor, String attrName)
  //  {
  //    ClassMappingDescriptor found = null;
  //    for (ClassMappingDescriptor descriptor : mop.getClassMappingDescriptor())
  //    {
  //      if (descriptor.getClassName().equals(className))
  //      {
  //        found = descriptor.get;
  //        break;
  //      }
  //    }
  //    return found;
  //  }

  private void createDescriptor(DataObjectInfo dataObject)
  {
    String fullyQualifiedClassName = dataObject.getFullyQualifiedClassName();
    ClassMappingDescriptor classMappingDescriptor = findDescriptor(fullyQualifiedClassName);
    if (classMappingDescriptor == null)
    {
      classMappingDescriptor = objectFactory.createClassMappingDescriptor();
      // add as last but three, so we keep storage object, data synch action and webServiceCall descs at the bottom
      int descCount = mop.getClassMappingDescriptor().size();
      int pos = descCount == 0? 0: (descCount == 1? 1: mop.getClassMappingDescriptor().size() - 3);
      mop.getClassMappingDescriptor().add(pos, classMappingDescriptor);
      classMappingDescriptor.setClassName(fullyQualifiedClassName);
    }
    classMappingDescriptor.setDateFormat(dataObject.getPayloadDateFormat());
    classMappingDescriptor.setDateTimeFormat(dataObject.getPayloadDateTimeFormat());
    classMappingDescriptor.setOrderBy(dataObject.getOrderBy());
    classMappingDescriptor.setPersisted(dataObject.isPersisted());
    if (dataObject.getCanonicalTriggerAttribute() != null)
    {
      classMappingDescriptor.setCanonicalTriggerAttribute(dataObject.getCanonicalTriggerAttribute().getAttrName());
    }

    CrudServiceClass service = classMappingDescriptor.getCrudServiceClass();
    if (service == null)
    {
      service = objectFactory.createCrudServiceClass();
      classMappingDescriptor.setCrudServiceClass(service);
    }
    service.setAutoIncrementPrimaryKey(true);
    if (dataObject.isGenerateServiceClass())
    {
      service.setClassName(dataObject.getFullyQualifiedServiceClassName());
      if (model.isWebServiceDataControl())
      {
        // deprecated SOAP WS wizard does not have runtime options panel to set persietnce managers
        service.setLocalPersistenceManager("oracle.ateam.sample.mobile.v2.persistence.manager.DBPersistenceManager");
        service.setRemotePersistenceManager("oracle.ateam.sample.mobile.v2.persistence.manager.DataControlPersistenceManager");
      }
      else
      {
        service.setLocalPersistenceManager(dataObject.getLocalPersistenceManager());
        service.setRemotePersistenceManager(dataObject.getRemotePersistenceManager());
      }
      service.setRemoteReadInBackground(dataObject.isRemoteReadInBackground());
      service.setRemoteWriteInBackground(dataObject.isRemoteWriteInBackground());
      service.setShowWebServiceInvocationErrors(dataObject.isShowWebServiceErrors());
      service.setAutoQuery(dataObject.isAutoQuery());
      service.setEnableOfflineTransactions(dataObject.isEnableOfflineTransactions());
    }
    else
    {
      // set service class to parent service class
      service.setClassName(dataObject.getRootDataObject().getFullyQualifiedServiceClassName());
    }

    Table table = objectFactory.createTable();
    classMappingDescriptor.setTable(table);
    table.setName(dataObject.getTableName());
    for (String columnName: dataObject.getKeyColumnNames())
    {
      PrimaryKeyColumn column = objectFactory.createPrimaryKeyColumn();
      table.getPrimaryKeyColumn().add(column);
      column.setName(columnName);
    }
    // recreate all attr mappings
    AttributeMappings attributeMappings = objectFactory.createAttributeMappings();
    classMappingDescriptor.setAttributeMappings(attributeMappings);
    for (AttributeInfo attr: dataObject.getAttributeDefs())
    {
      createDirectAttributeMapping(attributeMappings, attr);
    }
    for (AccessorInfo acc: dataObject.getChildren())
    {
      createOneToManyMapping(attributeMappings, acc);
    }
    for (AccessorInfo acc: dataObject.getParents())
    {
      createOneToOneMapping(attributeMappings, acc);
    }

    Methods methods = classMappingDescriptor.getMethods();
    if (methods == null)
    {
      methods = objectFactory.createMethods();
      classMappingDescriptor.setMethods(methods);
    }
    if (dataObject.getFindAllMethod() != null)
    {
      Method jaxbMethod = methods.getFindAllMethod();
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.setFindAllMethod(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, dataObject.getFindAllMethod());
      jaxbMethod.setDeleteLocalRows(dataObject.isDeleteLocalRows());
    }
    if (dataObject.getFindMethod() != null)
    {
      Method jaxbMethod = methods.getFindMethod();
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.setFindMethod(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, dataObject.getFindMethod());
    }
    for (DCMethod findAllInParentMethod: dataObject.getFindAllInParentMethods())
    {
      Method jaxbMethod = findMethod(methods.getFindAllInParentMethod(), findAllInParentMethod.getAccessorAttribute());
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.getFindAllInParentMethod().add(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, findAllInParentMethod);
    }
    for (DCMethod getAsParentMethod: dataObject.getGetAsParentMethods())
    {
      Method jaxbMethod = findMethod(methods.getGetAsParentMethod(), getAsParentMethod.getAccessorAttribute());
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.getGetAsParentMethod().add(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, getAsParentMethod);
    }
    if (dataObject.getGetCanonicalMethod() != null)
    {
      Method jaxbMethod = methods.getGetCanonicalMethod();
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.setGetCanonicalMethod(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, dataObject.getGetCanonicalMethod());
    }
    if (dataObject.getCreateMethod() != null)
    {
      Method jaxbMethod = methods.getCreateMethod();
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.setCreateMethod(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, dataObject.getCreateMethod());
    }
    if (dataObject.getUpdateMethod() != null)
    {
      Method jaxbMethod = methods.getUpdateMethod();
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.setUpdateMethod(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, dataObject.getUpdateMethod());
    }
    if (dataObject.getMergeMethod() != null)
    {
      Method jaxbMethod = methods.getMergeMethod();
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.setMergeMethod(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, dataObject.getMergeMethod());
    }
    if (dataObject.getDeleteMethod() != null)
    {
      Method jaxbMethod = methods.getRemoveMethod();
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.setRemoveMethod(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, dataObject.getDeleteMethod());
    }
    for (DCMethod customMethod: dataObject.getCustomMethods())
    {
      Method jaxbMethod = findMethod(methods.getCustomMethod(), customMethod.getAccessorAttribute());
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.getCustomMethod().add(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, customMethod);
    }

  }

  private void setMethodPropertiesAndParams(Method jaxbMethod, DCMethod wizardMethod)
  {
    // always update the resource and the verb
    if (model.isRestfulWebService())
    {
      if (wizardMethod.getAccessorAttribute() != null)
      {
        jaxbMethod.setName(wizardMethod.getAccessorAttribute());
      }
      // if it is a new method, then we must prefix with the (part of) the RAML base URI that is not
      // included at the end of the connection URI (If no RAML was used the uriPrefix is always
      // an empty string
      String uri = wizardMethod.isExisting()? wizardMethod.getName(): model.getUriPrefix() + wizardMethod.getName();
      jaxbMethod.setUri(uri);
      jaxbMethod.setConnectionName(wizardMethod.getConnectionName());
      //      jaxbMethod.setRequestType(RequestType.fromValue(wizardMethod.getRequestType()));
      jaxbMethod.setRequestType(wizardMethod.getRequestType());
      jaxbMethod.setSendDataObjectAsPayload(wizardMethod.isSendSerializedDataObjectAsPayload());
    }
    else
    {
      jaxbMethod.setName(wizardMethod.getName());
      jaxbMethod.setDataControlName(wizardMethod.getDataControlName());
    }
    jaxbMethod.setSecured(wizardMethod.isIsSecured());
    jaxbMethod.setPayloadElementName(wizardMethod.getPayloadElementName());
    jaxbMethod.setPayloadRowElementName(wizardMethod.getPayloadRowElementName());
    // set params again, all values that can be entered in XML fiel can also be set in wizard,so no issues
    // with recreating the param list
    jaxbMethod.getParameter().clear();
    for (DCMethodParameter methodParam: wizardMethod.getParams())
    {
      Parameter param = objectFactory.createParameter();
      jaxbMethod.getParameter().add(param);
      param.setName(methodParam.getName());
      param.setValueProvider(methodParam.getValueProvider());
      param.setDataObjectAttribute(methodParam.getDataObjectAttribute());
      param.setValue(methodParam.getValue());
      param.setJavaType(methodParam.getJavaType());
      if (model.isRestfulWebService())
      {
        param.setPathParam(methodParam.isPathParam());
      }
    }
    setHeaderParams(jaxbMethod, wizardMethod);
  }

  private void createDirectAttributeMapping(AttributeMappings attributeMappings, AttributeInfo attr)
  {
    DirectMapping mapping = objectFactory.createDirectMapping();
    attributeMappings.getDirectMapping().add(mapping);
    mapping.setAttributeName(attr.getAttrName());
    mapping.setKeyAttribute(attr.isKeyAttribute());
    mapping.setJavaType(attr.getJavaTypeFullName());
    mapping.setColumnName(attr.getColumnName());
    mapping.setColumnDataType(attr.getColumnType());
    mapping.setDateFormat(attr.getDateFormat());
    if (attr.getParentReferenceAttribute() != null)
    {
      mapping.setParentClass(attr.getParentDataObject().getFullyQualifiedClassName());
      mapping.setParentAttributeName(attr.getParentReferenceAttribute().getAttrName());
    }
    mapping.setPayloadAttributeName(attr.getPayloadName());
    mapping.setPersisted(attr.isPersisted());
    mapping.setRequired(attr.isRequired());
  }

  private void createOneToManyMapping(AttributeMappings attributeMappings, AccessorInfo acc)
  {
    OneToManyMapping mapping = objectFactory.createOneToManyMapping();
    attributeMappings.getOneToManyMapping().add(mapping);
    if (acc.getChildAccessorMethod() != null)
    {
      mapping.setAccessorMethod(acc.getChildAccessorMethod().getName());
    }
    mapping.setAttributeName(acc.getChildAccessorName());
    mapping.setPayloadAttributeName(acc.getChildAccessorPayloadName());
    mapping.setReferenceClassName(acc.getChildDataObject().getFullyQualifiedClassName());
    mapping.setSendAsArrayIfOnlyOneEntry(true);
    for (AccessorInfo.AttributeMapping attrMapping: acc.getAttributeMappings())
    {
      ForeignKeyColumnReference columnReference = objectFactory.createForeignKeyColumnReference();
      mapping.getForeignKeyColumnReference().add(columnReference);
      columnReference.setSourceColumn(attrMapping.getChildAttr().getColumnName());
      columnReference.setSourceTable(acc.getChildDataObject().getTableName());
      columnReference.setTargetColumn(attrMapping.getParentAttr().getColumnName());
      columnReference.setTargetTable(acc.getParentDataObject().getTableName());
    }
  }

  private void createOneToOneMapping(AttributeMappings attributeMappings, AccessorInfo acc)
  {
    OneToOneMapping mapping = objectFactory.createOneToOneMapping();
    attributeMappings.getOneToOneMapping().add(mapping);
    mapping.setAccessorMethod(acc.getParentAccessorMethod().getName());
    mapping.setAttributeName(acc.getParentAccessorName());
    mapping.setReferenceClassName(acc.getParentDataObject().getFullyQualifiedClassName());
    for (AccessorInfo.AttributeMapping attrMapping: acc.getAttributeMappings())
    {
      ForeignKeyColumnReference columnReference = objectFactory.createForeignKeyColumnReference();
      mapping.getForeignKeyColumnReference().add(columnReference);
      columnReference.setSourceColumn(attrMapping.getChildAttr().getColumnName());
      columnReference.setSourceTable(acc.getChildDataObject().getTableName());
      columnReference.setTargetColumn(attrMapping.getParentAttr().getColumnName());
      columnReference.setTargetTable(acc.getParentDataObject().getTableName());
    }
  }

  private void setHeaderParams(Method jaxbMethod, DCMethod wizardMethod)
  {
    // remove existing ones, they will be re-added through wizardMethod (if
    // they haven't been removed in wizard)
    jaxbMethod.getHeaderParameter().clear();
    for (HeaderParam param: wizardMethod.getHeaderParams())
    {
      if (param.getName() != null && !"".equals(param.getName()) && param.getValue() != null &&
          !"".equals(param.getValue()))
      {
        HeaderParameter headerParam = objectFactory.createHeaderParameter();
        jaxbMethod.getHeaderParameter().add(headerParam);
        headerParam.setName(param.getName());
        headerParam.setValue(param.getValue());
      }
    }
  }

  private Method findMethod(List<Method> methods, String name)
  {
    Method found = null;
    if (methods == null)
    {
      return null;
    }
    for (Method method: methods)
    {
      if (method.getName().equals(name))
      {
        found = method;
        break;
      }
    }
    return found;
  }

  private String writePersistenceMapping()
  {
    try
    {
      String instancePath = ObjectFactory.class.getPackage().getName();
      JAXBContext jc = JAXBContext.newInstance(instancePath, ObjectFactory.class.getClassLoader());
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      //  // SDA 07-nov-2012: use encoding of the node, need utf-8 to keep diacritic characters
      //  // by using node encoding, developers can still change the encoding if desired
      //  //      String encoding = IdeUtil.getIdeIanaEncoding();
      //      String encoding = node.getLoadEncoding()!=null ? node.getLoadEncoding() : "UTF-8";
      //      m.setProperty(Marshaller.JAXB_ENCODING, encoding);

      String output = null;
      StringWriter sw = new StringWriter();
      m.marshal(mop, sw); // new FileOutputStream("c:/temp/appstruct-test.xml"));
      output = sw.toString();
      return output;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

}
