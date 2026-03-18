package com.mcst.easyfk.idea.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EasyfkSettingsPanel implements Configurable {

    private JBTextField authorField;
    private JBTextField frameworkVersionField;
    private JBCheckBox refreshAfterGenerateCheck;
    private JBCheckBox showResultDialogCheck;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "EasyFK Generator";
    }

    @Override
    public @Nullable JComponent createComponent() {
        authorField = new JBTextField();
        frameworkVersionField = new JBTextField();
        refreshAfterGenerateCheck = new JBCheckBox("生成后自动刷新项目树");
        showResultDialogCheck = new JBCheckBox("生成后弹出结果统计");

        reset();

        return FormBuilder.createFormBuilder()
                .addLabeledComponent("默认作者:", authorField)
                .addLabeledComponent("默认框架版本:", frameworkVersionField)
                .addComponent(refreshAfterGenerateCheck)
                .addComponent(showResultDialogCheck)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @Override
    public boolean isModified() {
        EasyfkSettings settings = EasyfkSettingsState.getInstance().getState();
        if (settings == null) return false;
        return !authorField.getText().equals(settings.defaultAuthor)
                || !frameworkVersionField.getText().equals(settings.defaultFrameworkVersion)
                || refreshAfterGenerateCheck.isSelected() != settings.refreshAfterGenerate
                || showResultDialogCheck.isSelected() != settings.showResultDialog;
    }

    @Override
    public void apply() {
        EasyfkSettings settings = EasyfkSettingsState.getInstance().getState();
        if (settings == null) return;
        settings.defaultAuthor = authorField.getText();
        settings.defaultFrameworkVersion = frameworkVersionField.getText();
        settings.refreshAfterGenerate = refreshAfterGenerateCheck.isSelected();
        settings.showResultDialog = showResultDialogCheck.isSelected();
    }

    @Override
    public void reset() {
        EasyfkSettings settings = EasyfkSettingsState.getInstance().getState();
        if (settings == null) return;
        authorField.setText(settings.defaultAuthor);
        frameworkVersionField.setText(settings.defaultFrameworkVersion);
        refreshAfterGenerateCheck.setSelected(settings.refreshAfterGenerate);
        showResultDialogCheck.setSelected(settings.showResultDialog);
    }
}
