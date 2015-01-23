/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uipanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;

import oracle.ateam.sample.mobile.dt.model.UIAttributeInfo;
import oracle.ateam.sample.mobile.dt.model.UIDataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.UIGeneratorModel;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ateam.sample.mobile.dt.view.wizard.UIGeneratorWizard;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;


public class UIGeneratorSettingsPanel
  extends DefaultTraversablePanel
  implements ActionListener
{

  private UIGeneratorModel model;

  JLabel instruction = new JLabel("Set UI generation properties for each data object");
  JLabel doiLabel = new JLabel("Data Object");
  JComboBox doilist = new JComboBox();
  transient Map<String, UIDataObjectInfo> dataObjectMap = new HashMap<String, UIDataObjectInfo>();

  private JLabel layoutStyleLabel = new JLabel("Layout Style");
  private JComboBox layoutStyleField = new JComboBox();
  private JLabel displayTitleSingularLabel = new JLabel("Display Title Singular");
  private JTextField displayTitleSingularField = new JTextField();
  private JLabel displayTitlePluralLabel = new JLabel("Display Title Plural");
  private JTextField displayTitlePluralField = new JTextField();
  private JLabel listAttributesHeading = new JLabel("List Attributes");
  private JLabel listAttributeLabel1 = new JLabel("Main Left");
  private JComboBox listAttributeField1 = new JComboBox();
  private JLabel listAttributeLabel2 = new JLabel("Main Right");
  private JComboBox listAttributeField2 = new JComboBox();
  private JLabel listAttributeLabel3 = new JLabel("Sub Left");
  private JComboBox listAttributeField3 = new JComboBox();
  private JLabel listAttributeLabel4 = new JLabel("Sub Right");
  private JComboBox listAttributeField4 = new JComboBox();
  private JLabel dividerAttributeLabel = new JLabel("Divider Attribute");
  private JComboBox dividerAttributeField = new JComboBox();
  private JLabel dividerModeLabel = new JLabel("Divider Mode");
  private JComboBox dividerModeField = new JComboBox();

  private JLabel createLabel = new JLabel("Create Allowed?");
  private JCheckBox createField = new JCheckBox();
  private JLabel updateLabel = new JLabel("Update Allowed?");
  private JCheckBox updateField = new JCheckBox();
  private JLabel deleteLabel = new JLabel("Delete Allowed?");
  private JCheckBox deleteField = new JCheckBox();
  private JLabel samePageLabel = new JLabel("Show on Parent Page?");
  private JCheckBox samePageField = new JCheckBox();
  private JLabel quickSearchLabel = new JLabel("Add Quick Search?");
  private JCheckBox quickSearchField = new JCheckBox();

  private UIDataObjectInfo currentDataObject;

  public UIGeneratorSettingsPanel()
  {
    // GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty
    //                  , int anchor, int fill, Insets insets, int ipadx, int ipady)
    // Insets(int top, int left, int bottom, int right)

    doilist.addActionListener(this);
    dividerAttributeField.addActionListener(this);
    setLayout(new BorderLayout(0, 15));

    JPanel contentPanel = new JPanel();
    add(contentPanel, BorderLayout.NORTH);

    GridBagLayout containerLayout = new GridBagLayout();
    contentPanel.setLayout(containerLayout);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    gbc.insets = new Insets(0, 0, 20, 5);
    contentPanel.add(doiLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(doilist, gbc);
    gbc.weightx = 0;

    gbc.insets = new Insets(0, 0, 5, 5);
//    gbc.gridy++;
//    gbc.gridx = 0;
//    contentPanel.add(layoutStyleLabel, gbc);
//    gbc.gridx++;
//    contentPanel.add(layoutStyleField, gbc);
    layoutStyleField.addItem(UIDataObjectInfo.LAYOUT_STYLE_LIST_FORM);
    layoutStyleField.addItem(UIDataObjectInfo.LAYOUT_STYLE_LIST);
    layoutStyleField.addItem(UIDataObjectInfo.LAYOUT_STYLE_FORM);
    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(displayTitleSingularLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(displayTitleSingularField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(displayTitlePluralLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(displayTitlePluralField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(listAttributesHeading, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(listAttributeLabel1, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(listAttributeField1, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(listAttributeLabel2, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(listAttributeField2, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(listAttributeLabel3, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(listAttributeField3, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(listAttributeLabel4, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(listAttributeField4, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(dividerAttributeLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(dividerAttributeField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(dividerModeLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(dividerModeField, gbc);
    gbc.weightx = 0;
    dividerModeField.addItem("firstLetter");
    dividerModeField.addItem("all");

    gbc.gridy = 1;
    gbc.gridx = 2;
    gbc.insets = new Insets(0, 5, 5, 0);
    contentPanel.add(createLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(createField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 2;
    contentPanel.add(updateLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(updateField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 2;
    contentPanel.add(deleteLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(deleteField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 2;
    contentPanel.add(samePageLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(samePageField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 2;
    contentPanel.add(quickSearchLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(quickSearchField, gbc);
    gbc.weightx = 0;

  }

  private void populateDataObjectList()
  {
    List<UIDataObjectInfo> dois = model.getDataObjectInfos();
    List<String> dataObjectNames = new ArrayList<String>();
    for (UIDataObjectInfo doi: dois)
    {
      dataObjectMap.put(doi.getName(), doi);
      dataObjectNames.add(doi.getName());
    }
    doilist.setModel(new DefaultComboBoxModel(dataObjectNames.toArray()));
  }

  private void populateListAttributeLists()
  {
    List<String> attrNames = new ArrayList<String>();
    for (UIAttributeInfo attr: currentDataObject.getAttributeDefs())
    {
      if (!"isNewEntity".equals(attr.getAttrName()))
      {
        attrNames.add(attr.getAttrName());        
      }
    }
    listAttributeField1.setModel(new DefaultComboBoxModel(attrNames.toArray()));
    // add empty entry for other non-required list attrs
    attrNames.add(0, "");
    listAttributeField2.setModel(new DefaultComboBoxModel(attrNames.toArray()));
    listAttributeField3.setModel(new DefaultComboBoxModel(attrNames.toArray()));
    listAttributeField4.setModel(new DefaultComboBoxModel(attrNames.toArray()));
    dividerAttributeField.setModel(new DefaultComboBoxModel(attrNames.toArray()));
  }

  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    model = (UIGeneratorModel) tc.get(UIGeneratorWizard.MODEL_KEY);
    populateDataObjectList();
    if (doilist.getItemCount() > 0)
    {
      UIDataObjectInfo doi = dataObjectMap.get(doilist.getSelectedItem());
      setCurrentDataObject(doi);
      doilist.setSelectedItem(doilist.getItemAt(0));
    }
  }

  public void onExit(TraversableContext tc)
  {
    saveDataObjectProps();
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource()==doilist)
    {
      String dataObject = (String) doilist.getSelectedItem();
      saveDataObjectProps();
      setCurrentDataObject(dataObjectMap.get(dataObject));      
    }
    else if (e.getSource()==dividerAttributeField)
    {
      dividerModeField.setEnabled(!"".equals(dividerAttributeField.getSelectedItem()));      
    }
  }

  private void saveDataObjectProps()
  {
    getCurrentDataObject().setLayoutStyle((String) layoutStyleField.getSelectedItem());
    getCurrentDataObject().setDisplayTitleSingular(displayTitleSingularField.getText());
    getCurrentDataObject().setDisplayTitlePlural(displayTitlePluralField.getText());
    getCurrentDataObject().setListAttribute1(returnNullIfEmptyString(listAttributeField1.getSelectedItem()));
    getCurrentDataObject().setListAttribute2(returnNullIfEmptyString(listAttributeField2.getSelectedItem()));
    getCurrentDataObject().setListAttribute3(returnNullIfEmptyString(listAttributeField3.getSelectedItem()));
    getCurrentDataObject().setListAttribute4(returnNullIfEmptyString(listAttributeField4.getSelectedItem()));
    getCurrentDataObject().setDividerAttribute(returnNullIfEmptyString(dividerAttributeField.getSelectedItem()));
    getCurrentDataObject().setDividerMode((String) dividerModeField.getSelectedItem());
    getCurrentDataObject().setCreate(createField.isSelected());
    getCurrentDataObject().setUpdate(updateField.isSelected());
    getCurrentDataObject().setDelete(deleteField.isSelected());
    getCurrentDataObject().setSamePage(samePageField.isSelected());
    getCurrentDataObject().setHasQuickSearch(quickSearchField.isSelected());
  }

  public void setCurrentDataObject(UIDataObjectInfo currentDataObject)
  {
    this.currentDataObject = currentDataObject;
    populateListAttributeLists();
    layoutStyleField.setSelectedItem(currentDataObject.getLayoutStyle());
    displayTitleSingularField.setText(currentDataObject.getDisplayTitleSingular());
    displayTitlePluralField.setText(currentDataObject.getDisplayTitlePlural());
    listAttributeField1.setSelectedItem(currentDataObject.getListAttribute1());
    listAttributeField2.setSelectedItem(currentDataObject.getListAttribute2());
    listAttributeField3.setSelectedItem(currentDataObject.getListAttribute3());
    listAttributeField4.setSelectedItem(currentDataObject.getListAttribute4());
    dividerAttributeField.setSelectedItem(currentDataObject.getDividerAttribute());
    dividerModeField.setSelectedItem(currentDataObject.getDividerMode());
    dividerModeField.setEnabled(getCurrentDataObject().getDividerAttribute()!=null);
    createField.setSelected(currentDataObject.isCreate());
    updateField.setSelected(currentDataObject.isUpdate());
    deleteField.setSelected(currentDataObject.isDelete());
    samePageField.setSelected(currentDataObject.isSamePage());
    quickSearchField.setSelected(currentDataObject.isHasQuickSearch());
    boolean hasParent = currentDataObject.getParent()!=null;
    // show quick search only for top data objects
    // hide samePage for top data objects
//    samePageLabel.setVisible(hasParent);
//    samePageField.setVisible(hasParent);
    samePageField.setEnabled(hasParent);
//    quickSearchLabel.setVisible(!hasParent);
//    quickSearchField.setVisible(!hasParent);
    quickSearchField.setEnabled(!hasParent);
  }

  public UIDataObjectInfo getCurrentDataObject()
  {
    return currentDataObject;
  }

  private String returnNullIfEmptyString(Object object)
  {
    return (String) ("".equals(object) ? null : object);
  }
}
