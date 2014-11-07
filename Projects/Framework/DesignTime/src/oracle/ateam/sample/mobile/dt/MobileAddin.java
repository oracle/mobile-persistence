package oracle.ateam.sample.mobile.dt;

import oracle.adfdt.model.datacontrols.AdapterSettings;

import oracle.ide.Addin;

public class MobileAddin
  implements Addin
{
  public MobileAddin()
  {
    super();
  }

  @Override
  public void initialize()
  {
    MobileBeanDataControlFactory factory = new MobileBeanDataControlFactory();
    AdapterSettings.addFactory(factory);    
  }

}
