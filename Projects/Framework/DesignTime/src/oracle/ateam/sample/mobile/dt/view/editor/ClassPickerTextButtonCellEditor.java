/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.editor;

import java.awt.Component;

import oracle.ateam.sample.mobile.dt.util.ProjectUtils;

import oracle.bali.xml.gui.jdev.JDevXmlContext;

import oracle.ide.Context;

import oracle.ide.Ide;

import oracle.jdeveloper.dialogs.ClassPackageBrowserV2;
import oracle.jdeveloper.xml.j2ee.flateditor.AbstractTextButtonCellEditor;

public class ClassPickerTextButtonCellEditor
  extends AbstractTextButtonCellEditor
{
  Component component;
  public ClassPickerTextButtonCellEditor(Context context, Component component)
  {
    super(context);
    this.component = component;
  }

  @Override
  protected String getValueforTextField()
  {
    String[] names = ClassPackageBrowserV2.browseClassOrPackage(component // Ide.getMainWindow(),
                                               ,ProjectUtils.getViewControllerProject()
                                               ,ClassPackageBrowserV2.CLASS_ONLY,false
                                               ,null,null);
    if(names!=null && names.length >0) 
    {
      return names[0];
    }
    else
    {
      return null;   
    }  }
}
