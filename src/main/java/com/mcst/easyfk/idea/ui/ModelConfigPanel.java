package com.mcst.easyfk.idea.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.mcst.easyfk.generator.enums.DbType;
import com.mcst.easyfk.generator.util.GeneratorUtil;
import com.mcst.easyfk.generator.vo.ModelInfo;
import com.mcst.easyfk.idea.util.IdeaFileUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModelConfigPanel {

    private final JPanel mainPanel;
    private final JBTable table;
    private final ModelTableModel tableModel;

    private final ComboBox<DbType> dbTypeCombo = new ComboBox<>(new DbType[]{
            DbType.MYSQL, DbType.POSTGRE_SQL
    });
    private final JBTextField dbShortUrlField = new JBTextField();
    private final JBTextField dbUserField = new JBTextField("root");
    private final JPasswordField dbPwdField = new JPasswordField();
    private final JButton testConnBtn = new JButton("测试连接");
    private final JBTextField tablePrefixField = new JBTextField();
    private final JBTextField dbTablesField = new JBTextField();
    private final JTabbedPane modeTabbedPane = new JTabbedPane();

    private JPanel dbConnPanel;
    private JPanel dbImportPanel;
    private JPanel modelPanel;

    private Project project;

    private static final String[] COLUMN_NAMES = {
            "Model 名称", "表名", "说明", "ID 类型", "仅 Repository", "生成 Controller"
    };

    public ModelConfigPanel() {
        dbShortUrlField.getEmptyText().setText("localhost:3306/my_database");
        tablePrefixField.getEmptyText().setText("多个前缀用英文逗号隔开");
        dbTablesField.getEmptyText().setText("留空则导入时显示全部表，多个用逗号分隔");
        testConnBtn.addActionListener(e -> testConnection());

        tableModel = new ModelTableModel();
        table = new JBTable(tableModel);
        table.setRowHeight(28);

        table.getColumnModel().getColumn(3).setCellEditor(
                new DefaultCellEditor(new JComboBox<>(new String[]{"String", "Long", "Integer"})));
        table.getColumnModel().getColumn(4).setCellRenderer(table.getDefaultRenderer(Boolean.class));
        table.getColumnModel().getColumn(4).setCellEditor(table.getDefaultEditor(Boolean.class));
        table.getColumnModel().getColumn(5).setCellRenderer(table.getDefaultRenderer(Boolean.class));
        table.getColumnModel().getColumn(5).setCellEditor(table.getDefaultEditor(Boolean.class));

        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        dbConnPanel = buildDbConnPanel();
        dbImportPanel = buildDbImportPanel();
        modelPanel = buildModelPanel();

        modeTabbedPane.addTab("选择表", dbImportPanel);
        modeTabbedPane.addTab("确认模型", modelPanel);
        modeTabbedPane.addChangeListener(e -> {
            if (modeTabbedPane.getSelectedIndex() == 1) {
                syncModelsFromTables();
            }
        });

        mainPanel = new JPanel(new BorderLayout(0, 4));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        mainPanel.add(dbConnPanel, BorderLayout.NORTH);
        mainPanel.add(modeTabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildDbConnPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "数据库连接",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));
        Dimension userFieldSize = new Dimension(120, dbUserField.getPreferredSize().height);
        dbUserField.setPreferredSize(userFieldSize);
        dbUserField.setMinimumSize(userFieldSize);
        dbUserField.setMaximumSize(userFieldSize);
        Dimension pwdFieldSize = new Dimension(120, dbPwdField.getPreferredSize().height);
        dbPwdField.setPreferredSize(pwdFieldSize);
        dbPwdField.setMinimumSize(pwdFieldSize);
        dbPwdField.setMaximumSize(pwdFieldSize);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.anchor = GridBagConstraints.WEST;
        int row = 0;

        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JBLabel("数据库类型:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(dbTypeCombo, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(testConnBtn, gbc);
        row++;

        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JBLabel("连接地址:"), gbc);
        JPanel connRow = new JPanel(new GridBagLayout());
        GridBagConstraints rowGbc = new GridBagConstraints();
        rowGbc.insets = new Insets(0, 0, 0, 6);
        rowGbc.gridy = 0;
        rowGbc.gridx = 0;
        rowGbc.weightx = 1.0;
        rowGbc.fill = GridBagConstraints.HORIZONTAL;
        connRow.add(dbShortUrlField, rowGbc);
        rowGbc.gridx = 1;
        rowGbc.weightx = 0;
        rowGbc.fill = GridBagConstraints.NONE;
        connRow.add(new JBLabel("用户名:"), rowGbc);
        rowGbc.gridx = 2;
        rowGbc.weightx = 0;
        rowGbc.fill = GridBagConstraints.NONE;
        connRow.add(dbUserField, rowGbc);
        rowGbc.gridx = 3;
        rowGbc.weightx = 0;
        rowGbc.fill = GridBagConstraints.NONE;
        connRow.add(new JBLabel("密码:"), rowGbc);
        rowGbc.gridx = 4;
        rowGbc.weightx = 0;
        rowGbc.fill = GridBagConstraints.NONE;
        rowGbc.insets = new Insets(0, 0, 0, 0);
        connRow.add(dbPwdField, rowGbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(connRow, gbc);

        return panel;
    }

    private JPanel buildDbImportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JBLabel("忽略表前缀:"), gbc);
        JPanel prefixRow = new JPanel(new BorderLayout(6, 0));
        prefixRow.add(tablePrefixField, BorderLayout.CENTER);
        JButton clearTablesBtn = new JButton("清空表名");
        clearTablesBtn.addActionListener(e -> dbTablesField.setText(""));
        prefixRow.add(clearTablesBtn, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(prefixRow, gbc);

        gbc.gridy = 1; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JBLabel("指定表名:"), gbc);
        JButton importTablesBtn = new JButton("从数据库选择表...");
        importTablesBtn.addActionListener(e -> importTablesFromDb());
        JPanel tableRow = new JPanel(new BorderLayout(6, 0));
        tableRow.add(dbTablesField, BorderLayout.CENTER);
        tableRow.add(importTablesBtn, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(tableRow, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel buildModelPanel() {
        JPanel tablePanel = ToolbarDecorator.createDecorator(table)
                .setAddAction(button -> addEmptyRow())
                .setRemoveAction(button -> removeSelectedRows())
                .createPanel();

        return tablePanel;
    }

    private void testConnection() {
        testConnBtn.setEnabled(false);
        testConnBtn.setText("连接中...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private String errorMsg;

            @Override
            protected Boolean doInBackground() {
                try {
                    String url = buildJdbcUrl();
                    Connection conn = IdeaFileUtil.getConnection(url, dbUserField.getText(),
                            new String(dbPwdField.getPassword()), (DbType) dbTypeCombo.getSelectedItem());
                    conn.close();
                    return true;
                } catch (Exception ex) {
                    errorMsg = ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                testConnBtn.setText("测试连接");
                testConnBtn.setEnabled(true);
                try {
                    if (get()) {
                        Messages.showInfoMessage("数据库连接成功", "测试连接");
                    } else {
                        Messages.showErrorDialog("连接失败: " + errorMsg, "测试连接");
                    }
                } catch (Exception ex) {
                    Messages.showErrorDialog("连接失败: " + ex.getMessage(), "测试连接");
                }
            }
        };
        worker.execute();
    }

    public String buildJdbcUrl() {
        DbType dbType = (DbType) dbTypeCombo.getSelectedItem();
        String shortUrl = dbShortUrlField.getText().trim();
        if (dbType == DbType.POSTGRE_SQL) {
            return "jdbc:postgresql://" + shortUrl;
        } else {
            return "jdbc:mysql://" + shortUrl + "?characterEncoding=UTF-8&useSSL=false&useInformationSchema=true&remarks=true&useUnicode=true&allowPublicKeyRetrieval=true";
        }
    }

    public void setProject(Project project) {
        this.project = project;
    }

    private void addEmptyRow() {
        ModelInfo mi = new ModelInfo();
        mi.setIdType("String");
        mi.setOnlyRepository(false);
        mi.setCreateController(true);
        tableModel.addRow(mi);
    }

    private void removeSelectedRows() {
        int[] rows = table.getSelectedRows();
        for (int i = rows.length - 1; i >= 0; i--) {
            tableModel.removeRow(rows[i]);
        }
    }

    public void addModels(List<ModelInfo> models) {
        for (ModelInfo m : models) {
            tableModel.addRow(m);
        }
    }

    public void setModelList(List<ModelInfo> models) {
        tableModel.setData(models);
    }

    public List<ModelInfo> getModelList() {
        return filterModelList(parseDbTables(getDbTables()), tableModel.getData());
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public DbType getSelectedDbType() {
        return (DbType) dbTypeCombo.getSelectedItem();
    }

    public String getDbShortUrl() {
        return dbShortUrlField.getText().trim();
    }

    public String getDbUser() {
        return dbUserField.getText().trim();
    }

    public String getDbPwd() {
        return new String(dbPwdField.getPassword());
    }

    public String getTablePrefix() {
        return tablePrefixField.getText().trim();
    }

    public String getDbTables() {
        return dbTablesField.getText().trim();
    }

    public void setDbTables(String dbTables) {
        dbTablesField.setText(dbTables != null ? dbTables : "");
    }

    public void loadDbSettings(String dbShortUrl, String dbUser, String dbPwd, String tablePrefix) {
        if (dbShortUrl != null && !dbShortUrl.isEmpty()) dbShortUrlField.setText(dbShortUrl);
        if (dbUser != null && !dbUser.isEmpty()) dbUserField.setText(dbUser);
        if (dbPwd != null && !dbPwd.isEmpty()) dbPwdField.setText(dbPwd);
        if (tablePrefix != null && !tablePrefix.isEmpty()) tablePrefixField.setText(tablePrefix);
    }

    public void setDbType(DbType dbType) {
        if (dbType != null) dbTypeCombo.setSelectedItem(dbType);
    }

    public void resetDbSettings() {
        dbTypeCombo.setSelectedItem(DbType.MYSQL);
        dbShortUrlField.setText("");
        dbUserField.setText("root");
        dbPwdField.setText("");
        tablePrefixField.setText("");
        dbTablesField.setText("");
        tableModel.clear();
    }

    public String validateInput() {
        return validateInput(false);
    }

    public String validateInput(boolean requireModelSource) {
        String dbTables = getDbTables();
        if (!dbTables.isEmpty()) {
            if (getDbShortUrl().isEmpty()) {
                return "使用 db-tables 时，数据库连接地址不能为空";
            }
            if (getDbPwd().isEmpty()) {
                return "使用 db-tables 时，数据库密码不能为空";
            }
        }
        List<ModelInfo> modelList = buildMergedModelList(parseDbTables(dbTables), getModelList());
        if (requireModelSource && dbTables.isEmpty() && modelList.isEmpty()) {
            return "db-tables 和模型列表不能同时为空";
        }
        Set<String> modelNames = new LinkedHashSet<>();
        for (int i = 0; i < modelList.size(); i++) {
            ModelInfo modelInfo = modelList.get(i);
            String modelName = modelInfo.getModelName() != null ? modelInfo.getModelName().trim() : "";
            if (modelName.isEmpty()) {
                return "确认模型第 " + (i + 1) + " 行的 Model 名称不能为空";
            }
            if (!modelNames.add(modelName)) {
                return "Model 名称重复: " + modelName;
            }
        }
        return null;
    }

    private void importTablesFromDb() {
        String error = validateDbImportSettings();
        if (error != null) {
            Messages.showErrorDialog(error, "数据库配置校验失败");
            return;
        }
        DbTableImportDialog dialog = new DbTableImportDialog(project, buildJdbcUrl(), getDbUser(), getDbPwd(),
                getSelectedDbType(), getTablePrefix());
        if (!dialog.showAndGet()) {
            return;
        }
        tablePrefixField.setText(dialog.getTablePrefix());
        List<ModelInfo> selectedModels = dialog.getSelectedModels();
        if (selectedModels.isEmpty()) {
            return;
        }
        LinkedHashSet<String> dbTableSet = new LinkedHashSet<>(parseDbTables(getDbTables()));
        for (ModelInfo modelInfo : selectedModels) {
            if (modelInfo.getTableName() != null && !modelInfo.getTableName().isBlank()) {
                dbTableSet.add(modelInfo.getTableName());
            }
        }
        dbTablesField.setText(String.join(",", dbTableSet));
    }

    private String validateDbImportSettings() {
        if (getDbShortUrl().isEmpty()) {
            return "数据库连接地址不能为空";
        }
        if (getDbPwd().isEmpty()) {
            return "数据库密码不能为空";
        }
        return null;
    }

    private List<String> parseDbTables(String dbTables) {
        List<String> tables = new ArrayList<>();
        if (dbTables == null || dbTables.isBlank()) {
            return tables;
        }
        for (String table : dbTables.split(",")) {
            String value = table.trim();
            if (!value.isEmpty()) {
                tables.add(value);
            }
        }
        return tables;
    }

    private void syncModelsFromTables() {
        tableModel.setData(buildMergedModelList(parseDbTables(getDbTables()), tableModel.getData()));
    }

    private List<ModelInfo> filterModelList(List<String> tables, List<ModelInfo> existingModels) {
        Set<String> tableSet = new LinkedHashSet<>(tables);
        List<ModelInfo> filteredModels = new ArrayList<>();
        for (ModelInfo existingModel : existingModels) {
            String tableName = existingModel.getTableName() != null ? existingModel.getTableName().trim() : "";
            if (tableName.isEmpty() || tableSet.contains(tableName)) {
                filteredModels.add(copyModelInfo(existingModel));
            }
        }
        return filteredModels;
    }

    private List<ModelInfo> buildMergedModelList(List<String> tables, List<ModelInfo> existingModels) {
        Map<String, ModelInfo> existingModelMap = new LinkedHashMap<>();
        List<ModelInfo> manualModels = new ArrayList<>();
        for (ModelInfo existingModel : filterModelList(tables, existingModels)) {
            String tableName = existingModel.getTableName() != null ? existingModel.getTableName().trim() : "";
            if (!tableName.isEmpty()) {
                existingModelMap.put(tableName, copyModelInfo(existingModel));
            } else {
                manualModels.add(copyModelInfo(existingModel));
            }
        }
        List<ModelInfo> mergedModels = new ArrayList<>();
        for (String table : tables) {
            ModelInfo modelInfo = existingModelMap.get(table);
            if (modelInfo == null) {
                modelInfo = createDefaultModelInfo(table);
            } else {
                if (modelInfo.getModelName() == null || modelInfo.getModelName().isBlank()) {
                    modelInfo.setModelName(buildModelName(table));
                }
                modelInfo.setTableName(table);
            }
            mergedModels.add(modelInfo);
        }
        mergedModels.addAll(manualModels);
        return mergedModels;
    }

    private ModelInfo createDefaultModelInfo(String tableName) {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setTableName(tableName);
        modelInfo.setModelName(buildModelName(tableName));
        modelInfo.setIdType("String");
        modelInfo.setOnlyRepository(false);
        modelInfo.setCreateController(true);
        return modelInfo;
    }

    private ModelInfo copyModelInfo(ModelInfo source) {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelName(source.getModelName());
        modelInfo.setTableName(source.getTableName());
        modelInfo.setModelDesc(source.getModelDesc());
        modelInfo.setSuperClass(source.getSuperClass());
        modelInfo.setIdType(source.getIdType());
        modelInfo.setIdIsAuto(source.getIdIsAuto());
        modelInfo.setOnlyRepository(source.getOnlyRepository());
        modelInfo.setFieldList(source.getFieldList());
        modelInfo.setBasicPackages(source.getBasicPackages());
        modelInfo.setOtherPackages(source.getOtherPackages());
        modelInfo.setForbiddenFiled(source.getForbiddenFiled());
        modelInfo.setResourceGroup(source.getResourceGroup());
        modelInfo.setResourceName(source.getResourceName());
        modelInfo.setResourceId(source.getResourceId());
        modelInfo.setResourcePath(source.getResourcePath());
        modelInfo.setResourceSort(source.getResourceSort());
        modelInfo.setModelResourceSort(source.getModelResourceSort());
        modelInfo.setTablePrefix(source.getTablePrefix());
        modelInfo.setCreateController(source.getCreateController());
        return modelInfo;
    }

    private String buildModelName(String tableName) {
        return GeneratorUtil.buildModelNameFromTable(tableName, getTablePrefix());
    }

    private static class ModelTableModel extends AbstractTableModel {
        private final List<ModelInfo> data = new ArrayList<>();

        public void addRow(ModelInfo info) {
            data.add(info);
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }

        public void setData(List<ModelInfo> models) {
            data.clear();
            if (models != null) {
                data.addAll(models);
            }
            fireTableDataChanged();
        }

        public void removeRow(int row) {
            if (row >= 0 && row < data.size()) {
                data.remove(row);
                fireTableRowsDeleted(row, row);
            }
        }

        public List<ModelInfo> getData() {
            return new ArrayList<>(data);
        }

        public void clear() {
            if (data.isEmpty()) {
                return;
            }
            int last = data.size() - 1;
            data.clear();
            fireTableRowsDeleted(0, last);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 4, 5 -> Boolean.class;
                default -> String.class;
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ModelInfo m = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> m.getModelName();
                case 1 -> m.getTableName();
                case 2 -> m.getModelDesc();
                case 3 -> m.getIdType();
                case 4 -> m.getOnlyRepository();
                case 5 -> m.getCreateController();
                default -> null;
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            ModelInfo m = data.get(rowIndex);
            switch (columnIndex) {
                case 0 -> m.setModelName((String) aValue);
                case 1 -> m.setTableName((String) aValue);
                case 2 -> m.setModelDesc((String) aValue);
                case 3 -> m.setIdType((String) aValue);
                case 4 -> m.setOnlyRepository((Boolean) aValue);
                case 5 -> m.setCreateController((Boolean) aValue);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
