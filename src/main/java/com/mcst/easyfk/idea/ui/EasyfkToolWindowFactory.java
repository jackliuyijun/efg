package com.mcst.easyfk.idea.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EasyfkToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel panel = createPanel(project);
        Content content = ContentFactory.getInstance().createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private JPanel createPanel(Project project) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("EasyFK Generator");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        buttonPanel.add(titleLabel);

        buttonPanel.add(createSectionLabel("新建项目"));
        buttonPanel.add(createActionButton("全量生成 (项目骨架 + 全部代码)", "EasyFK.NewProject", project));
        buttonPanel.add(Box.createVerticalStrut(12));

        buttonPanel.add(createSectionLabel("增量生成 (已有项目)"));
        buttonPanel.add(createActionButton("增量生成 (新增表: Entity + 业务代码)", "EasyFK.IncrementalGenerate", project));
        buttonPanel.add(createActionButton("刷新模型 (字段变更: Entity + DTO/Param)", "EasyFK.RefreshModel", project));
        buttonPanel.add(Box.createVerticalStrut(12));

        buttonPanel.add(createSectionLabel("单独生成"));
        buttonPanel.add(createActionButton("仅生成业务代码", "EasyFK.GenerateCode", project));
        buttonPanel.add(createActionButton("仅刷新 DTO / Param", "EasyFK.RefreshDto", project));
        buttonPanel.add(createActionButton("生成自动装配配置", "EasyFK.GenerateConfig", project));

        root.add(buttonPanel, BorderLayout.NORTH);
        return root;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 6, 0));
        return label;
    }

    private JButton createActionButton(String text, String actionId, Project project) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(4, 8, 4, 8));

        button.addActionListener(e -> {
            AnAction action = ActionManager.getInstance().getAction(actionId);
            if (action != null) {
                DataContext dataContext = CustomizedDataContext.withSnapshot(DataContext.EMPTY_CONTEXT,
                        sink -> sink.set(CommonDataKeys.PROJECT, project));
                AnActionEvent event = AnActionEvent.createEvent(dataContext,
                        action.getTemplatePresentation().clone(),
                        ActionPlaces.TOOLWINDOW_CONTENT, ActionUiKind.NONE, null);
                action.actionPerformed(event);
            }
        });

        return button;
    }
}
