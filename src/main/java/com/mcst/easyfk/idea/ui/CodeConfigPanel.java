package com.mcst.easyfk.idea.ui;

import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import com.mcst.easyfk.generator.properties.CodeProperties;
import com.mcst.easyfk.generator.vo.ModelInfo;
import com.mcst.easyfk.idea.settings.EasyfkSettingsState;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CodeConfigPanel {

    private final JPanel mainPanel;
    private final JTabbedPane codeTabbedPane;

    private final JBTextField moduleNameField = new JBTextField();
    private final JBTextField authorField = new JBTextField();

    private final JCheckBox springAnnotationCheck = new JCheckBox("使用 Spring 注解", true);
    private final JCheckBox extendsSupperClassCheck = new JCheckBox("继承父类", true);

    private final DtoModelTableModel dtoModelTableModel = new DtoModelTableModel();
    private final JBTable dtoModelTable = new JBTable(dtoModelTableModel);

    public CodeConfigPanel() {
        EasyfkSettingsState settings = EasyfkSettingsState.getInstance();
        if (settings != null && settings.getState() != null) {
            authorField.setText(settings.getState().defaultAuthor);
        }

        codeTabbedPane = new JTabbedPane();
        codeTabbedPane.addTab("生成业务代码", buildCodePanel());
        codeTabbedPane.addTab("更新领域模型", buildDtoUpdatePanel());

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(codeTabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildCodePanel() {
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        optionsPanel.add(springAnnotationCheck);
        optionsPanel.add(extendsSupperClassCheck);

        return FormBuilder.createFormBuilder()
                .addLabeledComponent("模块名称 *:", moduleNameField, 8, false)
                .addLabeledComponent("作者:", authorField, 8, false)
                .addSeparator(12)
                .addLabeledComponent("生成选项:", optionsPanel, 8, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private JPanel buildDtoUpdatePanel() {
        dtoModelTable.setRowHeight(28);
        dtoModelTable.getColumnModel().getColumn(0).setPreferredWidth(200);

        JPanel tablePanel = ToolbarDecorator.createDecorator(dtoModelTable)
                .setAddAction(button -> {
                    dtoModelTableModel.addRow("");
                })
                .setRemoveAction(button -> {
                    int[] rows = dtoModelTable.getSelectedRows();
                    for (int i = rows.length - 1; i >= 0; i--) {
                        dtoModelTableModel.removeRow(rows[i]);
                    }
                })
                .createPanel();

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        JLabel hint = new JLabel("  输入需要更新的模型名称（Model Name），仅更新对应的 Dto、Param 等领域模型");
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 12f));
        hint.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        wrapper.add(hint, BorderLayout.NORTH);
        wrapper.add(tablePanel, BorderLayout.CENTER);
        return wrapper;
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public boolean isDtoOnly() {
        return codeTabbedPane.getSelectedIndex() == 1;
    }

    public List<ModelInfo> getDtoUpdateModelList() {
        List<ModelInfo> list = new ArrayList<>();
        for (String name : dtoModelTableModel.getData()) {
            if (name != null && !name.trim().isEmpty()) {
                ModelInfo mi = new ModelInfo();
                mi.setModelName(name.trim());
                list.add(mi);
            }
        }
        return list;
    }

    public CodeProperties toProperties() {
        CodeProperties cp = new CodeProperties();
        cp.setModuleName(moduleNameField.getText().trim());
        cp.setAuthor(authorField.getText().trim());
        cp.setSpringAnnotation(springAnnotationCheck.isSelected());
        cp.setExtendsSupperClass(extendsSupperClassCheck.isSelected());
        return cp;
    }

    public String validateInput() {
        if (moduleNameField.getText().trim().isEmpty()) return "模块名称不能为空";
        return null;
    }

    public void loadFrom(CodeProperties cp) {
        if (cp == null) return;
        if (cp.getModuleName() != null) moduleNameField.setText(cp.getModuleName());
        if (cp.getAuthor() != null) authorField.setText(cp.getAuthor());
        springAnnotationCheck.setSelected(cp.getSpringAnnotation());
        extendsSupperClassCheck.setSelected(cp.getExtendsSupperClass());
    }

    public void resetToDefaults() {
        moduleNameField.setText("");
        EasyfkSettingsState settings = EasyfkSettingsState.getInstance();
        if (settings != null && settings.getState() != null) {
            authorField.setText(settings.getState().defaultAuthor);
        } else {
            authorField.setText("");
        }
        springAnnotationCheck.setSelected(true);
        extendsSupperClassCheck.setSelected(true);
        dtoModelTableModel.clear();
        codeTabbedPane.setSelectedIndex(0);
    }

    private static class DtoModelTableModel extends AbstractTableModel {
        private final List<String> data = new ArrayList<>();

        public void addRow(String modelName) {
            data.add(modelName);
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }

        public void removeRow(int row) {
            if (row >= 0 && row < data.size()) {
                data.remove(row);
                fireTableRowsDeleted(row, row);
            }
        }

        public List<String> getData() {
            return new ArrayList<>(data);
        }

        public void clear() {
            int size = data.size();
            if (size > 0) {
                data.clear();
                fireTableRowsDeleted(0, size - 1);
            }
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return "Model 名称";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data.get(rowIndex);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            data.set(rowIndex, (String) aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
