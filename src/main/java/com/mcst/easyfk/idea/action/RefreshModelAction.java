package com.mcst.easyfk.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.mcst.easyfk.idea.ui.GenerateMode;
import com.mcst.easyfk.idea.ui.GeneratorDialog;
import org.jetbrains.annotations.NotNull;

public class RefreshModelAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new GeneratorDialog(e.getProject(), GenerateMode.MODEL_REFRESH).show();
    }
}
