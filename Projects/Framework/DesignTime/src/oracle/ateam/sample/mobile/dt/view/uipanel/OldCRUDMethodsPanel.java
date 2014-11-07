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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import oracle.ateam.sample.mobile.dt.model.DataObjectInfo;
import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;

import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;


public class OldCRUDMethodsPanel
  extends DefaultTraversablePanel
  implements ActionListener
{

  private BusinessObjectGeneratorModel model;

  JLabel instruction = new JLabel("Select the web service CRUD methods you want to invoke from your service object");
  JLabel doiLabel = new JLabel("Service Object");
  JComboBox doilist = new JComboBox();
  transient Map<String, DataObjectInfo> dataObjectMap = new HashMap<String, DataObjectInfo>();

  private JLabel findMethodLabel = new JLabel("FindAll Method");
  private JComboBox findMethodField = new JComboBox();
  private JLabel createMethodLabel = new JLabel("Create Method");
  private JComboBox createMethodField = new JComboBox();
  private JLabel updateMethodLabel = new JLabel("Update Method");
  private JComboBox updateMethodField = new JComboBox();
  private JLabel mergeMethodLabel = new JLabel("Merge Method");
  private JComboBox mergeMethodField = new JComboBox();
  private JLabel deleteMethodLabel = new JLabel("Delete Method");
  private JComboBox deleteMethodField = new JComboBox();

  private DataObjectInfo currentDataObject;

  public OldCRUDMethodsPanel()
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

    gbc.insets = new Insets(0, 0, 5, 5);
    contentPanel.add(doiLabel, gbc);
    gbc.gridx++;
    contentPanel.add(doilist, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(findMethodLabel, gbc);
    gbc.gridx++;
    contentPanel.add(findMethodField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(createMethodLabel, gbc);
    gbc.gridx++;
    contentPanel.add(createMethodField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(updateMethodLabel, gbc);
    gbc.gridx++;
    contentPanel.add(updateMethodField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(mergeMethodLabel, gbc);
    gbc.gridx++;
    contentPanel.add(mergeMethodField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    contentPanel.add(deleteMethodLabel, gbc);
    gbc.gridx++;
    contentPanel.add(deleteMethodField, gbc);

    //        gbc.weightx = 1.0f;
    //        gbc.insets = new Insets(0, 0, 5, 0);

  }

  private void populateDataObjectList()
  {
    List<DataObjectInfo> dois = model.getSelectedDataObjects();
    List<String> dataObjectNames = new ArrayList<String>();
    for (DataObjectInfo doi: dois)
    {
      if (doi.getParent() == null)
      {
        dataObjectMap.put(doi.getClassName() + "Service", doi);
        dataObjectNames.add(doi.getClassName()+ "Service");
      }
    }
    doilist.setModel(new DefaultComboBoxModel(dataObjectNames.toArray()));
  }

  private void populateCRUDMethodLists()
  {
    List<DataObjectInfo> dois = model.getDataControlVisitor().getMethodAccessorBeans();
    List<String> methodNames = new ArrayList<String>();
    for (DataObjectInfo doi: dois)
    {
      methodNames.add(doi.getName());
    }
    findMethodField.setModel(new DefaultComboBoxModel(methodNames.toArray()));
    createMethodField.setModel(new DefaultComboBoxModel(methodNames.toArray()));
    updateMethodField.setModel(new DefaultComboBoxModel(methodNames.toArray()));
    mergeMethodField.setModel(new DefaultComboBoxModel(methodNames.toArray()));
    deleteMethodField.setModel(new DefaultComboBoxModel(methodNames.toArray()));
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
    saveCRUDMethods();
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    String serviceName = (String) doilist.getSelectedItem();
    saveCRUDMethods();
    setCurrentDataObject(dataObjectMap.get(serviceName));
  }

  private void saveCRUDMethods()
  {
 //   getCurrentDataObject().setFindMethod((String) findMethodField.getSelectedItem());
  }

  public void setCurrentDataObject(DataObjectInfo currentDataObject)
  {
    this.currentDataObject = currentDataObject;
    findMethodField.setSelectedItem(currentDataObject.getFindMethod());
    populateCRUDMethodLists();
  }

  public DataObjectInfo getCurrentDataObject()
  {
    return currentDataObject;
  }
}
