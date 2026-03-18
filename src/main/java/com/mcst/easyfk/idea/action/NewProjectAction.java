package com.mcst.easyfk.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.mcst.easyfk.idea.ui.GenerateMode;
import com.mcst.easyfk.idea.ui.GeneratorDialog;
import org.jetbrains.annotations.NotNull;

public class NewProjectAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new GeneratorDialog(e.getProject(), GenerateMode.FULL).show();
    }
}
