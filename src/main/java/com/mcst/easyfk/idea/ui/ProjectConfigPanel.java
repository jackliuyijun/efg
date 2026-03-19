package com.mcst.easyfk.idea.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.mcst.easyfk.generator.enums.*;
import com.mcst.easyfk.generator.properties.ModuleInfo;
import com.mcst.easyfk.generator.properties.ProjectProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectConfigPanel {

    private final JPanel mainPanel;

    private final JBTextField projectNameField = new JBTextField();
    private final JBTextField groupIdField = new JBTextField();
    private final JBTextField basePackageField = new JBTextField();
    private final TextFieldWithBrowseButton projectDirField = new TextFieldWithBrowseButton();

    private final JRadioButton singleRadio = new JRadioButton("Single (单体)");
    private final JRadioButton microserviceRadio = new JRadioButton("Microservice (微服务)");
    private final JRadioButton smartRadio = new JRadioButton("Smart (多栈微服务)");
    private final ButtonGroup projectTypeGroup = new ButtonGroup();

    private final JRadioButton mavenRadio = new JRadioButton("Maven");
    private final JRadioButton gradleRadio = new JRadioButton("Gradle");
    private final ButtonGroup buildTypeGroup = new ButtonGroup();

    private final JRadioButton groovyRadio = new JRadioButton("Groovy");
    private final JRadioButton kotlinRadio = new JRadioButton("Kotlin");
    private final ButtonGroup gradleTypeGroup = new ButtonGroup();
    private final JPanel gradleTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    private final JBLabel gradleTypeLabel = new JBLabel("Gradle DSL:");

    private final ComboBox<OrmType> ormTypeCombo = new ComboBox<>(OrmType.values());
    private final ComboBox<RpcType> rpcTypeCombo = new ComboBox<>(RpcType.values());
    private final JBLabel rpcTypeLabel = new JBLabel("RPC 类型:");
    private final ComboBox<PrdType> prdTypeCombo = new ComboBox<>(PrdType.values());

    private final JRadioButton bmsRadio = new JRadioButton("BMS (后台管理端)");
    private final JRadioButton clientRadio = new JRadioButton("CLIENT (C端)");
    private final ButtonGroup appTypeGroup = new ButtonGroup();

    private final JRadioButton logbackRadio = new JRadioButton("Logback");
    private final JRadioButton log4j2Radio = new JRadioButton("Log4j2");
    private final ButtonGroup logTypeGroup = new ButtonGroup();

    private static final String[] AVAILABLE_MODULES = {
            "auth", "banner", "brand", "category", "vip", "user", "dict",
            "goods", "group", "tag", "merchant", "order", "trading",
            "payment", "pickup", "container", "media"
    };
    private final JCheckBox[] moduleCheckBoxes = new JCheckBox[AVAILABLE_MODULES.length];

    private final JBTextField frameworkVersionField = new JBTextField("3.2.12");
    private final JBTextField projectVersionField = new JBTextField("1.0.0-SNAPSHOT");

    public ProjectConfigPanel() {
        projectDirField.addBrowseFolderListener(null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
                        .withTitle("选择项目输出目录"));

        projectTypeGroup.add(singleRadio);
        projectTypeGroup.add(microserviceRadio);
        projectTypeGroup.add(smartRadio);
        singleRadio.setSelected(true);

        buildTypeGroup.add(mavenRadio);
        buildTypeGroup.add(gradleRadio);
        mavenRadio.setSelected(true);

        gradleTypeGroup.add(groovyRadio);
        gradleTypeGroup.add(kotlinRadio);
        groovyRadio.setSelected(true);
        gradleTypePanel.add(groovyRadio);
        gradleTypePanel.add(kotlinRadio);

        appTypeGroup.add(bmsRadio);
        appTypeGroup.add(clientRadio);

        prdTypeCombo.setSelectedItem(PrdType.SINGLE);

        for (int i = 0; i < AVAILABLE_MODULES.length; i++) {
            moduleCheckBoxes[i] = new JCheckBox(AVAILABLE_MODULES[i]);
        }

        logTypeGroup.add(logbackRadio);
        logTypeGroup.add(log4j2Radio);
        logbackRadio.setSelected(true);

        setupDynamicVisibility();

        JPanel projectTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        projectTypePanel.add(singleRadio);
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

        JPanel moduleGridPanel = new JPanel(new GridLayout(0, 3, 8, 2));
        for (JCheckBox cb : moduleCheckBoxes) {
            moduleGridPanel.add(cb);
        }
        JBScrollPane moduleScrollPane = new JBScrollPane(moduleGridPanel);

        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.anchor = GridBagConstraints.WEST;
        int row = 0;

        addTwoColumnRow(row++, "项目名称 *:", projectNameField, "GroupId *:", groupIdField);
        addTwoColumnRow(row++, "包根路径 *:", basePackageField, "项目目录 *:", projectDirField);
        addTwoColumnRow(row++, "框架版本:", frameworkVersionField, "项目版本:", projectVersionField);

        addSeparator(row++);

        addFullWidthRow(row++, "项目类型:", projectTypePanel);
        addFullWidthRow(row++, "构建工具:", buildTypePanel);
        addLabeledRow(row, gradleTypeLabel, gradleTypePanel); row++;
        addTwoColumnRow(row++, "ORM 框架:", ormTypeCombo, "PRD 策略:", prdTypeCombo);
        addLabeledRow(row, rpcTypeLabel, rpcTypeCombo); row++;
        addFullWidthRow(row++, "应用类型 *:", appTypePanel);
        addFullWidthRow(row++, "日志框架:", logTypePanel);

        addSeparator(row++);

        gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(3, 4, 3, 4);
        mainPanel.add(new JBLabel("业务模块:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        mainPanel.add(moduleScrollPane, gbc);

        updateGradleTypeVisibility();
        updateRpcTypeVisibility();
    }

    private void addTwoColumnRow(int row, String label1, JComponent comp1, String label2, JComponent comp2) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JBLabel(label1), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(comp1, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JBLabel(label2), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(comp2, gbc);
    }

    private void addFullWidthRow(int row, String label, JComponent comp) {
        addLabeledRow(row, new JBLabel(label), comp);
    }

    private void addLabeledRow(int row, JComponent labelComp, JComponent comp) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
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
        gbc.insets = new Insets(6, 0, 6, 0);
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

        mavenRadio.addItemListener(e -> updateGradleTypeVisibility());
        gradleRadio.addItemListener(e -> updateGradleTypeVisibility());
    }

    private void updateRpcTypeVisibility() {
        boolean show = !singleRadio.isSelected();
        rpcTypeLabel.setVisible(show);
        rpcTypeCombo.setVisible(show);
    }

    private void updateGradleTypeVisibility() {
        boolean show = gradleRadio.isSelected();
        gradleTypeLabel.setVisible(show);
        gradleTypePanel.setVisible(show);
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
        else pp.setProjectType(ProjectType.SMART);

        pp.setBuildType(mavenRadio.isSelected() ? BuildType.MAVEN : BuildType.GRADLE);
        pp.setGradleType(groovyRadio.isSelected() ? GradleType.GROOVY : GradleType.KOTLIN);
        pp.setOrmType((OrmType) ormTypeCombo.getSelectedItem());
        pp.setRpcType((RpcType) rpcTypeCombo.getSelectedItem());
        pp.setPrdType((PrdType) prdTypeCombo.getSelectedItem());

        if (bmsRadio.isSelected()) pp.setAppType(AppType.BMS);
        else if (clientRadio.isSelected()) pp.setAppType(AppType.CLIENT);

        pp.setLogType(logbackRadio.isSelected() ? LogType.LOGBACK : LogType.LOG4J2);
        pp.setFrameworkVersion(frameworkVersionField.getText().trim());
        pp.setProjectVersion(projectVersionField.getText().trim());

        List<ModuleInfo> modules = new ArrayList<>();
        for (int i = 0; i < AVAILABLE_MODULES.length; i++) {
            if (moduleCheckBoxes[i].isSelected()) {
                ModuleInfo mi = new ModuleInfo();
                mi.setName(AVAILABLE_MODULES[i]);
                if ("auth".equals(AVAILABLE_MODULES[i])) {
                    mi.setPrdSplit(false);
                }
                modules.add(mi);
            }
        }
        if (!modules.isEmpty()) {
            pp.setModules(modules);
        }

        return pp;
    }

    public String validateInput() {
        if (projectNameField.getText().trim().isEmpty()) return "项目名称不能为空";
        if (groupIdField.getText().trim().isEmpty()) return "GroupId 不能为空";
        if (basePackageField.getText().trim().isEmpty()) return "包根路径不能为空";
        if (projectDirField.getText().trim().isEmpty()) return "项目目录不能为空";
        if (!bmsRadio.isSelected() && !clientRadio.isSelected()) return "请选择应用类型";
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
            }
        }
        if (pp.getBuildType() != null) {
            if (pp.getBuildType() == BuildType.MAVEN) mavenRadio.setSelected(true);
            else gradleRadio.setSelected(true);
        }
        if (pp.getGradleType() != null) {
            if (pp.getGradleType() == GradleType.GROOVY) groovyRadio.setSelected(true);
            else kotlinRadio.setSelected(true);
        }
        if (pp.getOrmType() != null) ormTypeCombo.setSelectedItem(pp.getOrmType());
        if (pp.getRpcType() != null) rpcTypeCombo.setSelectedItem(pp.getRpcType());
        if (pp.getPrdType() != null) prdTypeCombo.setSelectedItem(pp.getPrdType());
        if (pp.getAppType() != null) {
            if (pp.getAppType() == AppType.BMS) bmsRadio.setSelected(true);
            else clientRadio.setSelected(true);
        }
        if (pp.getLogType() != null) {
            if (pp.getLogType() == LogType.LOGBACK) logbackRadio.setSelected(true);
            else log4j2Radio.setSelected(true);
        }
        if (pp.getFrameworkVersion() != null) frameworkVersionField.setText(pp.getFrameworkVersion());
        if (pp.getProjectVersion() != null) projectVersionField.setText(pp.getProjectVersion());

        if (pp.getModules() != null) {
            Set<String> selected = pp.getModules().stream()
                    .map(ModuleInfo::getName).collect(Collectors.toSet());
            for (int i = 0; i < AVAILABLE_MODULES.length; i++) {
                moduleCheckBoxes[i].setSelected(selected.contains(AVAILABLE_MODULES[i]));
            }
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
        prdTypeCombo.setSelectedItem(PrdType.SINGLE);
        bmsRadio.setSelected(true);
        logbackRadio.setSelected(true);
        for (JCheckBox cb : moduleCheckBoxes) {
            cb.setSelected(false);
        }
    }
}
