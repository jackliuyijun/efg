package com.mcst.easyfk.idea.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.mcst.easyfk.generator.enums.DbType;
import com.mcst.easyfk.generator.util.GeneratorUtil;
import com.mcst.easyfk.generator.vo.ModelInfo;
import com.mcst.easyfk.idea.util.IdeaFileUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbTableImportDialog extends DialogWrapper {

    private final JBTable table;
    private final TableListModel tableModel;
    private final JBTextField tablePrefixField;
    private final List<ModelInfo> selectedModels = new ArrayList<>();

    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPwd;
    private final DbType dbType;

    public DbTableImportDialog(Project project, String jdbcUrl, String dbUser, String dbPwd,
                               DbType dbType, String tablePrefix) {
        super(project, true);
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPwd = dbPwd;
        this.dbType = dbType;

        setTitle("选择数据库表");
        setOKButtonText("确认选择");
        setCancelButtonText("取消");

        tableModel = new TableListModel();
        table = new JBTable(tableModel);
        table.setRowHeight(26);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);

        tablePrefixField = new JBTextField(tablePrefix != null ? tablePrefix : "");
        tablePrefixField.getEmptyText().setText("多个前缀用英文逗号隔开");

        init();
        loadTables();
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        topPanel.add(new JBLabel("忽略表前缀:"));
        topPanel.add(tablePrefixField);
        tablePrefixField.setColumns(15);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        panel.setPreferredSize(new Dimension(550, 400));
        return panel;
    }

    private void loadTables() {
        SwingWorker<List<String[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String[]> doInBackground() throws Exception {
                List<String[]> tables = new ArrayList<>();
                try (Connection conn = IdeaFileUtil.getConnection(jdbcUrl, dbUser, dbPwd, dbType)) {
                    DatabaseMetaData meta = conn.getMetaData();
                    String catalog = conn.getCatalog();
                    ResultSet rs = meta.getTables(catalog, null, null, new String[]{"TABLE"});
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        String remark = rs.getString("REMARKS");
                        tables.add(new String[]{tableName, remark != null ? remark : ""});
                    }
                }
                return tables;
            }

            @Override
            protected void done() {
                try {
                    List<String[]> tables = get();
                    tableModel.setData(tables);
                } catch (Exception ex) {
                    Messages.showErrorDialog("加载表列表失败: " + ex.getMessage(), "错误");
                }
            }
        };
        worker.execute();
    }

    @Override
    protected void doOKAction() {
        String prefix = tablePrefixField.getText().trim();
        List<String[]> data = tableModel.getData();
        for (int i = 0; i < data.size(); i++) {
            if (tableModel.isSelected(i)) {
                String tableName = data.get(i)[0];
                String remark = data.get(i)[1];
                ModelInfo mi = new ModelInfo();
                mi.setTableName(tableName);
                mi.setModelDesc(remark);

                mi.setModelName(GeneratorUtil.buildModelNameFromTable(tableName, prefix));
                mi.setIdType("String");
                mi.setOnlyRepository(false);
                mi.setCreateController(true);
                selectedModels.add(mi);
            }
        }
        super.doOKAction();
    }

    public List<ModelInfo> getSelectedModels() {
        return selectedModels;
    }

    public String getTablePrefix() {
        return tablePrefixField.getText().trim();
    }

    private static class TableListModel extends AbstractTableModel {
        private final List<String[]> data = new ArrayList<>();
        private final List<Boolean> selected = new ArrayList<>();

        public void setData(List<String[]> tables) {
            data.clear();
            selected.clear();
            data.addAll(tables);
            for (int i = 0; i < tables.size(); i++) {
                selected.add(false);
            }
            fireTableDataChanged();
        }

        public List<String[]> getData() {
            return data;
        }

        public boolean isSelected(int row) {
            return selected.get(row);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> "选择";
                case 1 -> "表名";
                case 2 -> "备注";
                default -> "";
            };
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return switch (columnIndex) {
                case 0 -> selected.get(rowIndex);
                case 1 -> data.get(rowIndex)[0];
                case 2 -> data.get(rowIndex)[1];
                default -> null;
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                selected.set(rowIndex, (Boolean) aValue);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
}
