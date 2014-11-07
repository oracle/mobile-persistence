package oracle.ateam.sample.mobile.dt.model;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import oracle.adfdt.model.objects.DataControl;

import oracle.ateam.sample.mobile.dt.controller.parser.DataControlUIDataObjectParser;
import oracle.ateam.sample.mobile.dt.controller.PageDefGenerator;

import oracle.ide.Ide;

import oracle.javatools.db.DBObjectProvider;

import oracle.jdeveloper.db.ConnectionInfo;

public class UIGeneratorModel {

    private String dataControlName;
    private DataControl dataControl;
    private List<UIDataObjectInfo> dataObjectInfos;
    private UIDataObjectInfo currentDataObject;
    private DataControlUIDataObjectParser dataControlVisitor;
    private boolean overwritePages = true;
    private PageDefGenerator pageDef;
    private TaskFlowModel taskFlowModel; 
    private boolean enableSecurity = false;

    public void setEnableSecurity(boolean enableSecurity) {
        this.enableSecurity = enableSecurity;
    }

    public boolean isEnableSecurity() {
        return enableSecurity;
    }

    public UIGeneratorModel()
   {
   }

  public void setDataControlName(String dataControlName)
  {
    this.dataControlName = dataControlName;
  }
  
  public String getDataControlName()
  {
    return dataControlName;
  }

  public void setDataControl(DataControl dataControl)
  {
    this.dataControl = dataControl;
  }

  public DataControl getDataControl()
  {
    return dataControl;
  }

  public void setDataObjectInfos(List<UIDataObjectInfo> dataObjectInfos)
  {
    this.dataObjectInfos = dataObjectInfos;
  }

  public List<UIDataObjectInfo> getDataObjectInfos()
  {
    return dataObjectInfos;
  }

  public void setCurrentDataObject(UIDataObjectInfo currentDataObject)
  {
    this.currentDataObject = currentDataObject;
  }

  public UIDataObjectInfo getCurrentDataObject()
  {
    return currentDataObject;
  }

  public void setDataControlVisitor(DataControlUIDataObjectParser dataControlVisitor)
  {
    this.dataControlVisitor = dataControlVisitor;
  }

  public DataControlUIDataObjectParser getDataControlVisitor()
  {
    return dataControlVisitor;
  }

  public void setOverwritePages(boolean overwritePages)
  {
    this.overwritePages = overwritePages;
  }

  public boolean isOverwritePages()
  {
    return overwritePages;
  }

  public void setPageDef(PageDefGenerator pageDef)
  {
    this.pageDef = pageDef;
  }

  public PageDefGenerator getPageDef()
  {
    return pageDef;
  }

  public void setTaskFlowModel(TaskFlowModel taskFlowModel)
  {
    this.taskFlowModel = taskFlowModel;
  }

  public TaskFlowModel getTaskFlowModel()
  {
    return taskFlowModel;
  }

}
