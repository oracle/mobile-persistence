package mobile.model;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import oracle.ateam.sample.mobile.persistence.util.EntityUtils;
import oracle.ateam.sample.mobile.persistence.model.Entity;

import java.math.BigDecimal;

public class Region
  extends Entity
{

  private BigDecimal regionId;
  private String regionName;

  private List countriesViewList = createIndirectList("countriesViewList");


  public BigDecimal getRegionId()
  {
    return this.regionId;
  }

  public void setRegionId(BigDecimal regionId)
  {
    this.regionId = regionId;
  }

  public String getRegionName()
  {
    return this.regionName;
  }

  public void setRegionName(String regionName)
  {
    this.regionName = regionName;
  }


  public void setCountriesViewList(List countriesViewList)
  {
    this.countriesViewList = countriesViewList;
  }

  /**
   * This method is called when entity instance is recreated from persisted JSON string in DataSynchAction
   */
  public void setCountriesViewList(Country[] countriesViewList)
  {
    this.countriesViewList = Arrays.asList(countriesViewList);
  }

  public List getCountriesViewList()
  {
    return this.countriesViewList;
  }

  public Country[] getCountriesView()
  {
    List dataObjectList = getCountriesViewList();

    return (Country[]) dataObjectList.toArray(new Country[dataObjectList.size()]);
  }

  public void addCountry(int index, Country country)
  {
    country.setIsNewEntity(true);
    EntityUtils.generatePrimaryKeyValue(country, 1);
    country.setRegionId(getRegionId());
    getCountriesViewList().add(index, country);
  }

  public void removeCountry(Country country)
  {
    getCountriesViewList().remove(country);
  }


}
