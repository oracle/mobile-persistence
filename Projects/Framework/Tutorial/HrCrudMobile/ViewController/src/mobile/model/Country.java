/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 10-nov-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package mobile.model;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import oracle.ateam.sample.mobile.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.persistence.model.Entity;

import java.math.BigDecimal;

public class Country
  extends Entity
{

  private String countryId;
  private String countryName;
  private BigDecimal regionId;

  private List locationsViewList = createIndirectList("locationsViewList");


  public String getCountryId()
  {
    return this.countryId;
  }

  public void setCountryId(String countryId)
  {
    this.countryId = countryId;
  }

  public String getCountryName()
  {
    return this.countryName;
  }

  public void setCountryName(String countryName)
  {
    this.countryName = countryName;
  }

  public BigDecimal getRegionId()
  {
    return this.regionId;
  }

  public void setRegionId(BigDecimal regionId)
  {
    this.regionId = regionId;
  }


  public void setLocationsViewList(List locationsViewList)
  {
    this.locationsViewList = locationsViewList;
  }

  /**
   * This method is called when entity instance is recreated from persisted JSON string in DataSynchAction
   */
  public void setLocationsViewList(Location[] locationsViewList)
  {
    this.locationsViewList = Arrays.asList(locationsViewList);
  }

  public List getLocationsViewList()
  {
    return this.locationsViewList;
  }

  public Location[] getLocationsView()
  {
    List dataObjectList = getLocationsViewList();

    return (Location[]) dataObjectList.toArray(new Location[dataObjectList.size()]);
  }

  public void addLocation(int index, Location location)
  {
    location.setIsNewEntity(true);
    EntityUtils.generatePrimaryKeyValue(location, 1);
    location.setCountryId(getCountryId());
    getLocationsViewList().add(index, location);
  }

  public void removeLocation(Location location)
  {
    getLocationsViewList().remove(location);
  }


}
