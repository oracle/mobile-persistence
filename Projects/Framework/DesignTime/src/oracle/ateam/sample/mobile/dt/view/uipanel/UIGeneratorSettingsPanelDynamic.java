package oracle.ateam.sample.mobile.dt.view.uipanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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


public class UIGeneratorSettingsPanelDynamic
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
  private JLabel listAttributeLabel = new JLabel("List Attribute");
  private JComboBox listAttributeField = new JComboBox();
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

  public UIGeneratorSettingsPanelDynamic()
  {
    // GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty
    //                  , int anchor, int fill, Insets insets, int ipadx, int ipady)
    // Insets(int top, int left, int bottom, int right)

    doilist.addActionListener(this);
    layoutStyleField.addItem(UIDataObjectInfo.LAYOUT_STYLE_LIST_FORM);
    layoutStyleField.addItem(UIDataObjectInfo.LAYOUT_STYLE_LIST);
    layoutStyleField.addItem(UIDataObjectInfo.LAYOUT_STYLE_FORM);

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

    gbc.insets = new Insets(0, 0, 5, 5);
    Map<JComponent,JComponent> fields = new TreeMap<JComponent,JComponent>();
    fields.put(layoutStyleLabel, layoutStyleField);
    fields.put(displayTitleSingularLabel, displayTitleSingularField);
    fields.put(displayTitlePluralLabel, displayTitlePluralField);
    fields.put(listAttributeLabel, listAttributeField);
    fields.put(createLabel, createField);
    fields.put(updateLabel, updateField);
    fields.put(deleteLabel, deleteField);
    fields.put(samePageLabel, samePageField);
    fields.put(quickSearchLabel, quickSearchField);
    
    Iterator<JComponent> labels =fields.keySet().iterator();
    while (labels.hasNext())
    {
      JComponent label = labels.next();
      JComponent field = fields.get(label);
      gbc.gridy++;
      gbc.gridx = 0;
      contentPanel.add(label, gbc);
      gbc.gridx++;
      gbc.weightx = 1.0f;
      contentPanel.add(field, gbc);
      gbc.weightx = 0;
    }
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

  private void populateListAttributeList()
  {
    List<String> attrNames = new ArrayList<String>();
    for (UIAttributeInfo attr: currentDataObject.getAttributeDefs())
    {
      attrNames.add(attr.getAttrName());
    }
    listAttributeField.setModel(new DefaultComboBoxModel(attrNames.toArray()));
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
    String dataObject = (String) doilist.getSelectedItem();
    saveDataObjectProps();
    setCurrentDataObject(dataObjectMap.get(dataObject));
  }

  private void saveDataObjectProps()
  {
    getCurrentDataObject().setLayoutStyle((String) layoutStyleField.getSelectedItem());
    getCurrentDataObject().setDisplayTitleSingular(displayTitleSingularField.getText());
    getCurrentDataObject().setDisplayTitlePlural(displayTitlePluralField.getText());
    getCurrentDataObject().setListAttribute1((String) listAttributeField.getSelectedItem());
    getCurrentDataObject().setCreate(createField.isSelected());
    getCurrentDataObject().setUpdate(updateField.isSelected());
    getCurrentDataObject().setDelete(deleteField.isSelected());
    getCurrentDataObject().setSamePage(samePageField.isSelected());
    getCurrentDataObject().setHasQuickSearch(quickSearchField.isSelected());
  }

  public void setCurrentDataObject(UIDataObjectInfo currentDataObject)
  {
    this.currentDataObject = currentDataObject;
    populateListAttributeList();
    layoutStyleField.setSelectedItem(currentDataObject.getLayoutStyle());
    displayTitleSingularField.setText(currentDataObject.getDisplayTitleSingular());
    displayTitlePluralField.setText(currentDataObject.getDisplayTitlePlural());
    listAttributeField.setSelectedItem(currentDataObject.getListAttribute1());
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
}
