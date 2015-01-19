
package oracle.ateam.sample.mobile.dt.model.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the oracle.ateam.sample.mobile.dt.model.jaxb package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory
{

  private final static QName _MergeMethod_QNAME =
    new QName("http://www.oracle.com/ateam/mobile/persistenceMapping", "mergeMethod");
  private final static QName _GetAsParentMethod_QNAME =
    new QName("http://www.oracle.com/ateam/mobile/persistenceMapping", "getAsParentMethod");
  private final static QName _RemoveMethod_QNAME =
    new QName("http://www.oracle.com/ateam/mobile/persistenceMapping", "removeMethod");
  private final static QName _UpdateMethod_QNAME =
    new QName("http://www.oracle.com/ateam/mobile/persistenceMapping", "updateMethod");
  private final static QName _CustomMethod_QNAME =
    new QName("http://www.oracle.com/ateam/mobile/persistenceMapping", "customMethod");
  private final static QName _FindMethod_QNAME =
    new QName("http://www.oracle.com/ateam/mobile/persistenceMapping", "findMethod");
  private final static QName _CreateMethod_QNAME =
    new QName("http://www.oracle.com/ateam/mobile/persistenceMapping", "createMethod");
  private final static QName _FindAllInParentMethod_QNAME =
    new QName("http://www.oracle.com/ateam/mobile/persistenceMapping", "findAllInParentMethod");
  private final static QName _FindAllMethod_QNAME =
    new QName("http://www.oracle.com/ateam/mobile/persistenceMapping", "findAllMethod");
  private final static QName _GetCanonicalMethod_QNAME =
    new QName("http://www.oracle.com/ateam/mobile/persistenceMapping", "getCanonicalMethod");

  /**
   * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: oracle.ateam.sample.mobile.dt.model.jaxb
   *
   */
  public ObjectFactory()
  {
  }

  /**
   * Create an instance of {@link Methods }
   *
   */
  public Methods createMethods()
  {
    return new Methods();
  }

  /**
   * Create an instance of {@link Method }
   *
   */
  public Method createMethod()
  {
    return new Method();
  }

  /**
   * Create an instance of {@link OneToManyMapping }
   *
   */
  public OneToManyMapping createOneToManyMapping()
  {
    return new OneToManyMapping();
  }

  /**
   * Create an instance of {@link AttributeMapping }
   *
   */
  public AttributeMapping createAttributeMapping()
  {
    return new AttributeMapping();
  }

  /**
   * Create an instance of {@link ForeignKeyColumnReference }
   *
   */
  public ForeignKeyColumnReference createForeignKeyColumnReference()
  {
    return new ForeignKeyColumnReference();
  }

  /**
   * Create an instance of {@link OneToOneMapping }
   *
   */
  public OneToOneMapping createOneToOneMapping()
  {
    return new OneToOneMapping();
  }

  /**
   * Create an instance of {@link Table }
   *
   */
  public Table createTable()
  {
    return new Table();
  }

  /**
   * Create an instance of {@link PrimaryKeyColumn }
   *
   */
  public PrimaryKeyColumn createPrimaryKeyColumn()
  {
    return new PrimaryKeyColumn();
  }

  /**
   * Create an instance of {@link Parameter }
   *
   */
  public Parameter createParameter()
  {
    return new Parameter();
  }

  /**
   * Create an instance of {@link MobileObjectPersistence }
   *
   */
  public MobileObjectPersistence createMobileObjectPersistence()
  {
    return new MobileObjectPersistence();
  }

  /**
   * Create an instance of {@link ClassMappingDescriptor }
   *
   */
  public ClassMappingDescriptor createClassMappingDescriptor()
  {
    return new ClassMappingDescriptor();
  }

  /**
   * Create an instance of {@link CrudServiceClass }
   *
   */
  public CrudServiceClass createCrudServiceClass()
  {
    return new CrudServiceClass();
  }

  /**
   * Create an instance of {@link AttributeMappings }
   *
   */
  public AttributeMappings createAttributeMappings()
  {
    return new AttributeMappings();
  }

  /**
   * Create an instance of {@link DirectMapping }
   *
   */
  public DirectMapping createDirectMapping()
  {
    return new DirectMapping();
  }

  /**
   * Create an instance of {@link HeaderParameter }
   *
   */
  public HeaderParameter createHeaderParameter()
  {
    return new HeaderParameter();
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Method }{@code >}}
   *
   */
  @XmlElementDecl(namespace = "http://www.oracle.com/ateam/mobile/persistenceMapping", name = "mergeMethod")
  public JAXBElement<Method> createMergeMethod(Method value)
  {
    return new JAXBElement<Method>(_MergeMethod_QNAME, Method.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Method }{@code >}}
   *
   */
  @XmlElementDecl(namespace = "http://www.oracle.com/ateam/mobile/persistenceMapping", name = "getAsParentMethod")
  public JAXBElement<Method> createGetAsParentMethod(Method value)
  {
    return new JAXBElement<Method>(_GetAsParentMethod_QNAME, Method.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Method }{@code >}}
   *
   */
  @XmlElementDecl(namespace = "http://www.oracle.com/ateam/mobile/persistenceMapping", name = "removeMethod")
  public JAXBElement<Method> createRemoveMethod(Method value)
  {
    return new JAXBElement<Method>(_RemoveMethod_QNAME, Method.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Method }{@code >}}
   *
   */
  @XmlElementDecl(namespace = "http://www.oracle.com/ateam/mobile/persistenceMapping", name = "updateMethod")
  public JAXBElement<Method> createUpdateMethod(Method value)
  {
    return new JAXBElement<Method>(_UpdateMethod_QNAME, Method.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Method }{@code >}}
   *
   */
  @XmlElementDecl(namespace = "http://www.oracle.com/ateam/mobile/persistenceMapping", name = "customMethod")
  public JAXBElement<Method> createCustomMethod(Method value)
  {
    return new JAXBElement<Method>(_CustomMethod_QNAME, Method.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Method }{@code >}}
   *
   */
  @XmlElementDecl(namespace = "http://www.oracle.com/ateam/mobile/persistenceMapping", name = "findMethod")
  public JAXBElement<Method> createFindMethod(Method value)
  {
    return new JAXBElement<Method>(_FindMethod_QNAME, Method.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Method }{@code >}}
   *
   */
  @XmlElementDecl(namespace = "http://www.oracle.com/ateam/mobile/persistenceMapping", name = "createMethod")
  public JAXBElement<Method> createCreateMethod(Method value)
  {
    return new JAXBElement<Method>(_CreateMethod_QNAME, Method.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Method }{@code >}}
   *
   */
  @XmlElementDecl(namespace = "http://www.oracle.com/ateam/mobile/persistenceMapping", name = "findAllInParentMethod")
  public JAXBElement<Method> createFindAllInParentMethod(Method value)
  {
    return new JAXBElement<Method>(_FindAllInParentMethod_QNAME, Method.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Method }{@code >}}
   *
   */
  @XmlElementDecl(namespace = "http://www.oracle.com/ateam/mobile/persistenceMapping", name = "findAllMethod")
  public JAXBElement<Method> createFindAllMethod(Method value)
  {
    return new JAXBElement<Method>(_FindAllMethod_QNAME, Method.class, null, value);
  }

  /**
   * Create an instance of {@link JAXBElement }{@code <}{@link Method }{@code >}}
   *
   */
  @XmlElementDecl(namespace = "http://www.oracle.com/ateam/mobile/persistenceMapping", name = "getCanonicalMethod")
  public JAXBElement<Method> createGetCanonicalMethod(Method value)
  {
    return new JAXBElement<Method>(_GetCanonicalMethod_QNAME, Method.class, null, value);
  }

}
