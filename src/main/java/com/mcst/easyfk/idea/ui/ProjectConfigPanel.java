package com.mcst.easyfk.idea.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.mcst.easyfk.generator.enums.*;
import com.mcst.easyfk.generator.properties.ProjectProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class ProjectConfigPanel {

    private final JPanel mainPanel;

    private final JBTextField projectNameField = new JBTextField();
    private final JBTextField groupIdField = new JBTextField();
    private final JBTextField basePackageField = new JBTextField();
    private final TextFieldWithBrowseButton projectDirField = new TextFieldWithBrowseButton();

    private final JRadioButton singleRadio = new JRadioButton("Single (单体)");
    private final JRadioButton microserviceRadio = new JRadioButton("Microservice (微服务)");
    private final JRadioButton smartRadio = new JRadioButton("Smart (多栈微服务)");
    private final JRadioButton microPrdRadio = new JRadioButton("Micro-PRD (微服务产品层)");
    private final ButtonGroup projectTypeGroup = new ButtonGroup();

    private final JRadioButton mavenRadio = new JRadioButton("Maven");
    private final JRadioButton gradleRadio = new JRadioButton("Gradle");
    private final ButtonGroup buildTypeGroup = new ButtonGroup();

    private final ComboBox<OrmType> ormTypeCombo = new ComboBox<>(OrmType.values());
    private final ComboBox<RpcType> rpcTypeCombo = new ComboBox<>(RpcType.values());
    private final JBLabel rpcTypeLabel = new JBLabel("RPC 类型:");
    private final ComboBox<PrdType> prdTypeCombo = new ComboBox<>(PrdType.values());
    private final JBLabel prdTypeLabel = new JBLabel("PRD 策略:");

    private final JRadioButton bmsRadio = new JRadioButton("BMS (后台管理端)");
    private final JRadioButton clientRadio = new JRadioButton("CLIENT (C端)");
    private final ButtonGroup appTypeGroup = new ButtonGroup();

    private final JRadioButton logbackRadio = new JRadioButton("Logback");
    private final JRadioButton log4j2Radio = new JRadioButton("Log4j2");
    private final ButtonGroup logTypeGroup = new ButtonGroup();

    private final JCheckBox includeAuthCheckBox = new JCheckBox("引入 Auth 权限模块");

    private final JBTextField frameworkVersionField = new JBTextField("3.2.12");
    private final JBTextField projectVersionField = new JBTextField("1.0.0-SNAPSHOT");

    public ProjectConfigPanel() {
        projectDirField.addBrowseFolderListener(null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
                        .withTitle("选择项目输出目录"));

        projectTypeGroup.add(singleRadio);
        projectTypeGroup.add(microserviceRadio);
        projectTypeGroup.add(smartRadio);
        projectTypeGroup.add(microPrdRadio);
        singleRadio.setSelected(true);

        buildTypeGroup.add(mavenRadio);
        buildTypeGroup.add(gradleRadio);
        mavenRadio.setSelected(true);

        appTypeGroup.add(bmsRadio);
        appTypeGroup.add(clientRadio);

        prdTypeCombo.setSelectedItem(PrdType.NONE);

        logTypeGroup.add(logbackRadio);
        logTypeGroup.add(log4j2Radio);
        logbackRadio.setSelected(true);

        setupDynamicVisibility();

        JPanel projectTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        projectTypePanel.add(singleRadio);
        projectTypePanel.add(microPrdRadio);
        projectTypePanel.add(microserviceRadio);
        projectTypePanel.add(smartRadio);

        JPanel buildTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buildTypePanel.add(mavenRadio);
        buildTypePanel.add(gradleRadio);

        JPanel appTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        appTypePanel.add(bmsRadio);
        appTypePanel.add(clientRadio);

        JPanel logTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        logTypePanel.add(logbackRadio);
        logTypePanel.add(log4j2Radio);

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.anchor = GridBagConstraints.WEST;
        int row = 0;

        addTwoColumnRow(row++, "项目名称 *:", projectNameField, "GroupId *:", groupIdField);
        addTwoColumnRow(row++, "包根路径 *:", basePackageField, "项目目录 *:", projectDirField);
        addTwoColumnRow(row++, "框架版本:", frameworkVersionField, "项目版本:", projectVersionField);

        addSeparator(row++);

        addFullWidthRow(row++, "项目类型:", projectTypePanel);
        addFullWidthRow(row++, "构建工具:", buildTypePanel);
        addThreeColumnRow(row++, "ORM 框架:", ormTypeCombo, prdTypeLabel, prdTypeCombo, rpcTypeLabel, rpcTypeCombo);
        addFullWidthRow(row++, "应用类型 *:", appTypePanel);
        addFullWidthRow(row++, "日志框架:", logTypePanel);

        addSeparator(row++);

        addFullWidthRow(row++, "权限模块:", includeAuthCheckBox);

        GridBagConstraints fillerGbc = new GridBagConstraints();
        fillerGbc.gridx = 0; fillerGbc.gridy = row; fillerGbc.gridwidth = 4;
        fillerGbc.weighty = 1.0; fillerGbc.fill = GridBagConstraints.VERTICAL;
        mainPanel.add(Box.createVerticalGlue(), fillerGbc);

        updateRpcTypeVisibility();
        updateAppTypeVisibility();
    }

    private void addTwoColumnRow(int row, String label1, JComponent comp1, String label2, JComponent comp2) {
        addTwoColumnRow(row, new JBLabel(label1), comp1, new JBLabel(label2), comp2);
    }

    private void addTwoColumnRow(int row, JComponent label1, JComponent comp1, JComponent label2, JComponent comp2) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(label1, gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(comp1, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(label2, gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(comp2, gbc);
    }

    private void addThreeColumnRow(int row, String label1, JComponent comp1,
                                    JComponent label2, JComponent comp2,
                                    JComponent label3, JComponent comp3) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 0, 0, 8);
        g.anchor = GridBagConstraints.WEST;
        g.gridy = 0;

        g.gridx = 0; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        panel.add(new JBLabel(label1), g);
        g.gridx = 1; g.weightx = 0.34; g.fill = GridBagConstraints.HORIZONTAL;
        panel.add(comp1, g);
        g.gridx = 2; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        panel.add(label2, g);
        g.gridx = 3; g.weightx = 0.33; g.fill = GridBagConstraints.HORIZONTAL;
        panel.add(comp2, g);
        g.gridx = 4; g.weightx = 0; g.fill = GridBagConstraints.NONE; g.insets = new Insets(0, 0, 0, 8);
        panel.add(label3, g);
        g.gridx = 5; g.weightx = 0.33; g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(0, 0, 0, 0);
        panel.add(comp3, g);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridwidth = 4; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(panel, gbc);
    }

    private void addFullWidthRow(int row, String label, JComponent comp) {
        addLabeledRow(row, new JBLabel(label), comp);
    }

    private void addLabeledRow(int row, JComponent labelComp, JComponent comp) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(labelComp, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(comp, gbc);
    }

    private void addSeparator(int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        mainPanel.add(new JSeparator(), gbc);
    }

    private void setupDynamicVisibility() {
        singleRadio.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateRpcTypeVisibility();
            }
        });
        microserviceRadio.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateRpcTypeVisibility();
            }
        });
        smartRadio.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateRpcTypeVisibility();
            }
        });
        microPrdRadio.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateRpcTypeVisibility();
            }
        });

        prdTypeCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateAppTypeVisibility();
            }
        });

    }

    private void updateRpcTypeVisibility() {
        boolean rpcEnabled = microserviceRadio.isSelected() || microPrdRadio.isSelected();
        rpcTypeLabel.setEnabled(rpcEnabled);
        rpcTypeCombo.setEnabled(rpcEnabled);
        if (!rpcEnabled) {
            rpcTypeCombo.setSelectedItem(RpcType.NONE);
        } else if (microPrdRadio.isSelected() && RpcType.NONE.equals(rpcTypeCombo.getSelectedItem())) {
            rpcTypeCombo.setSelectedItem(RpcType.CLOUD);
        }

        boolean ormEnabled = !microPrdRadio.isSelected();
        ormTypeCombo.setEnabled(ormEnabled);
        if (!ormEnabled) {
            ormTypeCombo.setSelectedItem(OrmType.NONE);
        }

        boolean prdEnabled = !singleRadio.isSelected() && !microPrdRadio.isSelected();
        prdTypeLabel.setEnabled(prdEnabled);
        prdTypeCombo.setEnabled(prdEnabled);
        if (!prdEnabled) {
            prdTypeCombo.setSelectedItem(PrdType.NONE);
        }

        updateAppTypeVisibility();
    }

    private void updateAppTypeVisibility() {
        Object selectedPrd = prdTypeCombo.getSelectedItem();
        boolean enabled = singleRadio.isSelected()
                || microPrdRadio.isSelected()
                || PrdType.SINGLE.equals(selectedPrd);
        bmsRadio.setEnabled(enabled);
        clientRadio.setEnabled(enabled);
        if (enabled) {
            if (!bmsRadio.isSelected() && !clientRadio.isSelected()) {
                bmsRadio.setSelected(true);
            }
        } else {
            appTypeGroup.clearSelection();
        }
    }

    public void addOrmTypeChangeListener(java.awt.event.ItemListener listener) {
        ormTypeCombo.addItemListener(listener);
    }

    public OrmType getSelectedOrmType() {
        return (OrmType) ormTypeCombo.getSelectedItem();
    }

    public void addProjectTypeChangeListener(Runnable listener) {
        singleRadio.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) listener.run(); });
        microserviceRadio.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) listener.run(); });
        smartRadio.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) listener.run(); });
        microPrdRadio.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) listener.run(); });
    }

    public boolean isSmartProjectType() {
        return smartRadio.isSelected();
    }

    public boolean isMicroPrdProjectType() {
        return microPrdRadio.isSelected();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public ProjectProperties toProperties() {
        ProjectProperties pp = new ProjectProperties();
        pp.setProjectName(projectNameField.getText().trim());
        pp.setGroupId(groupIdField.getText().trim());
        pp.setBasePackage(basePackageField.getText().trim());
        pp.setProjectDir(projectDirField.getText().trim());

        if (singleRadio.isSelected()) pp.setProjectType(ProjectType.SINGLE);
        else if (microserviceRadio.isSelected()) pp.setProjectType(ProjectType.MICROSERVICE);
        else if (microPrdRadio.isSelected()) pp.setProjectType(ProjectType.MICRO_PRD);
        else pp.setProjectType(ProjectType.SMART);

        pp.setBuildType(mavenRadio.isSelected() ? BuildType.MAVEN : BuildType.GRADLE);
        pp.setOrmType((OrmType) ormTypeCombo.getSelectedItem());
        pp.setRpcType((RpcType) rpcTypeCombo.getSelectedItem());
        pp.setPrdType((PrdType) prdTypeCombo.getSelectedItem());

        if (bmsRadio.isEnabled()) {
            if (bmsRadio.isSelected()) pp.setAppType(AppType.BMS);
            else if (clientRadio.isSelected()) pp.setAppType(AppType.CLIENT);
        }

        pp.setLogType(logbackRadio.isSelected() ? LogType.LOGBACK : LogType.LOG4J2);
        pp.setFrameworkVersion(frameworkVersionField.getText().trim());
        pp.setProjectVersion(projectVersionField.getText().trim());

        pp.setIncludeAuth(includeAuthCheckBox.isSelected());

        return pp;
    }

    public String validateInput() {
        if (projectNameField.getText().trim().isEmpty()) return "项目名称不能为空";
        if (groupIdField.getText().trim().isEmpty()) return "GroupId 不能为空";
        if (basePackageField.getText().trim().isEmpty()) return "包根路径不能为空";
        if (projectDirField.getText().trim().isEmpty()) return "项目目录不能为空";
        if (bmsRadio.isEnabled() && !bmsRadio.isSelected() && !clientRadio.isSelected()) return "请选择应用类型";
        if (microPrdRadio.isSelected() && RpcType.NONE.equals(rpcTypeCombo.getSelectedItem())) return "MICRO_PRD 项目必须选择 RPC 类型（CLOUD 或 DUBBO）";
        return null;
    }

    public void loadFrom(ProjectProperties pp) {
        if (pp == null) return;
        if (pp.getProjectName() != null) projectNameField.setText(pp.getProjectName());
        if (pp.getGroupId() != null) groupIdField.setText(pp.getGroupId());
        if (pp.getBasePackage() != null) basePackageField.setText(pp.getBasePackage());
        if (pp.getProjectDir() != null) projectDirField.setText(pp.getProjectDir());
        if (pp.getProjectType() != null) {
            switch (pp.getProjectType()) {
                case SINGLE -> singleRadio.setSelected(true);
                case MICROSERVICE -> microserviceRadio.setSelected(true);
                case SMART -> smartRadio.setSelected(true);
                case MICRO_PRD -> microPrdRadio.setSelected(true);
            }
        }
        if (pp.getBuildType() != null) {
            if (pp.getBuildType() == BuildType.MAVEN) mavenRadio.setSelected(true);
            else gradleRadio.setSelected(true);
        }
        if (pp.getOrmType() != null) ormTypeCombo.setSelectedItem(pp.getOrmType());
        if (pp.getRpcType() != null) rpcTypeCombo.setSelectedItem(pp.getRpcType());
        if (pp.getPrdType() != null) prdTypeCombo.setSelectedItem(pp.getPrdType());
        if (pp.getAppType() != null) {
            if (pp.getAppType() == AppType.BMS) bmsRadio.setSelected(true);
            else clientRadio.setSelected(true);
        }
        updateAppTypeVisibility();
        if (pp.getLogType() != null) {
            if (pp.getLogType() == LogType.LOGBACK) logbackRadio.setSelected(true);
            else log4j2Radio.setSelected(true);
        }
        if (pp.getFrameworkVersion() != null) frameworkVersionField.setText(pp.getFrameworkVersion());
        if (pp.getProjectVersion() != null) projectVersionField.setText(pp.getProjectVersion());

        if (pp.getIncludeAuth() != null) {
            includeAuthCheckBox.setSelected(pp.getIncludeAuth());
        }
    }

    public void resetToDefaults() {
        projectNameField.setText("");
        groupIdField.setText("");
        basePackageField.setText("");
        projectDirField.setText("");
        frameworkVersionField.setText("3.2.12");
        projectVersionField.setText("1.0.0-SNAPSHOT");
        singleRadio.setSelected(true);
        mavenRadio.setSelected(true);
        ormTypeCombo.setSelectedItem(OrmType.NONE);
        prdTypeCombo.setSelectedItem(PrdType.NONE);
        bmsRadio.setSelected(true);
        logbackRadio.setSelected(true);
        includeAuthCheckBox.setSelected(false);
    }
}
