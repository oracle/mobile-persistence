package oracle.ateam.sample.mobile.dt.model;

public class HeaderParam
{
  
  private String name;
  private String value;
  public HeaderParam()
  {
    super();
  }

  public void setName(String key)
  {
    this.name = key;
  }

  public String getName()
  {
    return name;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof HeaderParam)
    {
      HeaderParam otherParam = (HeaderParam) obj;
      return otherParam.getName()!=null && otherParam.getName().equals(getName());
    }
    return false;
  }
}
