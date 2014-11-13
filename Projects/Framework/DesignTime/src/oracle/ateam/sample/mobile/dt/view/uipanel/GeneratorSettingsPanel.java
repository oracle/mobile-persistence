package oracle.ateam.sample.mobile.dt.view.uipanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import oracle.ateam.sample.mobile.dt.model.BusinessObjectGeneratorModel;

import oracle.ateam.sample.mobile.dt.view.wizard.BusinessObjectsFromWSDataControlWizard;

import oracle.ide.panels.DefaultTraversablePanel;
import oracle.ide.panels.TraversableContext;


public class GeneratorSettingsPanel extends DefaultTraversablePanel {
  private JLabel packageLabel = new JLabel("Data Objects Package");
  private JTextField packageField = new JTextField();
  private JLabel servicePackageLabel = new JLabel("Service Objects Package");
  private JTextField servicePackageField = new JTextField();
  private JLabel overwriteDataObjectsLabel = new JLabel("Overwrite Data Object Classes?");
  private JCheckBox overwriteDataObjectsField = new JCheckBox();
  private JLabel overwriteServiceObjectsLabel = new JLabel("Overwrite Service Object Classes?");
  private JCheckBox overwriteServiceObjectsField = new JCheckBox();
  private JLabel usageTrackingLabel = new JLabel("Enable Usage Tracking?");
  private JCheckBox usageTrackingField = new JCheckBox();

    public GeneratorSettingsPanel() {
      // GridBagConstraints(int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty
      //                  , int anchor, int fill, Insets insets, int ipadx, int ipady)
      // Insets(int top, int left, int bottom, int right)

      setLayout( new BorderLayout( 0,15 ) );

            JPanel contentPanel = new JPanel();
            add( contentPanel, BorderLayout.NORTH );
            
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
        contentPanel.add(packageLabel, gbc);
        gbc.gridy++;
        contentPanel.add(servicePackageLabel, gbc);
      gbc.gridy++;
      contentPanel.add(overwriteDataObjectsLabel, gbc);
      gbc.gridy++;
      contentPanel.add(overwriteServiceObjectsLabel, gbc);
        gbc.gridy++;
        contentPanel.add(usageTrackingLabel, gbc);
//        gbc.gridy++;
//        add(_lbAuthor, gbc);

        gbc.gridy = 0;
        gbc.gridx++;
        gbc.weightx = 1.0f;
        gbc.insets = new Insets(0, 0, 5, 0);

        contentPanel.add(packageField, gbc);
        gbc.gridy++;
        contentPanel.add(servicePackageField, gbc);
      gbc.gridy++;
      contentPanel.add(overwriteDataObjectsField, gbc);
      gbc.gridy++;
      contentPanel.add(overwriteServiceObjectsField, gbc);
        gbc.gridy++;
        contentPanel.add(usageTrackingField, gbc);
//        gbc.gridy++;
//        add(_tfAuthor, gbc);
    }

    public void onEntry(TraversableContext tc) {
    BusinessObjectGeneratorModel model =
            (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);

      packageField.setText(model.getPackageName());
      servicePackageField.setText(model.getServicePackageName());
      overwriteDataObjectsField.setSelected(model.isOverwriteDataObjectClasses());
      overwriteServiceObjectsField.setSelected(model.isOverwriteServiceObjectClasses());
      usageTrackingField.setSelected(model.isEnableUsageTracking());
    }

    public void onExit(TraversableContext tc) {
    BusinessObjectGeneratorModel model =
            (BusinessObjectGeneratorModel) tc.get(BusinessObjectsFromWSDataControlWizard.MODEL_KEY);

      model.setPackageName(packageField.getText().trim());
      model.setServicePackageName(servicePackageField.getText().trim());
      model.setOverwriteDataObjectClasses(overwriteDataObjectsField.isSelected());
      model.setOverwriteServiceObjectClasses(overwriteServiceObjectsField.isSelected());
      model.setEnableUsageTracking(usageTrackingField.isSelected());
    }
}
