package mobile.model.service;


import java.util.ArrayList;

import java.util.List;

import oracle.ateam.sample.mobile.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.persistence.service.EntityCRUDService;

import mobile.model.Region;


public class RegionService
  extends EntityCRUDService
{

  public RegionService()
  {
  }

  protected Class getEntityClass()
  {
    return Region.class;
  }

  protected String getEntityListName()
  {
    return "region";
  }

  public Region[] getRegion()
  {
    List dataObjectList = getEntityList();

    Region[] dataObjects = (Region[]) dataObjectList.toArray(new Region[dataObjectList.size()]);
    return dataObjects;
  }

  /**
   * This method is automatically called when using the Create operation on the region collection
   * in the data control palette. It gets a new region instance as argument and adds this instance to the
   * region list.
   * Do NOT drag and drop this method from the data control palette, use the Create operation instead to ensure
   * that iterator binding and region list stay in sync.
   * @param index
   * @param region
   */
  public void addRegion(int index, Region region)
  {
    addEntity(index, region);
  }

  /**
   * This method is automatically called when using the Delete operation on the region collection
   * in the data control palette. It removes the region instance passed in from the region list, deletes the
   * corresponding row from the database (if persisted) and calls the configured remove method on the remote
   * persistence manager.
   * Do NOT drag and drop this method from the data control palette, use the Delete operation instead to ensure
   * that iterator binding and region list stay in sync.
   * @param region
   */
  public void removeRegion(Region region)
  {
    removeEntity(region);
  }

  /**
   * Inserts or updates a region using the configured persistence managers.
   * The insert or update is determined by calling isNewEntity on the region instance.
   * @param region
   */
  public void saveRegion(Region region)
  {
    super.mergeEntity(region);
  }

  /**
   * Retrieves all region instances using the configured persistence managers and populates the region list
   * with the result.
   * When this method is called for the first time, and a remote persistence manager is configured,
   * the data is fetched remotely and the local DB is populated with the results.
   */
  public void findAllRegion()
  {
    super.findAll();
  }

  /**
   * Retrieves all region instances using the findAll method on the remote persistence manager
   * and populates the region list
   */
  public void findAllRegionRemote()
  {
    super.doRemoteFindAll();
  }

  /**
   * Retrieves the region instances that match the searchValue filter using the configured persistence
   * managers and populates the region list with the result.
   * By default, the search value is applied to all string attributes using a "startsWith" operator.
   * To customize the attributes on which the searchValue is applied, you can override method getQuickSearchAttributeNames.
   * If a find method is configured against the remote persistence manager, then this method will also
   * call this method.
   * @param searchValue
   */
  public void findRegion(String searchValue)
  {
    super.find(searchValue);
  }


  /**
   * Synchronizes all pending data sync actions using the remote persistence manager
   * @param inBackground
   */
  public void synchronizeRegion(Boolean inBackground)
  {
    super.synchronize(inBackground);
  }

  /**
   * Resets the values of the region instance to the values as stored in the SQLite database. This method
   * will do nothing when the region is not persisted to the database.
   * @param region
   */
  public void resetRegion(Region region)
  {
    super.resetEntity(region);
  }

  /**
   * Returns true when there are pending region data sync actions. Returns false if there are no such actions.
   */
  public boolean getHasRegionDataSynchActions()
  {
    return getDataSynchManager().getHasDataSynchActions();
  }
}


