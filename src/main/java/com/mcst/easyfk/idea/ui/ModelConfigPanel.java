package com.mcst.easyfk.idea.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.mcst.easyfk.generator.enums.DbType;
import com.mcst.easyfk.generator.vo.ModelInfo;
import com.mcst.easyfk.idea.util.IdeaFileUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

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
    private final JBLabel testResultLabel = new JBLabel("");
    private final JBTextField tablePrefixField = new JBTextField();
    private final JBTextField fromDbTablesField = new JBTextField();

    private JPanel dbConnPanel;
    private JPanel dbImportPanel;
    private JPanel modelPanel;

    private Project project;

    private static final String[] COLUMN_NAMES = {
            "Model 名称", "表名", "说明", "ID 类型", "仅 Repository", "生成 Controller"
    };

    public ModelConfigPanel() {
        dbShortUrlField.getEmptyText().setText("localhost:3306/my_database");
        fromDbTablesField.getEmptyText().setText("留空则导入时显示全部表，多个用逗号分隔");
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

        JTabbedPane modeTabbedPane = new JTabbedPane();
        modeTabbedPane.addTab("从数据库导入", dbImportPanel);
        modeTabbedPane.addTab("手动创建模型", modelPanel);

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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.anchor = GridBagConstraints.WEST;
        int row = 0;

        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JBLabel("数据库类型:"), gbc);
        JPanel dbTypeRow = new JPanel(new BorderLayout(6, 0));
        dbTypeRow.add(dbTypeCombo, BorderLayout.CENTER);
        JPanel testConnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        testConnPanel.add(testConnBtn);
        testConnPanel.add(testResultLabel);
        dbTypeRow.add(testConnPanel, BorderLayout.EAST);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(dbTypeRow, gbc);
        gbc.gridwidth = 1;
        row++;

        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JBLabel("连接地址:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(dbShortUrlField, gbc);
        gbc.gridwidth = 1;
        row++;

        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JBLabel("用户名:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(dbUserField, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JBLabel("密码:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(dbPwdField, gbc);
        row++;

        return panel;
    }

    private JPanel buildDbImportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JBLabel("表前缀:"), gbc);
        JPanel prefixRow = new JPanel(new BorderLayout(6, 0));
        prefixRow.add(tablePrefixField, BorderLayout.CENTER);
        JButton clearTablesBtn = new JButton("清空表名");
        clearTablesBtn.addActionListener(e -> fromDbTablesField.setText(""));
        prefixRow.add(clearTablesBtn, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(prefixRow, gbc);

        gbc.gridy = 1; gbc.gridx = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JBLabel("指定表名:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(fromDbTablesField, gbc);

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
        testResultLabel.setText("连接中...");
        testResultLabel.setForeground(Color.GRAY);

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
                try {
                    if (get()) {
                        testResultLabel.setText("连接成功");
                        testResultLabel.setForeground(new Color(0, 128, 0));
                    } else {
                        testResultLabel.setText("连接失败: " + errorMsg);
                        testResultLabel.setForeground(Color.RED);
                    }
                } catch (Exception ex) {
                    testResultLabel.setText("连接失败");
                    testResultLabel.setForeground(Color.RED);
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

    public List<ModelInfo> getModelList() {
        return tableModel.getData();
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

    public String getFromDbTables() {
        return fromDbTablesField.getText().trim();
    }

    public void loadDbSettings(String dbShortUrl, String dbUser, String tablePrefix) {
        if (dbShortUrl != null && !dbShortUrl.isEmpty()) dbShortUrlField.setText(dbShortUrl);
        if (dbUser != null && !dbUser.isEmpty()) dbUserField.setText(dbUser);
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
        fromDbTablesField.setText("");
    }

    private static class ModelTableModel extends AbstractTableModel {
        private final List<ModelInfo> data = new ArrayList<>();

        public void addRow(ModelInfo info) {
            data.add(info);
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
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
