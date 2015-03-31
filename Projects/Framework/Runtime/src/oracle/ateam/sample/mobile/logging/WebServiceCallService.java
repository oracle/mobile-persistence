package oracle.ateam.sample.mobile.logging;


import java.util.List;

import oracle.ateam.sample.mobile.v2.persistence.manager.DBPersistenceManager;
import oracle.ateam.sample.mobile.v2.persistence.service.EntityCRUDService;


/**
 *  Service class that provides CRUD and operations against the webServiceCall data object.
 *  This data object is used to log web service calls in the local SQLite database.
 *  The framework will automatically log each web service (SOAP or REST) call when the property
 *  logWebServiceCalls is set to true in persistence-mapping.xml
 */
public class WebServiceCallService
  extends EntityCRUDService<WebServiceCall>
{

  /**
   * Default constructor. If autoQuery is set to true in the classMappingDescriptor in persistence-mapping.xml, then
   * the findAll method will be executed and the webServiceCall list will be populated when this constructor is invoked.
   * If you created a data control for this service class, the data control will use this constructor, allowing you to
   * immediately show data in your user interface when accessing the data control for the first time.
   * The findAll method will first query the local SQLite database for any rows and show these immediately in
   * the UI.  
   */
  public WebServiceCallService()
  {
  }

  /**
   * Use this constructor with autoQuery=false in Java code when you want to execute a method in this service class
   * without querying the local database for or call entries first. 
   */
  public WebServiceCallService(boolean autoQuery)
  {
    super(autoQuery);
  }
  
  protected Class getEntityClass()
  {
    return WebServiceCall.class;
  }

  protected String getEntityListName()
  {
    return "webServiceCalls";
  }

  public List<WebServiceCall> getWebServiceCalls()
  {
    return getEntityList();
  }

  /**
   * Inserts or updates a webServiceCall using the local DB persistence manager.
   * The insert or update is determined by calling isNewEntity on the webServiceCall instance.
   * @param webServiceCall
   */
  public void saveWebServiceCall(WebServiceCall webServiceCall)
  {
    super.mergeEntity(webServiceCall);
  }

  /**
   * Retrieves all webServiceCall instances using the configured persistence managers and populates the webServiceCall list
   * with the result.
   * When this method is called for the first time, and a remote persistence manager is configured,
   * the data is fetched remotely and the local DB is populated with the results.
   */
  public void findAllWebServiceCalls()
  {
    super.findAll();
  }



  /**
   * Retrieves the webServiceCall instances that match the searchValue filter using the configured persistence
   * managers and populates the webServiceCall list with the result.
   * By default, the search value is applied to all string attributes using a "startsWith" operator.
   * To customize the attributes on which the searchValue is applied, you can override method getQuickSearchAttributeNames.
   * @param searchValue
   */
  public void findWebServiceCalls(String searchValue)
  {
    super.find(searchValue);
  }

  /**
   *  Removes current selection of web service calls from local DB
   */
  public void removeAll() {
      List<WebServiceCall> oldList = getWebServiceCalls();
      DBPersistenceManager pm = getLocalPersistenceManager();
      for (WebServiceCall call :oldList) {
          pm.removeEntity(call, true);
      }
      oldList.clear();
      refreshEntityList(oldList);
  }

}


