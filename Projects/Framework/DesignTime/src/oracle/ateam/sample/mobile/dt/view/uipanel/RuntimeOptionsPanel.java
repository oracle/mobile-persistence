/*******************************************************************************
 Copyright (c) 2014-2016, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 31-jan-2016   Steven Davelaar
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

import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;
import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;


public class RuntimeOptionsPanel
  extends DefaultTraversablePanel
  implements ActionListener
{

  private BusinessObjectGeneratorModel model;

  JLabel instruction = new JLabel("Set runtime options for each data object");
  JLabel doiLabel = new JLabel("Data Object");
  JComboBox doilist = new JComboBox();
  transient Map<String,DataObjectInfo> dataObjectMap = new HashMap<String, DataObjectInfo>();

  private JLabel remoteReadLabel = new JLabel("Remote Read in Background");
  private JCheckBox remoteReadField = new JCheckBox();
  private JLabel remoteWriteLabel = new JLabel("Remote Write in Background");
  private JCheckBox remoteWriteField = new JCheckBox();
  private JLabel autoQueryLabel = new JLabel("Auto Query");
  private JCheckBox autoQueryField = new JCheckBox();
  private JLabel generatePkLabel = new JLabel("Generate Primary Key");
  private JCheckBox generatePkField = new JCheckBox();
  private JLabel offlineTransactionsLabel = new JLabel("Enable Offline Transactions");
  private JCheckBox offlineTransactionsField = new JCheckBox();
  private JLabel showWsErrorsLabel = new JLabel("Show Web Service Errors");
  private JCheckBox showWsErrorsField = new JCheckBox();
  private JLabel deleteLocalRowsLabel = new JLabel("Delete Local Rows on Find All?");
  private JCheckBox deleteLocalRowsField = new JCheckBox();
  JLabel sortLabel = new JLabel("Sort Order");
  JTextField sortField = new JTextField();

  private JLabel remotePmLabel = new JLabel("Remote Persistence Manager");
  private JTextField remotePmField = new JTextField();
  private JLabel localPmLabel = new JLabel("Local Persistence Manager");
  private JTextField localPmField = new JTextField();

  private DataObjectInfo currentDataObject;

  public RuntimeOptionsPanel()
  {
    // GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty
    //                  , int anchor, int fill, Insets insets, int ipadx, int ipady)
    // Insets(int top, int left, int bottom, int right)
    doilist.addActionListener(this);
    
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

    gbc.weightx = 0;
    gbc.insets = new Insets(0, 0, 20, 5);
    contentPanel.add(doiLabel, gbc);
    gbc.gridx++;
    contentPanel.add(doilist, gbc);

    gbc.insets = new Insets(0, 0, 5, 5);
    gbc.gridy++;
    gbc.weightx = 0;
    gbc.gridx = 0;
    contentPanel.add(remoteReadLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(remoteReadField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0;
    contentPanel.add(remoteWriteLabel, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(remoteWriteField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0;
    contentPanel.add(deleteLocalRowsLabel, gbc); 
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(deleteLocalRowsField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0;
    contentPanel.add(autoQueryLabel, gbc); 
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(autoQueryField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0;
    contentPanel.add(generatePkLabel, gbc); 
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(generatePkField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0;
    contentPanel.add(offlineTransactionsLabel, gbc); 
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(offlineTransactionsField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0;
    contentPanel.add(showWsErrorsLabel, gbc); 
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(showWsErrorsField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0;
    contentPanel.add(remotePmLabel, gbc); 
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(remotePmField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0;
    contentPanel.add(localPmLabel, gbc); 
    gbc.gridx++;
    gbc.weightx = 1.0f;
    contentPanel.add(localPmField, gbc);

  }

  private void populateDataObjectList()
  {
    List<DataObjectInfo> dois = model.getSelectedDataObjects();
    List<String> dataObjectNames = new ArrayList<String>();
    for (DataObjectInfo doi: dois)
    {
// show only data objects where  service class for is generated
      if (doi.isGenerateServiceClass())
      {
        dataObjectMap.put(doi.getClassName(), doi);
        dataObjectNames.add(doi.getClassName());        
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


   
  private void saveDataObjectProps()
  {    
    getCurrentDataObject().setRemoteReadInBackground(remoteReadField.isSelected());
    getCurrentDataObject().setRemoteWriteInBackground(remoteWriteField.isSelected());
    getCurrentDataObject().setDeleteLocalRows(deleteLocalRowsField.isSelected());
    getCurrentDataObject().setAutoQuery(autoQueryField.isSelected());
    getCurrentDataObject().setGeneratePrimaryKey(generatePkField.isSelected());
    getCurrentDataObject().setEnableOfflineTransactions(offlineTransactionsField.isSelected());
    getCurrentDataObject().setShowWebServiceErrors(showWsErrorsField.isSelected());
    getCurrentDataObject().setRemotePersistenceManager(remotePmField.getText());
    getCurrentDataObject().setLocalPersistenceManager(localPmField.getText());
  }

  public void setCurrentDataObject(DataObjectInfo currentDataObject)
  {
    this.currentDataObject = currentDataObject;
    remoteReadField.setSelected(currentDataObject.isRemoteReadInBackground());
    remoteWriteField.setSelected(currentDataObject.isRemoteWriteInBackground());
    deleteLocalRowsField.setSelected(currentDataObject.isDeleteLocalRows());
    autoQueryField.setSelected(currentDataObject.isAutoQuery());
    generatePkField.setSelected(currentDataObject.isGeneratePrimaryKey());
    offlineTransactionsField.setSelected(currentDataObject.isEnableOfflineTransactions());
    showWsErrorsField.setSelected(currentDataObject.isShowWebServiceErrors());

    if (currentDataObject.getRemotePersistenceManager()==null)
    {
      // new Data object, need to default persistence managers
      String remotePersistenceManager =  currentDataObject.isXmlPayload() ? "RestXMLPersistenceManager" : 
        ( model.isUseMCS() ? "MCSPersistenceManager" : "RestJSONPersistenceManager");
      currentDataObject.setLocalPersistenceManager("oracle.ateam.sample.mobile.v2.persistence.manager.DBPersistenceManager");
      currentDataObject.setRemotePersistenceManager("oracle.ateam.sample.mobile.v2.persistence.manager." +
                                          remotePersistenceManager);
    }
    remotePmField.setText(currentDataObject.getRemotePersistenceManager());
    localPmField.setText(currentDataObject.getLocalPersistenceManager());
  }

  public DataObjectInfo getCurrentDataObject()
  {
    return currentDataObject;
  }

}
