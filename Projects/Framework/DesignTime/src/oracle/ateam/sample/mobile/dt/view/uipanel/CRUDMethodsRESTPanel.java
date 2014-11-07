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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;

import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.util.StringUtils;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.bali.ewt.dialog.JEWTDialog;
import oracle.bali.ewt.shuttle.ListPicker;
import oracle.bali.ewt.shuttle.ReorderableListPicker;
import oracle.bali.ewt.shuttle.Shuttle;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;

import oracle.javatools.icons.OracleIcons;


public class CRUDMethodsRESTPanel
  extends DefaultTraversablePanel
  implements ActionListener
{

  private BusinessObjectGeneratorModel model;

  JLabel instruction = new JLabel("Set CRUD Resources for each service object");
  JLabel doiLabel = new JLabel("Service Object");
  JComboBox doilist = new JComboBox();
  transient Map<String,DataObjectInfo> dataObjectMap = new HashMap<String, DataObjectInfo>();
  transient Map<String,DCMethod> methodMap = new HashMap<String, DCMethod>();

  private JLabel findAllLabel = new JLabel("Find All Resource");
  private JTextField findAllField = new JTextField();
  private JComboBox findAllRequestType = new JComboBox();
  private JLabel getCanonicalLabel = new JLabel("Get Canonical Resource");
  private JTextField getCanonicalField = new JTextField();
  private JComboBox getCanonicalRequestType = new JComboBox();
  private JLabel findLabel = new JLabel("Quick Search Resource");
  private JTextField findField = new JTextField();
  private JComboBox findRequestType = new JComboBox();
  private JLabel createLabel = new JLabel("Create Resource");
  private JTextField createField = new JTextField();
  private JComboBox createRequestType = new JComboBox();
  private JLabel updateLabel = new JLabel("Update Resource");
  private JTextField updateField = new JTextField();
  private JComboBox updateRequestType = new JComboBox();
  private JLabel deleteLabel = new JLabel("Delete Resource");
  private JTextField deleteField = new JTextField();
  private JComboBox deleteRequestType = new JComboBox();
  private JLabel mergeLabel = new JLabel("Merge Resource");
  private JTextField mergeField = new JTextField();
  private JComboBox mergeRequestType = new JComboBox();
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

  public CRUDMethodsRESTPanel()
  {
    // GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty
    //                  , int anchor, int fill, Insets insets, int ipadx, int ipady)
    // Insets(int top, int left, int bottom, int right)
    doilist.addActionListener(this);
    populateRequestTypeLists();
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
    gbc.gridx++;
    contentPanel.add(findAllRequestType, gbc);


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
    gbc.gridx++;
    contentPanel.add(findRequestType, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(getCanonicalLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(getCanonicalField, gbc);
    gbc.weightx = 0;
    gbc.gridx++;
    contentPanel.add(getCanonicalRequestType, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(createLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(createField, gbc);
    gbc.weightx = 0;
    gbc.gridx++;
    contentPanel.add(createRequestType, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(updateLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(updateField, gbc);
    gbc.weightx = 0;
    gbc.gridx++;
    contentPanel.add(updateRequestType, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(mergeLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(mergeField, gbc);
    gbc.weightx = 0;
    gbc.gridx++;
    contentPanel.add(mergeRequestType, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(deleteLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(deleteField, gbc);
    gbc.weightx = 0;
    gbc.gridx++;
    contentPanel.add(deleteRequestType, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(sortLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(sortField, gbc);
    gbc.weightx = 0;
    sortField.setToolTipText("Specify comma-delimited list of attributes to sort on. Add space-separated 'desc' after attribute name to indicate descending order");
    sortField.setEditable(true);
    sortField.setSize(80,1);
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

  private void populateRequestTypeLists()
  {
    List<String> types = new ArrayList<String>();
    types.add("GET");
    types.add("POST");
    types.add("PUT");
    types.add("PATCH");
    types.add("DELETE");

    findAllRequestType.setModel(new DefaultComboBoxModel(types.toArray()));
    getCanonicalRequestType.setModel(new DefaultComboBoxModel(types.toArray()));
    findRequestType.setModel(new DefaultComboBoxModel(types.toArray()));
    createRequestType.setModel(new DefaultComboBoxModel(types.toArray()));
    updateRequestType.setModel(new DefaultComboBoxModel(types.toArray()));
    mergeRequestType.setModel(new DefaultComboBoxModel(types.toArray()));
    deleteRequestType.setModel(new DefaultComboBoxModel(types.toArray()));
  }

  private void populateDataObjectList()
  {
    List<DataObjectInfo> dois = model.getSelectedDataObjects();
    List<String> dataObjectNames = new ArrayList<String>();
    for (DataObjectInfo doi: dois)
    {
      if (doi.getParent()==null)
      {
        dataObjectMap.put(doi.getClassName()+"Service", doi);
        dataObjectNames.add(doi.getClassName()+"Service");        
      }
    }
    doilist.setModel(new DefaultComboBoxModel(dataObjectNames.toArray()));
  }

  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    populateDataObjectList();
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
    String dataObject = (String) doilist.getSelectedItem();
    saveDataObjectProps();
    setCurrentDataObject(dataObjectMap.get(dataObject));
  }

  private DCMethod findOrCreateMethod(JTextField methodField, JComboBox requestType)
  {
    if (methodField.getText()==null || "".equals(methodField.getText().trim()))
    {
      return null;
    }
    String fullUri = methodField.getText();
    String name = fullUri;
    int startQueryString = fullUri.indexOf("?");
    if (startQueryString>0)
    {
      name = fullUri.substring(0,startQueryString);
    }
    String methodKey =  requestType.getSelectedItem()+": "+name;
    DCMethod method = methodMap.get(methodKey);
    if (method==null)
    {
      // no method with this URI created yet
      method = new DCMethod(model.getConnectionName(), fullUri, (String) requestType.getSelectedItem());
      methodMap.put(methodKey, method);
    }
    method.setRequestType((String) requestType.getSelectedItem());
    // We assume that the structure for write methods is same as for findAll method
    // so we set payloadElementName to and payloadRowElementName (might be null in case of JSON)
    // to the names stored on the DataObjectInfo when discovering it
    method.setPayloadElementName(getCurrentDataObject().getPayloadListElementName()); 
    method.setPayloadRowElementName(getCurrentDataObject().getPayloadRowElementName()); 
    return method;
  }

   
  private void saveDataObjectProps()
  {    
    getCurrentDataObject().setFindAllMethod(findOrCreateMethod(findAllField, findAllRequestType));
    getCurrentDataObject().setFindMethod(findOrCreateMethod(findField,findRequestType));
    getCurrentDataObject().setGetCanonicalMethod(findOrCreateMethod(getCanonicalField, getCanonicalRequestType));
    getCurrentDataObject().setCreateMethod(findOrCreateMethod(createField, createRequestType));
    getCurrentDataObject().setUpdateMethod(findOrCreateMethod(updateField, updateRequestType));
    getCurrentDataObject().setMergeMethod(findOrCreateMethod(mergeField, mergeRequestType));
    getCurrentDataObject().setDeleteMethod(findOrCreateMethod(deleteField, deleteRequestType));
    getCurrentDataObject().setDeleteLocalRows(deleteLocalRowsField.isSelected());
    String orderBy = convertToSQLOrderBy(sortField.getText());
    getCurrentDataObject().setSortOrder(sortField.getText());
    getCurrentDataObject().setOrderBy(orderBy);
    getCurrentDataObject().setPayloadDateFormat(dateFormatField.getText());
    getCurrentDataObject().setPayloadDateTimeFormat(dateTimeFormatField.getText());
  }

  public void setCurrentDataObject(DataObjectInfo currentDataObject)
  {
    this.currentDataObject = currentDataObject;
    methodMap.clear();
    DCMethod method = currentDataObject.getFindAllMethod();
    initMethodAndRequestTypeFields(method,findAllField, findAllRequestType,"GET");

    method = currentDataObject.getFindMethod();
    initMethodAndRequestTypeFields(method,findField, findRequestType,"GET");

    method = currentDataObject.getGetCanonicalMethod();
    initMethodAndRequestTypeFields(method,getCanonicalField, getCanonicalRequestType,"GET");

    method = currentDataObject.getCreateMethod();
    initMethodAndRequestTypeFields(method,createField, createRequestType,"PUT");

    method = currentDataObject.getUpdateMethod();
    initMethodAndRequestTypeFields(method,updateField, updateRequestType,"PUT");

    method = currentDataObject.getMergeMethod();
    initMethodAndRequestTypeFields(method,mergeField, mergeRequestType,"PUT");

    method = currentDataObject.getDeleteMethod();
    initMethodAndRequestTypeFields(method,deleteField, deleteRequestType,"DELETE");

    deleteLocalRowsField.setSelected(currentDataObject.isDeleteLocalRows());
    sortField.setText(currentDataObject.getSortOrder());
    dateFormatField.setText(currentDataObject.getPayloadDateFormat());
    dateTimeFormatField.setText(currentDataObject.getPayloadDateTimeFormat());
  }

  private void initMethodAndRequestTypeFields(DCMethod method, JTextField methodField, JComboBox requestType, String requestTypeDefault)
  {
    if (method!=null)
    {
      String methodKey = method.getRequestType()+": "+method.getName();
      if (!methodMap.containsKey(methodKey))
      {
        methodMap.put(methodKey, method);             
      }
      methodField.setText(method.getName());
      if (requestType!=null)
      {
        requestType.setSelectedItem(method.getRequestType());        
      }
    }
    else
    {
      methodField.setText(null);      
      if (requestType!=null)
      {
        requestType.setSelectedItem(requestTypeDefault);
      }
    }
  }

  public DataObjectInfo getCurrentDataObject()
  {
    return currentDataObject;
  }

  private String convertToSQLOrderBy(String sortOrder)
  {
    if (sortOrder==null)
    {
      return null;
    }
    StringBuffer orderBy = new StringBuffer();
    String[] attrs = StringUtils.stringToStringArray(sortOrder, ",");
    boolean firstAttr=true;
    for (int i = 0; i < attrs.length; i++)
    {
      String orderAttr = attrs[i].trim();
      String attrName = orderAttr;
      String ascDesc = null;
      int spacePos = orderAttr.lastIndexOf(" ");
      if (spacePos>-1)
      {
        attrName = orderAttr.substring(0,spacePos);
        ascDesc = orderAttr.substring(spacePos+1);
        ascDesc =ascDesc.trim();
        if (!"desc".equalsIgnoreCase(ascDesc) && !"asc".equalsIgnoreCase(ascDesc))
        {
          continue;
  //          throw new RuntimeException("Sort Order is invalid, you cannot use "+ascDesc);
        }
      }
      AttributeInfo attributeDef = getCurrentDataObject().getAttributeDef(attrName);
      if (attributeDef==null)
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
      if (ascDesc!=null)
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
