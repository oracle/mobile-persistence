/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.model;

import java.util.ArrayList;
import java.util.List;

import oracle.adfdt.controller.adfc.source.controlflow.ControlFlowRule;

public class TaskFlowModel
{
  private String name;
  private List<ViewActivity> views = new ArrayList<ViewActivity>();
  private List<ControlFlowRule> rules = new ArrayList<ControlFlowRule>();
  
  public TaskFlowModel(String name)
  {
    super();
    this.name = name;
  }
  
  public void addViewActivity(String id, String page)
  {
    views.add(new ViewActivity(id,page));
  }

  public String addListToFormPageControlFlowRule(UIDataObjectInfo dataObject)
  {
    return addControlFlowRule(dataObject.getName()+"List", dataObject.getName(),dataObject.getName());
  }

  public String addChildPageControlFlowRule(String currentPage, UIDataObjectInfo childDataObject)
  {
    String targetPageSuffix = childDataObject.isSamePage() ? "" : "List";
    return addControlFlowRule(currentPage, childDataObject.getName()+targetPageSuffix, childDataObject.getName());
  }

  public String addNewChildPageControlFlowRule(String currentPage, UIDataObjectInfo childDataObject)
  {
    return addControlFlowRule(currentPage, childDataObject.getName(), "new"+childDataObject.getName());
  }

  public String addControlFlowRule(String fromId, String toId, String outcome)
  {
    TaskFlowModel.ControlFlowRule controlFlowRule = new TaskFlowModel.ControlFlowRule(fromId, toId, outcome);
    if (!rules.contains(controlFlowRule))
    {
      rules.add(controlFlowRule);      
    }
    return outcome;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public List<TaskFlowModel.ViewActivity> getViewActivities()
  {
    return views;
  }

  public List<TaskFlowModel.ControlFlowRule> getControlFlowRules()
  {
    return rules;
  }

  public class ViewActivity
  {
    private String id;
    private String page;
    
    public ViewActivity(String id, String page)
    {
      this.id= id;
      this.page = page;      
    }

    public void setId(String id)
    {
      this.id = id;
    }

    public String getId()
    {
      return id;
    }

    public void setPage(String page)
    {
      this.page = page;
    }

    public String getPage()
    {
      return page;
    }

    public boolean equals(Object obj)
    {
      ViewActivity otherView = (TaskFlowModel.ViewActivity) obj;
      return getId().equals(otherView.getId());
    }
  }
  public class ControlFlowRule
  {
    private String fromId;
    private String toId;
    private String outcome;
    
    public ControlFlowRule(String fromId, String toId, String outcome)
    {
      this.fromId= fromId;
      this.toId= toId;
      this.outcome = outcome;      
    }

    public void setFromId(String fromId)
    {
      this.fromId = fromId;
    }

    public String getFromId()
    {
      return fromId;
    }

    public void setToId(String toId)
    {
      this.toId = toId;
    }

    public String getToId()
    {
      return toId;
    }

    public void setOutcome(String outcome)
    {
      this.outcome = outcome;
    }

    public String getOutcome()
    {
      return outcome;
    }
    public boolean equals(Object obj)
    {
      ControlFlowRule otherRule = (TaskFlowModel.ControlFlowRule) obj;
      return getFromId().equals(otherRule.getFromId()) && getToId().equals(otherRule.getToId()) && getOutcome().equals(otherRule.getOutcome());
    }
  }
}
