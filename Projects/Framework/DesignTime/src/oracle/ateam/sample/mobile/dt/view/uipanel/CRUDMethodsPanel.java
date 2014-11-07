package oracle.ateam.sample.mobile.dt.view.uipanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.bali.ewt.shuttle.ListPicker;
import oracle.bali.ewt.shuttle.Shuttle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import javax.swing.ListModel;

import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;

import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.UIAttributeInfo;
import oracle.ateam.sample.mobile.dt.model.UIDataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.UIGeneratorModel;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ateam.sample.mobile.dt.view.wizard.UIGeneratorWizard;

import oracle.ateam.sample.mobile.dt.util.StringUtils;

import oracle.bali.ewt.dialog.JEWTDialog;

import oracle.bali.ewt.shuttle.ItemPicker;

import oracle.bali.ewt.shuttle.ReorderableListPicker;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;

import oracle.javatools.icons.OracleIcons;


public class CRUDMethodsPanel
  extends DefaultTraversablePanel
  implements ActionListener
{

  private BusinessObjectGeneratorModel model;

  JLabel instruction = new JLabel("Set CRUD Methods for each service object");
  JLabel doiLabel = new JLabel("Service Object");
  JComboBox doilist = new JComboBox();
  transient Map<String, DataObjectInfo> dataObjectMap = new HashMap<String, DataObjectInfo>();
  transient Map<String, DCMethod> methodMap = new HashMap<String, DCMethod>();

  private JLabel findAllLabel = new JLabel("Find All Method");
  private JComboBox findAllField = new JComboBox();
  private JLabel getCanonicalLabel = new JLabel("Get Canonical Method");
  private JTextField getCanonicalField = new JTextField();
  private JLabel findLabel = new JLabel("Quick Search Method");
  private JComboBox findField = new JComboBox();
  private JLabel createLabel = new JLabel("Create Method");
  private JComboBox createField = new JComboBox();
  private JLabel updateLabel = new JLabel("Update Method");
  private JComboBox updateField = new JComboBox();
  private JLabel deleteLabel = new JLabel("Delete Method");
  private JComboBox deleteField = new JComboBox();
  private JLabel mergeLabel = new JLabel("Merge Method");
  private JComboBox mergeField = new JComboBox();
  private JLabel deleteLocalRowsLabel = new JLabel("Delete Local Rows on Find All?");
  private JCheckBox deleteLocalRowsField = new JCheckBox();
  JLabel sortLabel = new JLabel("Sort Order");
  JTextField sortField = new JTextField();
  JLabel dateFormatLabel = new JLabel("Payload Date Format");
  JTextField dateFormatField = new JTextField();
  JLabel dateTimeFormatLabel = new JLabel("Payload DateTime Format");
  JTextField dateTimeFormatField = new JTextField();


  final JEWTDialog sortingDialog = JEWTDialog.createDialog(this, "Set sorting attributes", JEWTDialog.BUTTON_DEFAULT);
  private JButton sortDialogButton = new JButton(OracleIcons.getIcon(OracleIcons.EDIT));
  private Shuttle sortingShuttle = new Shuttle();
  JList attrList = new JList();
  JList sortList = new JList();

  private DataObjectInfo currentDataObject;

  public CRUDMethodsPanel()
  {
    // GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty
    //                  , int anchor, int fill, Insets insets, int ipadx, int ipady)
    // Insets(int top, int left, int bottom, int right)

    doilist.addActionListener(this);
    buildSortingDialog();
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
    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(findAllLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(findAllField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(deleteLocalRowsLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(deleteLocalRowsField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(findLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(findField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(getCanonicalLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(getCanonicalField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(createLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(createField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(updateLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(updateField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(mergeLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(mergeField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(deleteLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(deleteField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(sortLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(sortField, gbc);
    gbc.weightx = 0;
    sortField.setToolTipText("Specify comma-delimited list of attributes to sort on. Add space-separated 'desc' after attribute name to indicate descending order");
    sortField.setEditable(true);
    sortField.setSize(80, 1);

    gbc.gridx++;
    contentPanel.add(sortDialogButton, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(dateFormatLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(dateFormatField, gbc);
    gbc.weightx = 0;

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(dateTimeFormatLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(dateTimeFormatField, gbc);
    gbc.weightx = 0;
  }

  private void populateDataObjectList()
  {
    Map<String, DCMethod> methods = model.getDataControlVisitor().getCRUDMethods();
    List<DataObjectInfo> dois = model.getSelectedDataObjects();
    List<String> dataObjectNames = new ArrayList<String>();
    for (DataObjectInfo doi: dois)
    {
      if (doi.getParent()==null)
      {
        doi.initCrudMethodsIfNeeded(methods);
        dataObjectMap.put(doi.getClassName() + "Service", doi);
        dataObjectNames.add(doi.getClassName() + "Service");
      }
    }
    doilist.setModel(new DefaultComboBoxModel(dataObjectNames.toArray()));
  }

  private void populateMethodLists()
  {
    Map<String, DCMethod> methods = model.getDataControlVisitor().getCRUDMethods();

    List<String> methodNames = new ArrayList<String>();
    List<String> writeMethodNames = new ArrayList<String>();
    methodNames.add("");
    writeMethodNames.add("");
    Iterator<String> iterator = methods.keySet().iterator();
    while (iterator.hasNext())
    {
      String methodName = iterator.next();
      methodNames.add(methodName);
      methodMap.put(methodName, methods.get(methodName));
      // method must have at least one param to be a write action
      if (methods.get(methodName).getParams().size()>0)
      {
        writeMethodNames.add(methodName);       
      }
    }
    findAllField.setModel(new DefaultComboBoxModel(methodNames.toArray()));
    findField.setModel(new DefaultComboBoxModel(methodNames.toArray()));
    createField.setModel(new DefaultComboBoxModel(writeMethodNames.toArray()));
    updateField.setModel(new DefaultComboBoxModel(writeMethodNames.toArray()));
    mergeField.setModel(new DefaultComboBoxModel(writeMethodNames.toArray()));
    deleteField.setModel(new DefaultComboBoxModel(writeMethodNames.toArray()));
  }

  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    populateDataObjectList();
    populateMethodLists();
    if (doilist.getItemCount() > 0)
    {
      DataObjectInfo doi = dataObjectMap.get(doilist.getSelectedItem());
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
    if (e.getSource() == doilist)
    {
      String dataObject = (String) doilist.getSelectedItem();
      saveDataObjectProps();
      setCurrentDataObject(dataObjectMap.get(dataObject));
    }
  }

  private void saveDataObjectProps()
  {
    getCurrentDataObject().setFindAllMethod(methodMap.get(findAllField.getSelectedItem()));
    getCurrentDataObject().setFindMethod(methodMap.get(findField.getSelectedItem()));
    getCurrentDataObject().setCreateMethod(methodMap.get(createField.getSelectedItem()));
    getCurrentDataObject().setUpdateMethod(methodMap.get(updateField.getSelectedItem()));
    getCurrentDataObject().setMergeMethod(methodMap.get(mergeField.getSelectedItem()));
    getCurrentDataObject().setDeleteMethod(methodMap.get(deleteField.getSelectedItem()));
    getCurrentDataObject().setDeleteLocalRows(deleteLocalRowsField.isSelected());
    String orderBy = convertToSQLOrderBy(sortField.getText());
    getCurrentDataObject().setSortOrder(sortField.getText());
    getCurrentDataObject().setOrderBy(orderBy);
    getCurrentDataObject().setPayloadDateFormat(dateFormatField.getText());
    getCurrentDataObject().setPayloadDateTimeFormat(dateTimeFormatField.getText());
    dateFormatField.setText(currentDataObject.getPayloadDateFormat());
    dateTimeFormatField.setText(currentDataObject.getPayloadDateTimeFormat());
  }

  public void setCurrentDataObject(DataObjectInfo currentDataObject)
  {
    this.currentDataObject = currentDataObject;
    findAllField.setSelectedItem(currentDataObject.getFindAllMethod() != null?
                                 currentDataObject.getFindAllMethod().getName(): null);
    findField.setSelectedItem(currentDataObject.getFindMethod() != null? currentDataObject.getFindMethod().getName():
                              null);
    getCanonicalField.setText(currentDataObject.getGetCanonicalMethod() != null?
                                 currentDataObject.getGetCanonicalMethod().getName(): null);
    createField.setSelectedItem(currentDataObject.getCreateMethod() != null?
                                currentDataObject.getCreateMethod().getName(): null);
    updateField.setSelectedItem(currentDataObject.getUpdateMethod() != null?
                                currentDataObject.getUpdateMethod().getName(): null);
    mergeField.setSelectedItem(currentDataObject.getMergeMethod() != null?
                               currentDataObject.getMergeMethod().getName(): null);
    deleteField.setSelectedItem(currentDataObject.getDeleteMethod() != null?
                                currentDataObject.getDeleteMethod().getName(): null);
    deleteLocalRowsField.setSelected(currentDataObject.isDeleteLocalRows());
    sortField.setText(currentDataObject.getSortOrder());
    dateFormatField.setText(currentDataObject.getPayloadDateFormat());
    dateTimeFormatField.setText(currentDataObject.getPayloadDateTimeFormat());
  }

  public DataObjectInfo getCurrentDataObject()
  {
    return currentDataObject;
  }

  private String convertToSQLOrderBy(String sortOrder)
  {
    if (sortOrder == null)
    {
      return null;
    }
    StringBuffer orderBy = new StringBuffer();
    String[] attrs = StringUtils.stringToStringArray(sortOrder, ",");
    boolean firstAttr = true;
    for (int i = 0; i < attrs.length; i++)
    {
      String orderAttr = attrs[i].trim();
      String attrName = orderAttr;
      String ascDesc = null;
      int spacePos = orderAttr.lastIndexOf(" ");
      if (spacePos > -1)
      {
        attrName = orderAttr.substring(0, spacePos);
        ascDesc = orderAttr.substring(spacePos + 1);
        ascDesc = ascDesc.trim();
        if (!"desc".equalsIgnoreCase(ascDesc) && !"asc".equalsIgnoreCase(ascDesc))
        {
          continue;
          //          throw new RuntimeException("Sort Order is invalid, you cannot use "+ascDesc);
        }
      }
      AttributeInfo attributeDef = getCurrentDataObject().getAttributeDef(attrName);
      if (attributeDef == null)
      {
        continue;
        //        throw new RuntimeException("Attribute name "+attr+" in Sort Order is invalid");
      }
      if (firstAttr)
      {
        firstAttr = false;
      }
      else
      {
        orderBy.append(",");
      }
      orderBy.append(attributeDef.getColumnName());
      if (ascDesc != null)
      {
        orderBy.append(" ");
        orderBy.append(ascDesc);
      }
    }
    return orderBy.toString();
  }

  private void buildSortingDialog()
  {
    sortDialogButton.setToolTipText("Select sorting attributes");
    JPanel dialogPanel = new JPanel();
    sortingDialog.setContent(dialogPanel);
    sortingDialog.setPreferredSize(400, 300);
    //    newAccessorDialog.setOKButtonEnabled(true);
    //    newAccessorDialog.setOKButtonText("OK");
    sortingDialog.setResizable(true);
    sortingDialog.setModal(true);
    sortingDialog.setButtonMask((JEWTDialog.BUTTON_OK | JEWTDialog.BUTTON_CANCEL));
    sortingDialog.setDefaultButton(JEWTDialog.BUTTON_OK);
    sortDialogButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        String[] attrNames = new String[getCurrentDataObject().getAttributeDefs().size()];
        int counter = 0;
        for (AttributeInfo attr: getCurrentDataObject().getAttributeDefs())
        {
          attrNames[counter] = attr.getAttrName();
          counter++;
        }
        attrList.setListData(attrNames);
        String[] sortAttrs = new String[]
          { };
        sortList.setListData(sortAttrs);
        //        sortingDialog.setOKButtonEnabled(false);
        boolean OK = sortingDialog.runDialog();
        if (OK)
        {
          ListModel listModel = sortList.getModel();
          List<String> attrs = new ArrayList<String>();
          for (int i = 0; i < listModel.getSize(); i++)
          {
            attrs.add((String) listModel.getElementAt(i));
          }

          sortField.setText(StringUtils.listToString(attrs, ","));
        }
      }
    });
    GridBagLayout containerLayout = new GridBagLayout();
    dialogPanel.setLayout(containerLayout);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.fill = GridBagConstraints.BOTH;

    gbc.insets = new Insets(5, 5, 5, 5);
    dialogPanel.add(sortingShuttle, gbc);
    ListPicker attrPicker = new ListPicker(attrList);
    sortingShuttle.setFromPicker(attrPicker);
    ListPicker sortPicker = new ReorderableListPicker(sortList);
    sortingShuttle.setToPicker(sortPicker);
  }

}
