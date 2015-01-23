/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import oracle.adf.model.adapter.dataformat.MethodDef;

import oracle.ateam.sample.mobile.dt.util.StringUtils;

import oracle.binding.meta.AccessorDefinition;
import oracle.binding.meta.AttributeDefinition;
import oracle.binding.meta.DefinitionContainer;
import oracle.binding.meta.NamedDefinition;
import oracle.binding.meta.StructureDefinition;
import oracle.javatools.db.Table;
import oracle.javatools.db.Column;

import oracle.toplink.workbench.addin.mappings.spi.db.JDeveloperTable;
import oracle.toplink.workbench.mappingsmodel.spi.db.ExternalColumn;


public class UIDataObjectInfo
{
  public static final String LAYOUT_STYLE_LIST_FORM = "list-form";
  public static final String LAYOUT_STYLE_LIST = "list";
  public static final String LAYOUT_STYLE_FORM = "form";
  public static final String DIVIDER_MODE_ALL = "all";
  public static final String DIVIDER_MODE_FIRST_LETTER = "firstLetter";
  private String name;
  private UIDataObjectInfo parent = null;
  private List<UIAttributeInfo> attributeInfos;
  private List<UIDataObjectInfo> children = new ArrayList<UIDataObjectInfo>();

  private boolean hasListPage = true;
  private boolean hasFormPage = true;
  private boolean hasQuickSearch = true;
  private boolean create = true;
  private boolean update = true;
  private boolean delete = true;
  private boolean samePage = false;
  private String listAttribute1;
  private String listAttribute2;
  private String listAttribute3;
  private String listAttribute4;
  private String dividerAttribute;
  private String dividerMode  = DIVIDER_MODE_FIRST_LETTER;   // all / 
  private StructureDefinition beanDef;
  private AccessorDefinition accessor;
  private String displayTitleSingular;
  private String displayTitlePlural;
  private String layoutStyle;
    
  public UIDataObjectInfo(AccessorDefinition accessor,StructureDefinition beanDef,UIDataObjectInfo parent)
  {
    this.name = beanDef.getName();
    this.parent = parent;
    this.beanDef = beanDef;
    this.accessor = accessor;
    setLayoutStyle(LAYOUT_STYLE_LIST_FORM);
    setDisplayTitlePlural(name);
    setDisplayTitleSingular(name);

    attributeInfos = new ArrayList<UIAttributeInfo>();
    DefinitionContainer attributeDefinitions = beanDef.getAttributeDefinitions();
    Iterator iterator = attributeDefinitions.iterator();
    while (iterator.hasNext())
    {
      AttributeDefinition attr = (AttributeDefinition) iterator.next();
      // do key (id) attrs at beginning fopr better layout, hack until we can properly
      // sequence attrs in wizard
      if(attr.getName().equalsIgnoreCase("ID"))
      {
        attributeInfos.add(0,new UIAttributeInfo(this,attr));                       
      }
      else
      {
        attributeInfos.add(new UIAttributeInfo(this,attr));               
      }
      if (listAttribute1==null)
      {
        listAttribute1 = attr.getName();
      }
    }
    if (parent!=null)
    {
      parent.addChild(this);
      setSamePage(true);
      setHasQuickSearch(false);
    }
  }

  public void setName(String wsName)
  {
    this.name = wsName;
  }

  public String getName()
  {
    return name;
  }

  public List<String> getAttributes()
  {
    List<String> attrs = new ArrayList<String>();
    for(UIAttributeInfo attr : getAttributeDefs())
    {
      attrs.add(attr.getAttrName());        
    }
    return attrs;                   
  }


  public List<UIAttributeInfo> getAttributeDefs()
  {
    return attributeInfos;                   
  }

  public UIAttributeInfo getAttributeDef(String name)
  {
    UIAttributeInfo attr = null;
    for(UIAttributeInfo attrInfo : getAttributeDefs())
    {
      if (attrInfo.getAttrName().equals(name))
      {
        attr = attrInfo;
        break;
      }
    }
    return attr;                   
  }

  public void setParent(UIDataObjectInfo parent)
  {
    this.parent = parent;
  }

  public UIDataObjectInfo getParent()
  {
    return parent;
  }

  public UIDataObjectInfo getRootDataObject()
  {
    UIDataObjectInfo root = this;
    while (root.getParent()!=null)
    {
      root = root.getParent();      
    }
    return root;
  }

  public void addChild(UIDataObjectInfo child)
  {
    children.add(child);
  }

  /**
   * Returns child UIDataObjectInfos
   * @return
   */
  public List<UIDataObjectInfo> getChildren()
  {
    return children;
  }

  public List<UIDataObjectInfo> getChildrenOnSamePage()
  {
    List<UIDataObjectInfo> kids = new ArrayList<UIDataObjectInfo>();
    for(UIDataObjectInfo kid : children)
    {
      if (kid.isSamePage())
      {
        kids.add(kid);        
      }
    }
    return kids;
  }

  public List<UIDataObjectInfo> getChildrenOnSeparatePage()
  {
    List<UIDataObjectInfo> kids = new ArrayList<UIDataObjectInfo>();
    for(UIDataObjectInfo kid : children)
    {
      if (!kid.isSamePage())
      {
        kids.add(kid);        
      }
    }
    return kids;
  }

  public void setHasListPage(boolean hasListPage)
  {
    this.hasListPage = hasListPage;
  }

  public boolean isHasListPage()
  {
    return hasListPage;
  }

  public void setHasFormPage(boolean hasFormPage)
  {
    this.hasFormPage = hasFormPage;
  }

  public boolean isHasFormPage()
  {
    return hasFormPage;
  }

  public void setHasQuickSearch(boolean hasQuickSearch)
  {
    this.hasQuickSearch = hasQuickSearch;
  }

  public boolean isHasQuickSearch()
  {
    return hasQuickSearch;
  }

  public void setCreate(boolean create)
  {
    this.create = create;
  }

  public boolean isCreate()
  {
    return create;
  }

  public void setUpdate(boolean update)
  {
    this.update = update;
  }

  public boolean isUpdate()
  {
    return update;
  }

  public void setDelete(boolean delete)
  {
    this.delete = delete;
  }

  public boolean isDelete()
  {
    return delete;
  }

  public void setListAttribute1(String listAttribute)
  {
    this.listAttribute1 = listAttribute;
  }

  public String getFullyQualifiedClassName()
  {
    return beanDef.getFullName();
  }

  public String getAccessorName()
  {
    return accessor.getName();
  }

  public String getListAttribute1()
  {
    return listAttribute1;
  }

  public void setSamePage(boolean samePage)
  {
    this.samePage = samePage;
  }

  public boolean isSamePage()
  {
    return samePage;
  }

  public void setDisplayTitleSingular(String displayTitleSingular)
  {
    this.displayTitleSingular = displayTitleSingular;
  }

  public String getDisplayTitleSingular()
  {
    return displayTitleSingular;
  }

  public void setDisplayTitlePlural(String displayTitlePlural)
  {
    this.displayTitlePlural = displayTitlePlural;
  }

  public String getDisplayTitlePlural()
  {
    return displayTitlePlural;
  }

  public void setLayoutStyle(String layoutStyle)
  {
    this.layoutStyle = layoutStyle;
    if (layoutStyle.equals(LAYOUT_STYLE_LIST_FORM))
    {
      setHasListPage(true);
      setHasFormPage(true);
    }
    else if (layoutStyle.equals(LAYOUT_STYLE_FORM))
    {
      setHasListPage(false);
      setHasFormPage(true);      
    }
    else if (layoutStyle.equals(LAYOUT_STYLE_LIST))
    {
      setHasListPage(true);
      setHasFormPage(false);      
    }
  }

  public String getLayoutStyle()
  {
    return layoutStyle;
  }

  public boolean equals(Object obj)
  {
    if (obj instanceof UIDataObjectInfo)
    {
      UIDataObjectInfo otherObj = (UIDataObjectInfo) obj;
      return getFullyQualifiedClassName().equals(otherObj.getFullyQualifiedClassName());
    }
    return false;
  }

  public void setListAttribute2(String listAttribute2)
  {
    this.listAttribute2 = listAttribute2;
  }

  public String getListAttribute2()
  {
    return listAttribute2;
  }

  public void setListAttribute3(String listAttribute3)
  {
    this.listAttribute3 = listAttribute3;
  }

  public String getListAttribute3()
  {
    return listAttribute3;
  }

  public void setListAttribute4(String listAttribute4)
  {
    this.listAttribute4 = listAttribute4;
  }

  public String getListAttribute4()
  {
    return listAttribute4;
  }

  public void setDividerAttribute(String dividerAttribute)
  {
    this.dividerAttribute = dividerAttribute;
  }

  public String getDividerAttribute()
  {
    return dividerAttribute;
  }

  public void setDividerMode(String dividerMode)
  {
    this.dividerMode = dividerMode;
  }

  public String getDividerMode()
  {
    return dividerMode;
  }
  
  public boolean isSimpleList()
  {
    return getListAttribute2()==null && getListAttribute3()==null && getListAttribute4()!=null;
  }

  public boolean isStartEndList()
  {
    return getListAttribute2()!=null && getListAttribute3()==null && getListAttribute4()!=null;
  }

  public boolean isMainSubList()
  {
    return getListAttribute2()==null && getListAttribute3()!=null && getListAttribute4()==null;
  }

}
