package oracle.ateam.sample.mobile.dt.controller;

import java.io.InputStream;
import java.io.StringWriter;

import java.net.URL;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import oracle.adfmf.common.util.McAppUtils;

import oracle.ateam.sample.mobile.dt.controller.parser.PersistenceMappingLoader;
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
import oracle.ateam.sample.mobile.dt.model.jaxb.Table;
import oracle.ateam.sample.mobile.dt.util.FileUtils;

import oracle.ide.Ide;
import oracle.ide.model.Project;

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
    mop = new PersistenceMappingLoader().loadJaxbModel();
    if (mop==null)
    {
      mop = objectFactory.createMobileObjectPersistence();
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
    ClassMappingDescriptor classMappingDescriptor = findDescriptor(dataObject.getClassName());
    if (classMappingDescriptor == null)
    {
      classMappingDescriptor = objectFactory.createClassMappingDescriptor();
      mop.getClassMappingDescriptor().add(classMappingDescriptor);
      classMappingDescriptor.setClassName(dataObject.getClassName());
    }
    classMappingDescriptor.setDateFormat(dataObject.getPayloadDateFormat());
    classMappingDescriptor.setDateTimeFormat(dataObject.getPayloadDateTimeFormat());
    classMappingDescriptor.setOrderBy(dataObject.getOrderBy());
    classMappingDescriptor.setPersisted(dataObject.isPersisted());

    CrudServiceClass service = classMappingDescriptor.getCrudServiceClass();
    if (service == null)
    {
      service = objectFactory.createCrudServiceClass();
      classMappingDescriptor.setCrudServiceClass(service);
      service.setClassName(model.getServicePackageName() + "." + dataObject.getRootDataObject().getClassName());
      service.setAutoIncrementPrimaryKey(true);
      if (dataObject.getParent() == null)
      {
        String remotePersistenceManager = null;
        if (model.isWebServiceDataControl())
        {
          remotePersistenceManager = "DataControlPersistenceManager";
        }
        else if (model.getCurrentDataObject().isXmlPayload())
        {
          remotePersistenceManager = "RestXMLPersistenceManager";
        }
        else
        {
          remotePersistenceManager = "RestJSONPersistenceManager";
        }
        service.setLocalPersistenceManager("oracle.ateam.sample.mobile.v2.persistence.manager.DBPersistenceManager");
        service.setRemotePersistenceManager("oracle.ateam.sample.mobile.v2.persistence.manager." +
                                            remotePersistenceManager);
        service.setRemoteReadInBackground(true);
        service.setRemoteWriteInBackground(true);
        service.setShowWebServiceInvocationErrors(true);
        service.setAutoQuery(true);
      }
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
    if (methods==null)
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
        // new method, set default header params
        setHeaderParams(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, dataObject.getFindAllMethod());
    }
    if (dataObject.getFindMethod() != null)
    {
      Method jaxbMethod = methods.getFindMethod();
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.setFindMethod(jaxbMethod);
        // new method, set default header params
        setHeaderParams(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, dataObject.getFindMethod());
    }
    for (DCMethod findAllInParentMethod: dataObject.getFindAllInParentMethods())
    {
      Method jaxbMethod = findMethod(methods, findAllInParentMethod.getAccessorAttribute());
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.getFindAllInParentMethod().add(jaxbMethod);
        // new method, set default header params
        setHeaderParams(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, findAllInParentMethod);
    }
    for (DCMethod getAsParentMethod: dataObject.getGetAsParentMethods())
    {
      Method jaxbMethod = findMethod(methods, getAsParentMethod.getAccessorAttribute());
      if (jaxbMethod == null)
      {
        jaxbMethod = objectFactory.createMethod();
        methods.getGetAsParentMethod().add(jaxbMethod);
        // new method, set default header params
        setHeaderParams(jaxbMethod);
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
        // new method, set default header params
        setHeaderParams(jaxbMethod);
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
        // new method, set default header params
        setHeaderParams(jaxbMethod);
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
        // new method, set default header params
        setHeaderParams(jaxbMethod);
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
        // new method, set default header params
        setHeaderParams(jaxbMethod);
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
        // new method, set default header params
        setHeaderParams(jaxbMethod);
      }
      setMethodPropertiesAndParams(jaxbMethod, dataObject.getDeleteMethod());
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
      jaxbMethod.setUri(wizardMethod.getName());
      jaxbMethod.setConnectionName(wizardMethod.getConnectionName());
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
  }

  private void createDirectAttributeMapping(AttributeMappings attributeMappings, AttributeInfo attr)
  {
    DirectMapping mapping = objectFactory.createDirectMapping();
    attributeMappings.getDirectMapping().add(mapping);
    mapping.setAttributeName(attr.getAttrName());
    mapping.setColumnName(attr.getColumnName());
    mapping.setColumnDataType(attr.getColumnType());
    if (attr.getParentReferenceAttribute()!=null)
    {
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
    mapping.setAccessorMethod(acc.getChildAccessorMethod().getName());
    mapping.setAttributeName(acc.getChildAccessorName());
    mapping.setPayloadAttributeName(acc.getChildAccessorPayloadName());
    mapping.setReferenceClassName(acc.getChildDataObject().getClassName());
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
    mapping.setReferenceClassName(acc.getParentDataObject().getClassName());
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

  private void setHeaderParams(Method method)
  {
    for (HeaderParam param: model.getHeaderParams())
    {
      if (param.getName() != null && !"".equals(param.getName()) && param.getValue() != null &&
          !"".equals(param.getValue()))
      {
        HeaderParameter headerParam = objectFactory.createHeaderParameter();
        method.getHeaderParameter().add(headerParam);
        headerParam.setName(param.getName());
        headerParam.setValue(param.getValue());
      }
    }
  }

  private Method findMethod(Methods methods, String name)
  {
    Method found = null;
    for (Method method: methods.getFindAllInParentMethod())
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
      JAXBContext jc = JAXBContext.newInstance(instancePath,ObjectFactory.class.getClassLoader());
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//  // SDA 07-nov-2012: use encoding of the node, need utf-8 to keep diacritic characters
//  // by using node encoding, developers can still change the encoding if desired
//  //      String encoding = IdeUtil.getIdeIanaEncoding();
//      String encoding = node.getLoadEncoding()!=null ? node.getLoadEncoding() : "UTF-8";
//      m.setProperty(Marshaller.JAXB_ENCODING, encoding);

      String output = null;
        StringWriter sw = new StringWriter();
        m.marshal(mop,
                  sw); // new FileOutputStream("c:/temp/appstruct-test.xml"));
        output = sw.toString();
        return output;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

}
