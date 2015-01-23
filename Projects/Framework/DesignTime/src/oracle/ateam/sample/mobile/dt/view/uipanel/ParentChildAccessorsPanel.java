/*******************************************************************************
 Copyright (c) 2014,2015, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 06-feb-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.dt.view.uipanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import oracle.ateam.sample.mobile.dt.model.AccessorInfo;
import oracle.ateam.sample.mobile.dt.model.AttributeInfo;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.model.DCMethod;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.util.StringUtils;
import oracle.ateam.sample.mobile.dt.view.uimodel.AttributeMappingTableModel;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.bali.ewt.dialog.JEWTDialog;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;
import oracle.ide.panels.TraversalException;

import oracle.javatools.icons.OracleIcons;
import oracle.javatools.ui.table.GenericTable;

public class ParentChildAccessorsPanel
  extends DefaultTraversablePanel
  implements ActionListener, ListSelectionListener, KeyListener
{
  JTextArea instruction =
    new JTextArea("Specify the attributes for parent-child accessor relationships. Clicking the Add button with only a parent attribute selected, will create a new reference attribute in the child data object.");
  JLabel accessorListLabel = new JLabel("Accessor");
  JComboBox accessorList = new JComboBox();
  JList masterAttrs = new JList();
  JList detailAttrs = new JList();
  JLabel parentListLabel = new JLabel("Select Parent Attribute");
  JLabel childListLabel = new JLabel("Select Child Attribute");
  transient Map<String, AccessorInfo> accessorsMap = new HashMap<String, AccessorInfo>();
  transient Map<String, DataObjectInfo> dataObjectMap = new HashMap<String, DataObjectInfo>();
  transient Map<String, DCMethod> methodMap = new HashMap<String, DCMethod>();

  JScrollPane scrollPane =
    new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
  GenericTable table = null;
  private BusinessObjectGeneratorModel model;

  AccessorInfo currentAccessor;
  JButton addButton = new JButton("Add");
  JButton removeButton = new JButton("Remove");
  JEWTDialog accessorDialog = null;
  JButton newAccessorButton = new JButton(OracleIcons.getIcon(OracleIcons.ADD));
  JButton editAccessorButton = new JButton(OracleIcons.getIcon(OracleIcons.EDIT));
  private JLabel parentDataObjectLabel = new JLabel("Parent Data Object");
  private JComboBox parentDataObjectField = new JComboBox();
  private JLabel childDataObjectLabel = new JLabel("Child Data Object");
  private JComboBox childDataObjectField = new JComboBox();
  private JLabel childDataRetrievalLabel = new JLabel("Child Data Retrieval");
  private JRadioButton radioInParentPayload = new JRadioButton("Included in Parent Payload");
  private JRadioButton radioWSCall = new JRadioButton("Web service call",true);
  private JLabel childAccessorMethodLabel = new JLabel("Child Accessor Method");
  private JComboBox childAccessorMethodField = new JComboBox();
  private JTextField childAccessorMethodFieldRest = new JTextField();
  private JComboBox childAccessorMethodRequestType = new JComboBox();
  private JLabel childAccessorPayloadNameLabel = new JLabel("Child Accessor Payload Attribute");
  private JTextField childAccessorPayloadNameField = new JTextField();
  private JLabel childAccessorNameLabel = new JLabel("Child Accessor Attribute");
  private JTextField childAccessorNameField = new JTextField();
  private JLabel parentAccessorMethodLabel = new JLabel("Parent Accessor Method");
  private JComboBox parentAccessorMethodField = new JComboBox();
  private JTextField parentAccessorMethodFieldRest = new JTextField();
  private JComboBox parentAccessorMethodRequestType = new JComboBox();
  private JLabel parentAccessorNameLabel = new JLabel("Parent Accessor Attribute");
  private JTextField parentAccessorNameField = new JTextField();
  // mode to indicate whether we are creating a new accessor or editing an existing one
  private boolean accessorDialogEditMode = false;

  public ParentChildAccessorsPanel()
  {
    super();
    accessorList.addActionListener(this);
    parentDataObjectField.addActionListener(this);
    childDataObjectField.addActionListener(this);
    childAccessorMethodField.addActionListener(this);
    childAccessorMethodFieldRest.addKeyListener(this);
    childAccessorNameField.addKeyListener(this);
    childAccessorPayloadNameField.addKeyListener(this);
    parentAccessorMethodField.addActionListener(this);
    parentAccessorMethodFieldRest.addKeyListener(this);
    parentAccessorNameField.addKeyListener(this);
    setLayout(new GridBagLayout());

    JPanel cardPanel = new JPanel();
    cardPanel.setLayout(new GridBagLayout());
    cardPanel.add(accessorListLabel,
                  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                                         new Insets(0, 5, 0, 0), 0, 0));
    cardPanel.add(accessorList,
                  new GridBagConstraints(1, 2, 4, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                         new Insets(0, 5, 5, 0), 0, 0));
    cardPanel.add(newAccessorButton,
                  new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                         new Insets(0, 5, 5, 0), 0, 0));
    newAccessorButton.setToolTipText("New Parent-Child Accessor");      

    cardPanel.add(editAccessorButton,
                  new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                         new Insets(0, 5, 5, 0), 0, 0));
    editAccessorButton.setToolTipText("Edit Parent-Child Accessor");      

    instruction.setLineWrap(true);
    instruction.setWrapStyleWord(true);
    instruction.setEditable(false);
    instruction.setBorder(null);
    this.add(instruction,
             new GridBagConstraints(0, 0, 6, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 0, 0, 0), 0, 0));

    this.add(cardPanel,
             new GridBagConstraints(0, 1, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
                                                                                                                       0,
                                                                                                                       0,
                                                                                                                       0),
                                    0, 0));

    this.add(parentListLabel,
             new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                                    new Insets(5, 5, 0, 0), 0, 0));
    this.add(childListLabel,
             new GridBagConstraints(4, 2, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                                    new Insets(5, 5, 0, 0), 0, 0));

    masterAttrs.addListSelectionListener(this);
    JScrollPane parentListScroller = new JScrollPane(masterAttrs);
    this.add(parentListScroller,
             new GridBagConstraints(0, 3, 2, 1, 1.0, .45, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                    new Insets(0, 5, 0, 5), 0, 0));
    detailAttrs.addListSelectionListener(this);
    JScrollPane childListScroller = new JScrollPane(detailAttrs);
    this.add(childListScroller,
             new GridBagConstraints(4, 3, 2, 1, 1.0, 1.00, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                    new Insets(0, 5, 0, 5), 0, 0));

    JPanel attrPanel = new JPanel();
    attrPanel.setLayout(new GridBagLayout());

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridBagLayout());
    addButton.addActionListener(this);
    addButton.setEnabled(false);
    buttonPanel.add(addButton,
                    new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                           new Insets(0, 5, 0, 0), 0, 0));
    removeButton.addActionListener(this);
    removeButton.setEnabled(false);
    buttonPanel.add(removeButton,
                    new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                           new Insets(0, 5, 0, 0), 0, 0));

    attrPanel.add(buttonPanel,
                  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                         new Insets(5, 5, 0, 5), 0, 0));
    attrPanel.add(scrollPane,
                  new GridBagConstraints(0, 4, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                                         new Insets(3, 5, 0, 5), 0, 0));

    this.add(attrPanel,
             new GridBagConstraints(0, 6, 6, 1, 1.0, .35, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5,
                                                                                                                       5,
                                                                                                                       0,
                                                                                                                       0),
                                    0, 0));
  }

  private void buildAccessorDialog(final BusinessObjectGeneratorModel model)
  {
    JPanel dialogPanel = new JPanel();
    accessorDialog.setContent(dialogPanel);
    int width = model.isWebServiceDataControl() ? 350 : 500;
    accessorDialog.setPreferredSize(width, 320);
    accessorDialog.setResizable(true);
    accessorDialog.setModal(true);
    accessorDialog.setButtonMask((JEWTDialog.BUTTON_OK | JEWTDialog.BUTTON_CANCEL));
    accessorDialog.setDefaultButton(JEWTDialog.BUTTON_OK);
    newAccessorButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        accessorDialog.setTitle("New Parent-Child Accessor");
        resetAccessorDialogFields();
        enableOrDisableAccessorDialogOKButton();
        boolean OK = accessorDialog.runDialog();
        if (OK)
        {
          DataObjectInfo parent = dataObjectMap.get(parentDataObjectField.getSelectedItem());
          DataObjectInfo child = dataObjectMap.get(childDataObjectField.getSelectedItem());
          AccessorInfo accessorInfo = new AccessorInfo(parent, child,false);
          saveAccessor(accessorInfo);
        }
      }

    });

    editAccessorButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {         
        accessorDialog.setTitle("Edit Parent-Child Accessor");
        initAccessorDialogFields(getCurrentAccessor());
        enableOrDisableAccessorDialogOKButton();
        boolean OK = accessorDialog.runDialog();
        if (OK)
        {
          saveAccessor(getCurrentAccessor());
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
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    gbc.insets = new Insets(0, 10, 5, 5);
    dialogPanel.add(parentDataObjectLabel, gbc);
    gbc.gridy++;
    dialogPanel.add(childDataObjectLabel, gbc);
    gbc.gridy++;
    dialogPanel.add(childDataRetrievalLabel, gbc);
    gbc.gridy++;
    gbc.gridy++;
    dialogPanel.add(childAccessorMethodLabel, gbc);
    gbc.gridy++;
    dialogPanel.add(childAccessorPayloadNameLabel, gbc);
    gbc.gridy++;
    dialogPanel.add(childAccessorNameLabel, gbc);
    gbc.gridy++;
    dialogPanel.add(parentAccessorMethodLabel, gbc);
    gbc.gridy++;
    dialogPanel.add(parentAccessorNameLabel, gbc);
    if (!model.isWebServiceDataControl())
    {
      childAccessorMethodLabel.setText("Child Accessor Resource");
      parentAccessorMethodLabel.setText("Parent Accessor Resource");
    }  

    gbc.gridy = 0;
    gbc.gridx = 1;
    gbc.weightx = 1.0f;
    gbc.insets = new Insets(0, 0, 5, 10);

    dialogPanel.add(parentDataObjectField, gbc);
    gbc.gridy++;
    dialogPanel.add(childDataObjectField, gbc);
    gbc.gridy++;

    ButtonGroup radioGroup = new ButtonGroup();
    radioGroup.add(radioWSCall);
    radioGroup.add(radioInParentPayload);
    dialogPanel.add(radioWSCall, gbc);      
    gbc.gridy++;
    dialogPanel.add(radioInParentPayload, gbc);      

    radioWSCall.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent e) {         
               // 1 = checked
               if (e.getStateChange()==1)
               {
                 childAccessorMethodField.setEnabled(true);
                 childAccessorMethodFieldRest.setEnabled(true);
                 childAccessorPayloadNameField.setEnabled(false);
                 childAccessorPayloadNameField.setText(null);
               }
               else
               {
                 childAccessorMethodField.setEnabled(false);
                 childAccessorMethodField.setSelectedIndex(-1);
                 childAccessorMethodFieldRest.setEnabled(false);
                 childAccessorMethodFieldRest.setText(null);
                 childAccessorPayloadNameField.setEnabled(true);
               }
             }           
          });

    gbc.gridx = 1;
    gbc.gridy++;
    if (model.isWebServiceDataControl())
    {
      dialogPanel.add(childAccessorMethodField, gbc);      
      gbc.gridy++;
      dialogPanel.add(childAccessorPayloadNameField, gbc);      
      gbc.gridy++;
      dialogPanel.add(childAccessorNameField, gbc);      
      gbc.gridy++;
      dialogPanel.add(parentAccessorMethodField, gbc);      
      gbc.gridy++;
      dialogPanel.add(parentAccessorNameField, gbc);      
    }
    else
    {
      List<String> types = new ArrayList<String>();
      types.add("GET");
      types.add("POST");
      types.add("PUT");
      types.add("PATCH");
      types.add("DELETE");

      dialogPanel.add(childAccessorMethodFieldRest, gbc);      
      gbc.gridx++;
      gbc.weightx = 0;
      childAccessorMethodRequestType.setModel(new DefaultComboBoxModel(types.toArray()));
      childAccessorMethodRequestType.setSelectedItem("GET");
      dialogPanel.add(childAccessorMethodRequestType, gbc);            

      gbc.gridx = 1;
      gbc.weightx = 1.0f;
      gbc.gridy++;
      dialogPanel.add(childAccessorPayloadNameField, gbc);            
      gbc.gridy++;
      dialogPanel.add(childAccessorNameField, gbc);            

      gbc.gridy++;      
      dialogPanel.add(parentAccessorMethodFieldRest, gbc);      
      gbc.gridx++;
      gbc.weightx = 0;
      parentAccessorMethodRequestType.setModel(new DefaultComboBoxModel(types.toArray()));
      parentAccessorMethodRequestType.setSelectedItem("GET");
      dialogPanel.add(parentAccessorMethodRequestType, gbc);            

      gbc.gridx = 1;
      gbc.weightx = 1.0f;
      gbc.gridy++;
      dialogPanel.add(parentAccessorNameField, gbc);            
    }
  }

  /**
   * This method is called for both creating a new accessor as editing an existing one
   * @param accessorInfo
   */
  private void saveAccessor(AccessorInfo accessorInfo)
  {
    
    // if child accessor method/resource AND child accessor attribute is set : add as child accessor
    // if parent accessor method/resource AND parent accessor attribute is set: add as parent accessor
    // if both are set, add both parent and child accessor
    DataObjectInfo parent = accessorInfo.getParentDataObject();
    DataObjectInfo child = accessorInfo.getChildDataObject();
    DCMethod childAccessorMethod = accessorInfo.getChildAccessorMethod();
    DCMethod parentAccessorMethod = accessorInfo.getParentAccessorMethod();
    if (model.isWebServiceDataControl())
    {
      childAccessorMethod = methodMap.get(childAccessorMethodField.getSelectedItem());
      parentAccessorMethod = methodMap.get(parentAccessorMethodField.getSelectedItem());
    }
    else 
    {
      if (StringUtils.isNotEmpty(childAccessorMethodFieldRest.getText()) && StringUtils.isNotEmpty(childAccessorNameField.getText()))
      {
        if (childAccessorMethod==null)
        {
          String suffix = model.isMaf20Style() ? "List" : "";
          childAccessorMethod = new DCMethod(childAccessorNameField.getText()+suffix,model.getConnectionName(), childAccessorMethodFieldRest.getText(), (String) childAccessorMethodRequestType.getSelectedItem());                      
        }
        childAccessorMethod.setParameterValueProviderDataObject(parent);
        // we assume payload structure is same as one used to discover the data objects
        // We ONLY do this for new methods, we should preserve the values manually set in persistence-mapping
        if (!childAccessorMethod.isExisting())
        {
          childAccessorMethod.setPayloadElementName(child.getPayloadListElementName());
          childAccessorMethod.setPayloadRowElementName(child.getPayloadRowElementName());           
        }
      }
      if (StringUtils.isNotEmpty(parentAccessorMethodFieldRest.getText()) && StringUtils.isNotEmpty(parentAccessorNameField.getText()))
      {
        if (parentAccessorMethod==null)
        {
          parentAccessorMethod = new DCMethod(parentAccessorNameField.getText(),model.getConnectionName(), parentAccessorMethodFieldRest.getText(), (String) parentAccessorMethodRequestType.getSelectedItem());                      
        }
        parentAccessorMethod.setParameterValueProviderDataObject(child);
        // we assume payload structure is same as one used to discover dthe data objects
        // We ONLY do this for new methods, we should preserve the values manually set in persistence-mapping
        if (!parentAccessorMethod.isExisting())
        {
          parentAccessorMethod.setPayloadElementName(parent.getPayloadListElementName());
          parentAccessorMethod.setPayloadRowElementName(parent.getPayloadRowElementName()); 
        }
      }
    }
    if (childAccessorMethod!=null || StringUtils.isNotEmpty(childAccessorPayloadNameField.getText()))
    {  
      child.addFindAllInParentMethod(childAccessorMethod);            
      accessorInfo.setChildAccessorMethod(childAccessorMethod);
      accessorInfo.setChildAccessorPayloadName(StringUtils.convertEmptyStringToNull(childAccessorPayloadNameField.getText()));  
      accessorInfo.setChildAccessorName(childAccessorNameField.getText());            
      // add as new child if this accessor instance is not already added
      // note that we cant just pass in edit or new mode to this method to determine whether we
      // need to add a child accessor, because even
      // in edit mode we might need to add a child when previously only a parent accessor was
      // defined for this parent-child relationship
      boolean alreadyAdded = false;
      for(AccessorInfo acc : parent.getAllChildren())
      {
        if (acc==accessorInfo)
        {
          alreadyAdded = true;
          break;
        }
      }
      if (!alreadyAdded)
      {
        parent.addChild(accessorInfo);        
      }
    } 
    if (parentAccessorMethod!=null)
    {
      parent.addGetAsParentMethod(parentAccessorMethod);            
      accessorInfo.setParentAccessorMethod(parentAccessorMethod);
      accessorInfo.setParentAccessorName(parentAccessorNameField.getText());
      // add as new parent if this accessor instance is not already added
      // note that we cant just pass in edit or new mode to this method to determine whether we
      // need to add a parent accessor, because even
      // in edit mode we might need to add a parent when previously only a child accessor was
      // defined for this parent-child relationship
      boolean alreadyAdded = false;
      for(AccessorInfo acc : child.getAllParents())
      {
        if (acc==accessorInfo)
        {
          alreadyAdded = true;
          break;
        }
      }
      if (!alreadyAdded)
      {
        child.addParent(accessorInfo);
      }
    }
    populateAccessorList();
    accessorList.setSelectedItem(accessorInfo.getAccessorDisplayName());
    setCurrentAccessor(accessorInfo);
  }

  private void resetAccessorDialogFields()
  {
    parentDataObjectField.setSelectedIndex(-1);
    childDataObjectField.setSelectedIndex(-1);
    childAccessorMethodField.setSelectedIndex(-1);
    childAccessorMethodFieldRest.setText(null);
    childAccessorPayloadNameField.setText(null);
    childAccessorPayloadNameField.setEnabled(false);
    childAccessorNameField.setText(null);
    parentAccessorMethodField.setSelectedIndex(-1);
    parentAccessorMethodFieldRest.setText(null);
    parentAccessorNameField.setText(null);
    radioWSCall.setSelected(true);
  }

  private void initAccessorDialogFields(AccessorInfo accessorInfo)
  {
    parentDataObjectField.setSelectedItem(accessorInfo.getParentDataObject().getClassName());
    childDataObjectField.setSelectedItem(accessorInfo.getChildDataObject().getClassName());
    if (model.isWebServiceDataControl())
    {      
      String cmethod = accessorInfo.getChildAccessorMethod()!=null ? accessorInfo.getChildAccessorMethod().getName() : null;
      radioWSCall.setSelected(cmethod!=null);
      radioInParentPayload.setSelected(cmethod==null);
      childAccessorMethodField.setSelectedItem(cmethod);
      String pmethod = accessorInfo.getParentAccessorMethod()!=null ? accessorInfo.getParentAccessorMethod().getName() : null;
      parentAccessorMethodField.setSelectedItem(pmethod);      
    }
    else
    {
      String cmethod = accessorInfo.getChildAccessorMethod()!=null ? accessorInfo.getChildAccessorMethod().getName() : null;
      radioWSCall.setSelected(cmethod!=null);
      radioInParentPayload.setSelected(cmethod==null);
      childAccessorMethodFieldRest.setText(cmethod);
      String pmethod = accessorInfo.getParentAccessorMethod()!=null ? accessorInfo.getParentAccessorMethod().getName() : null;
      parentAccessorMethodFieldRest.setText(pmethod);      
    }
    childAccessorPayloadNameField.setText(accessorInfo.getChildAccessorPayloadName());
    childAccessorNameField.setText(accessorInfo.getChildAccessorName());
    parentAccessorNameField.setText(accessorInfo.getParentAccessorName());
  }

  private void populateDataObjectList()
  {
    List<DataObjectInfo> dois = model.getSelectedDataObjects();
    List<String> dataObjectNames = new ArrayList<String>();
    for (DataObjectInfo doi: dois)
    {
      dataObjectMap.put(doi.getClassName(), doi);
      dataObjectNames.add(doi.getClassName());
    }
    parentDataObjectField.setModel(new DefaultComboBoxModel(dataObjectNames.toArray()));
    childDataObjectField.setModel(new DefaultComboBoxModel(dataObjectNames.toArray()));
  }

  private void populateMethodLists()
  {
    Map<String, DCMethod> methods = model.getDataControlVisitor().getCRUDMethods();

    List<String> methodNames = new ArrayList<String>();
    methodNames.add("");
    Iterator<String> iterator = methods.keySet().iterator();
    while (iterator.hasNext())
    {
      String methodName = iterator.next();
      methodNames.add(methodName);
      methodMap.put(methodName, methods.get(methodName));
    }
    childAccessorMethodField.setModel(new DefaultComboBoxModel(methodNames.toArray()));
    parentAccessorMethodField.setModel(new DefaultComboBoxModel(methodNames.toArray()));
  }

  private void populateAccessorList()
  {
    List<DataObjectInfo> dois = model.getSelectedDataObjects();
    accessorsMap.clear();
    List<String> accessorNames = new ArrayList<String>();
    for (DataObjectInfo doi: dois)
    {
      List<AccessorInfo> children = doi.getAllChildren();
      for (AccessorInfo child: children)
      {
        if (child.getChildDataObject().isGenerate())
        {
          String accessorPath = child.getAccessorDisplayName();
          accessorsMap.put(accessorPath, child);
          accessorNames.add(accessorPath);          
        }
      }
    }
    for (DataObjectInfo doi: dois)
    {
      List<AccessorInfo> parents = doi.getAllParents();
      for (AccessorInfo parent: parents)
      {
        if (parent.getChildDataObject().isGenerate())
        {
          String accessorPath = parent.getAccessorDisplayName();
          if (!(accessorsMap.containsKey(accessorPath)))
          {
            accessorsMap.put(accessorPath, parent);
            accessorNames.add(accessorPath);                      
          }
        }
      }
    }
    accessorList.setModel(new DefaultComboBoxModel(accessorNames.toArray()));
  }


  public void onEntry(TraversableContext tc)
  {
    super.onEntry(tc);
    model = (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    if (accessorDialog==null)
    {
      accessorDialog = JEWTDialog.createDialog(this, "New Parent-Child Accessor", JEWTDialog.BUTTON_DEFAULT);
      buildAccessorDialog(model);
    }
    populateAccessorList();
    populateDataObjectList();
    if (model.isWebServiceDataControl())
    {
      populateMethodLists();      
    }    
    //    if (model.getDataControlName() != null)
    //    {
    //      doilist.setSelectedItem(model.getDataControlName());
    //    }
    if (accessorList.getItemCount() > 0)
    {
      accessorList.setSelectedItem(accessorList.getItemAt(0));
      setCurrentAccessor(accessorsMap.get(accessorList.getSelectedItem()));
    }
    // enable back -  next - finish
    tc.getWizardCallbacks().wizardEnableButtons(true, true, false);
  }

  public void onExit(TraversableContext tc)
    throws TraversalException
  {
    BusinessObjectGeneratorModel model =
      (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);
    super.onExit(tc);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == addButton)
    {
      addAttributeMapping();
    }
    else if (e.getSource() == removeButton)
    {
      removeAttributeMapping();
    }
    else if (e.getSource() == accessorList)
    {
      setCurrentAccessor(accessorsMap.get(accessorList.getSelectedItem()));
    }
    else if (e.getSource() == parentDataObjectField || e.getSource() == childDataObjectField)
    {
      // default accessor method to findAll methodof child DO if set
      if ( e.getSource() == childDataObjectField )
      {
        DataObjectInfo child = dataObjectMap.get(childDataObjectField.getSelectedItem());
        if (child!=null && child.getFindAllMethod()!=null)
        {
          childAccessorMethodFieldRest.setText(child.getFindAllMethod().getFullUri());
        }
      }
    } 
    enableOrDisableAccessorDialogOKButton();
  }

  private void enableOrDisableAccessorDialogOKButton()
  {
    boolean childAccessorOK = false;
    boolean parentAccessorOK = false;
    if (model.isWebServiceDataControl())
    {
      if (radioWSCall.isSelected())
      {
        childAccessorOK = childAccessorMethodField.getSelectedItem() != null && StringUtils.isNotEmpty(childAccessorNameField.getText());      
      }
      else
      {
        childAccessorOK = StringUtils.isNotEmpty(childAccessorPayloadNameField.getText()) && StringUtils.isNotEmpty(childAccessorNameField.getText());              
      }
      parentAccessorOK = parentAccessorMethodField.getSelectedItem() != null && StringUtils.isNotEmpty(parentAccessorNameField.getText());      
    }
    else 
    {
      if (radioWSCall.isSelected())
      {
        childAccessorOK = StringUtils.isNotEmpty(childAccessorMethodFieldRest.getText()) && StringUtils.isNotEmpty(childAccessorNameField.getText());      
      }
      else
      {
        childAccessorOK = StringUtils.isNotEmpty(childAccessorPayloadNameField.getText()) && StringUtils.isNotEmpty(childAccessorNameField.getText());              
      }
      parentAccessorOK = StringUtils.isNotEmpty(parentAccessorMethodFieldRest.getText()) && StringUtils.isNotEmpty(parentAccessorNameField.getText());      
    }    
    accessorDialog.setOKButtonEnabled(parentAccessorOK || childAccessorOK);
  }

  private void removeAttributeMapping()
  {
    int row = table.getSelectedRow();
    if (row > -1)
    {
      getCurrentAccessor().getAttributeMappings().remove(row);
      buildSourceAndDestLists();
    }
    removeButton.setEnabled(false);
  }

  private void addAttributeMapping()
  {
    String masterAttr = (String) masterAttrs.getSelectedValue();
    String detailAttr = (String) detailAttrs.getSelectedValue();
    AttributeInfo parentAttr = getCurrentAccessor().getParentDataObject().getAttributeDef(masterAttr);
    AttributeInfo childAttr = null;
    if (detailAttr == null)
    {
      // create reference attribute in child data object
      String attrName =
        getCurrentAccessor().getParentDataObject().getClassName() + StringUtils.initCap(masterAttr);
      childAttr = new AttributeInfo(attrName, getCurrentAccessor().getParentDataObject(), parentAttr);
      DataObjectInfo childDataObject = getCurrentAccessor().getChildDataObject();
      childDataObject.addAttribute(childAttr);
    }
    else
    {
      childAttr = getCurrentAccessor().getChildDataObject().getAttributeDef(detailAttr);
    }
    getCurrentAccessor().addAttributeMapping(parentAttr, childAttr);
    buildSourceAndDestLists();
  }

  private void buildSourceAndDestLists()
  {
    DataObjectInfo masterDoi = getCurrentAccessor().getParentDataObject();
    masterAttrs.setModel(new DefaultComboBoxModel(masterDoi.getAttributeNames().toArray()));
    DataObjectInfo detailDoi = getCurrentAccessor().getChildDataObject();
    detailAttrs.setModel(new DefaultComboBoxModel(detailDoi.getAttributeNames().toArray()));

    table = new GenericTable(new AttributeMappingTableModel(getCurrentAccessor()));
    table.setColumnSelectorAvailable(false);

    //To stop cell editing when user switches to another component without using tab/enter keys
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    //      TableCellRenderer checkBoxRenderer = new TableCellCheckBoxRenderer();
    //      TableCellCheckBoxEditor checkBoxEditor = new TableCellCheckBoxEditor();
    table.getSelectionModel().addListSelectionListener(this);
    scrollPane.getViewport().setView(table);
  }

  @Override
  public void valueChanged(ListSelectionEvent e)
  {
    //    boolean addEnable = masterAttrs.getSelectedValue()!=null && detailAttrs.getSelectedValue()!=null;
    boolean addEnable = masterAttrs.getSelectedValue() != null;
    addButton.setEnabled(addEnable);
    boolean removeEnable = table.getSelectedRow() > -1;
    removeButton.setEnabled(removeEnable);
  }

  public void setCurrentAccessor(AccessorInfo currentChildAccessor)
  {
    this.currentAccessor = currentChildAccessor;
    buildSourceAndDestLists();
  }

  public AccessorInfo getCurrentAccessor()
  {
    return currentAccessor;
  }

  @Override
  public void keyTyped(KeyEvent e)
  {
    enableOrDisableAccessorDialogOKButton();
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    // TODO Implement this method
  }

  @Override
  public void keyReleased(KeyEvent e)
  {
    // TODO Implement this method
  }
}
