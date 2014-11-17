/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 10-nov-2014   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package mobile.model;

import oracle.ateam.sample.mobile.persistence.model.Entity;


public class Location
  extends Entity
{

  private Integer locationId;
  private String streetAddress;
  private String postalCode;
  private String city;
  private String stateProvince;
  private String countryId;


  public Integer getLocationId()
  {
    return this.locationId;
  }

  public void setLocationId(Integer locationId)
  {
    this.locationId = locationId;
  }

  public String getStreetAddress()
  {
    return this.streetAddress;
  }

  public void setStreetAddress(String streetAddress)
  {
    this.streetAddress = streetAddress;
  }

  public String getPostalCode()
  {
    return this.postalCode;
  }

  public void setPostalCode(String postalCode)
  {
    this.postalCode = postalCode;
  }

  public String getCity()
  {
    return this.city;
  }

  public void setCity(String city)
  {
    this.city = city;
  }

  public String getStateProvince()
  {
    return this.stateProvince;
  }

  public void setStateProvince(String stateProvince)
  {
    this.stateProvince = stateProvince;
  }

  public String getCountryId()
  {
    return this.countryId;
  }

  public void setCountryId(String countryId)
  {
    this.countryId = countryId;
  }


}
