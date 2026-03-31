package com.mcst.easyfk.idea.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.mcst.easyfk.generator.EasyfkGenerator;
import com.mcst.easyfk.generator.enums.*;
import com.mcst.easyfk.generator.properties.CodeProperties;
import com.mcst.easyfk.generator.properties.ProjectProperties;
import com.mcst.easyfk.generator.vo.ModelInfo;
import com.mcst.easyfk.idea.config.GeneratorConfigUtil;
import com.mcst.easyfk.idea.settings.EasyfkSettings;
import com.mcst.easyfk.idea.settings.EasyfkSettingsState;
import com.mcst.easyfk.idea.util.IdeaFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GeneratorDialog extends DialogWrapper {

    private final GenerateMode mode;
    private final ProjectConfigPanel projectConfigPanel;
    private final CodeConfigPanel codeConfigPanel;
    private final ModelConfigPanel modelConfigPanel;
    private final JTabbedPane tabbedPane;
    private final @Nullable Project ideProject;
    private final List<Action> generateActions = new ArrayList<>();
    private GeneratorConfigUtil.LoadedConfig currentLoadedConfig;
    private Action genModelAction;
    private Action genConfigAction;

    public GeneratorDialog(@Nullable Project project, GenerateMode mode) {
        super(project, true);
        this.mode = mode;
        this.ideProject = project;

        projectConfigPanel = new ProjectConfigPanel();
        codeConfigPanel = new CodeConfigPanel();
        modelConfigPanel = new ModelConfigPanel();
        modelConfigPanel.setProject(project);

        tabbedPane = new JTabbedPane();

        setupTabs();
        loadSettings(project);
        setupOrmTypeListener();
        setupProjectTypeListener();

        setTitle(getDialogTitle());
        setOKButtonText("关闭");
        init();
    }

    private void setupTabs() {
        tabbedPane.addTab("项目配置", projectConfigPanel.getPanel());
        tabbedPane.addTab("模型配置", modelConfigPanel.getPanel());
        tabbedPane.addTab("代码配置", codeConfigPanel.getPanel());

        switch (mode) {
            case MODEL_REFRESH, DTO_ONLY -> tabbedPane.setSelectedIndex(1);
            case CODE_ONLY -> tabbedPane.setSelectedIndex(2);
            default -> {}
        }
    }

    private void setupOrmTypeListener() {
    }

    private void setupProjectTypeListener() {
        projectConfigPanel.addProjectTypeChangeListener(() -> {
            updateConfigActionEnabled();
            updateGenerateActionsEnabled();
        });
        updateConfigActionEnabled();
        updateGenerateActionsEnabled();
    }

    private void updateConfigActionEnabled() {
        if (genConfigAction != null) {
            genConfigAction.setEnabled(projectConfigPanel.isSmartProjectType());
        }
    }

    private void updateGenerateActionsEnabled() {
        if (genModelAction != null) {
            genModelAction.setEnabled(!projectConfigPanel.isMicroPrdProjectType());
        }
    }

    private String getDialogTitle() {
        return "EasyFK Generator";
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JButton loadConfigBtn = new JButton("加载配置...");
        loadConfigBtn.addActionListener(e -> doLoadConfigFromFile());
        JButton resetConfigBtn = new JButton("重置配置");
        resetConfigBtn.addActionListener(e -> doResetConfig());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(resetConfigBtn);
        btnPanel.add(loadConfigBtn);

        JLayeredPane layered = new JLayeredPane() {
            @Override
            public void doLayout() {
                int w = getWidth();
                int h = getHeight();
                tabbedPane.setBounds(0, 0, w, h);
                Dimension btnSize = btnPanel.getPreferredSize();
                btnPanel.setBounds(w - btnSize.width - 2, 2, btnSize.width, btnSize.height);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(780, 480);
            }
        };
        layered.add(tabbedPane, JLayeredPane.DEFAULT_LAYER);
        layered.add(btnPanel, JLayeredPane.PALETTE_LAYER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(layered, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(780, 480));
        return wrapper;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{getOKAction()};
    }

    @Override
    protected void doOKAction() {
        saveConfigAndSettings();
        super.doOKAction();
    }

    @Override
    protected Action @NotNull [] createLeftSideActions() {
        Action genProjectAction = new AbstractAction("生成项目") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doGenerateProject();
            }
        };
        genModelAction = new AbstractAction("生成模型") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doGenerateModel();
            }
        };
        Action genCodeAction = new AbstractAction("生成业务代码") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doGenerateCode();
            }
        };
        genConfigAction = new AbstractAction("生成自动装配") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doGenerateConfig();
            }
        };
        Action genAllAction = new AbstractAction("生成全部") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doGenerateAll();
            }
        };
        Action[] actions = {genProjectAction, genModelAction, genCodeAction, genConfigAction, genAllAction};
        generateActions.clear();
        for (Action a : actions) {
            generateActions.add(a);
        }
        updateConfigActionEnabled();
        return actions;
    }

    // ============ 全部生成 ============

    private void doGenerateAll() {
        ProjectProperties pp = collectProjectProperties();
        if (pp == null) return;
        boolean dtoOnly = codeConfigPanel.isDtoOnly();
        if (!dtoOnly && !validateModelConfig(true)) return;
        CodeProperties cp = collectCodeProperties();
        if (cp == null) return;
        boolean isMicroPrd = ProjectType.MICRO_PRD.equals(pp.getProjectType());

        runInBackground("EasyFK 生成全部...", indicator -> {
            EasyfkGenerator generator = new EasyfkGenerator(pp, cp);

            indicator.setText("步骤 1/4: 生成项目骨架...");
            generator.generateProject();

            if (!isMicroPrd) {
                indicator.setText("步骤 2/4: 生成 Entity + Mapper...");
                generator.generateModel();
            }

            indicator.setText("步骤 3/4: 生成业务代码...");
            if (dtoOnly) {
                generator.updateDtoAndParam();
            } else {
                generator.generateCode();
            }

            if (!isMicroPrd) {
                indicator.setText("步骤 4/4: 生成自动装配配置...");
                generator.generateConfig();
            }

            refreshAndSaveConfig(pp);
            return "全部生成完成！\n路径: " + pp.getProjectDir() + File.separator + pp.getProjectName();
        });
    }

    // ============ 分步生成 ============

    private void doGenerateProject() {
        ProjectProperties pp = collectProjectProperties();
        if (pp == null) return;
        CodeProperties cp = collectCodeProperties(false);
        if (cp == null) return;

        runInBackground("EasyFK 生成项目骨架...", indicator -> {
            EasyfkGenerator generator = new EasyfkGenerator(pp, cp);
            indicator.setText("生成项目骨架...");
            generator.generateProject();
            refreshAndSaveConfig(pp);
            return "项目骨架生成成功！\n路径: " + pp.getProjectDir() + File.separator + pp.getProjectName();
        });
    }

    private void doGenerateModel() {
        ProjectProperties pp = collectProjectProperties();
        if (pp == null) return;
        if (!validateModelConfig(true)) return;
        CodeProperties cp = collectCodeProperties(false);
        if (cp == null) return;

        runInBackground("EasyFK 生成模型...", indicator -> {
            EasyfkGenerator generator = new EasyfkGenerator(pp, cp);
            indicator.setText("生成 Entity + Mapper...");
            generator.generateModel();
            refreshAndSaveConfig(pp);
            return "Entity/Mapper 模型生成成功！";
        });
    }

    private void doGenerateCode() {
        ProjectProperties pp = collectProjectProperties();
        if (pp == null) return;
        boolean dtoOnly = codeConfigPanel.isDtoOnly();
        if (!dtoOnly && !validateModelConfig(true)) return;
        CodeProperties cp = collectCodeProperties();
        if (cp == null) return;

        runInBackground("EasyFK 生成业务代码...", indicator -> {
            EasyfkGenerator generator = new EasyfkGenerator(pp, cp);
            if (dtoOnly) {
                indicator.setText("仅刷新 DTO 和 Param...");
                generator.updateDtoAndParam();
                refreshAndSaveConfig(pp);
                return "DTO/Param 刷新成功！";
            } else {
                indicator.setText("生成业务代码...");
                generator.generateCode();
                refreshAndSaveConfig(pp);
                return "业务代码生成成功！";
            }
        });
    }

    private void doGenerateConfig() {
        ProjectProperties pp = collectProjectProperties();
        if (pp == null) return;
        CodeProperties cp = collectCodeProperties();
        if (cp == null) return;

        runInBackground("EasyFK 生成自动装配...", indicator -> {
            EasyfkGenerator generator = new EasyfkGenerator(pp, cp);
            indicator.setText("生成自动装配配置...");
            generator.generateConfig();
            refreshAndSaveConfig(pp);
            return "自动装配配置生成成功！";
        });
    }

    // ============ 收集属性 ============

    private @Nullable ProjectProperties collectProjectProperties() {
        String projectError = projectConfigPanel.validateInput();
        if (projectError != null) {
            Messages.showErrorDialog(projectError, "项目配置校验失败");
            tabbedPane.setSelectedIndex(0);
            return null;
        }
        ProjectProperties pp = projectConfigPanel.toProperties();
        pp.setDbType(modelConfigPanel.getSelectedDbType());
        return pp;
    }

    private @Nullable CodeProperties collectCodeProperties() {
        return collectCodeProperties(true);
    }

    private @Nullable CodeProperties collectCodeProperties(boolean validate) {
        if (validate) {
            int codeTabIndex = tabbedPane.indexOfTab("代码配置");
            if (codeTabIndex >= 0) {
                String codeError = codeConfigPanel.validateInput();
                if (codeError != null) {
                    Messages.showErrorDialog(codeError, "代码配置校验失败");
                    tabbedPane.setSelectedIndex(codeTabIndex);
                    return null;
                }
            }
        }

        CodeProperties cp = codeConfigPanel.toProperties();
        cp.setDbType(modelConfigPanel.getSelectedDbType());
        cp.setDbShortUrl(modelConfigPanel.getDbShortUrl());
        cp.setDbUser(modelConfigPanel.getDbUser());
        cp.setDbPwd(modelConfigPanel.getDbPwd());
        cp.setTablePrefix(modelConfigPanel.getTablePrefix());
        String dbTables = modelConfigPanel.getDbTables();
        if (!dbTables.isEmpty()) {
            cp.setDbTables(dbTables);
        }

        if (codeConfigPanel.isDtoOnly()) {
            List<ModelInfo> dtoModelList = codeConfigPanel.getDtoUpdateModelList();
            if (!dtoModelList.isEmpty()) {
                cp.setModelList(dtoModelList);
            }
        } else {
            List<ModelInfo> modelList = modelConfigPanel.getModelList();
            if (!modelList.isEmpty()) {
                cp.setModelList(modelList);
            }
        }
        return cp;
    }

    private boolean validateModelConfig(boolean requireModelSource) {
        int modelTabIndex = tabbedPane.indexOfTab("模型配置");
        String modelError = modelConfigPanel.validateInput(requireModelSource);
        if (modelError != null) {
            Messages.showErrorDialog(modelError, "模型配置校验失败");
            if (modelTabIndex >= 0) {
                tabbedPane.setSelectedIndex(modelTabIndex);
            }
            return false;
        }
        return true;
    }

    // ============ 后台执行 ============

    @FunctionalInterface
    private interface GenerateTask {
        String run(ProgressIndicator indicator) throws Exception;
    }

    private static final long MIN_DISPLAY_MS = 2000;

    private void runInBackground(String title, GenerateTask task) {
        setGenerateActionsEnabled(false);
        final String[] resultMsg = {null};
        final String[] errorMsg = {null};
        ProgressManager.getInstance().run(new Task.Modal(ideProject, title, true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                long start = System.currentTimeMillis();
                try {
                    resultMsg[0] = task.run(indicator);
                } catch (Exception ex) {
                    errorMsg[0] = "生成失败: " + ex.getMessage();
                }
                indicator.setText("正在生成中.....");
                ensureMinDisplay(start);
            }
        });
        setGenerateActionsEnabled(true);
        if (errorMsg[0] != null) {
            Messages.showErrorDialog(errorMsg[0], "EasyFK Generator");
        } else if (resultMsg[0] != null) {
            Messages.showInfoMessage(resultMsg[0], "EasyFK Generator");
        }
    }

    private static void ensureMinDisplay(long stepStart) {
        long remaining = MIN_DISPLAY_MS - (System.currentTimeMillis() - stepStart);
        if (remaining > 0) {
            try { Thread.sleep(remaining); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
    }

    private void setGenerateActionsEnabled(boolean enabled) {
        for (Action a : generateActions) {
            a.setEnabled(enabled);
        }
        if (enabled) {
            updateGenerateActionsEnabled();
            updateConfigActionEnabled();
        }
    }

    private void refreshAndSaveConfig(ProjectProperties pp) {
        EasyfkSettings settings = EasyfkSettingsState.getInstance().getState();
        if (settings == null || settings.refreshAfterGenerate) {
            IdeaFileUtil.refreshProjectDir(pp.getProjectDir(), pp.getProjectName());
        }
        saveConfigFile(pp);
    }

    // ============ 配置加载 ============

    private void loadSettings(@Nullable Project project) {
        loadGlobalDefaults();

        if (project != null && project.getBasePath() != null) {
            GeneratorConfigUtil.LoadedConfig config = GeneratorConfigUtil.load(project.getBasePath());
            if (config != null) {
                applyConfig(config);
            }
        }
    }

    private void loadGlobalDefaults() {
        currentLoadedConfig = null;
        EasyfkSettings s = EasyfkSettingsState.getInstance().getState();
        if (s == null) return;

        ProjectProperties pp = new ProjectProperties();
        if (!s.lastProjectDir.isEmpty()) pp.setProjectDir(s.lastProjectDir);
        if (!s.lastGroupId.isEmpty()) pp.setGroupId(s.lastGroupId);
        if (!s.lastBasePackage.isEmpty()) pp.setBasePackage(s.lastBasePackage);
        pp.setFrameworkVersion(s.defaultFrameworkVersion);
        pp.setProjectType(null);
        pp.setPrdType(null);
        pp.setAppType(null);
        projectConfigPanel.loadFrom(pp);

        CodeProperties cp = new CodeProperties();
        cp.setAuthor(s.defaultAuthor);
        if (!s.lastModuleName.isEmpty()) cp.setModuleName(s.lastModuleName);
        codeConfigPanel.loadFrom(cp);

        modelConfigPanel.loadDbSettings(s.lastDbShortUrl, s.lastDbUser, null, s.lastTablePrefix);
    }

    private void applyConfig(GeneratorConfigUtil.LoadedConfig config) {
        currentLoadedConfig = config;
        ProjectProperties pp = config.getProjectProperties();
        if (pp != null) {
            projectConfigPanel.loadFrom(pp);
        }

        CodeProperties cp = config.getCodeProperties();
        if (cp != null) {
            codeConfigPanel.loadFrom(cp);
            modelConfigPanel.loadDbSettings(cp.getDbShortUrl(), cp.getDbUser(), cp.getDbPwd(), cp.getTablePrefix());
            modelConfigPanel.setDbTables(cp.getDbTables());
            if (cp.getDbType() != null) modelConfigPanel.setDbType(cp.getDbType());
            modelConfigPanel.setModelList(cp.getModelList());
        }
    }

    // ============ 配置保存 ============

    private void saveConfigAndSettings() {
        ProjectProperties pp = projectConfigPanel.toProperties();
        saveLastSettings(pp);
        saveConfigFile(pp);
    }

    private void saveLastSettings(ProjectProperties pp) {
        EasyfkSettings s = EasyfkSettingsState.getInstance().getState();
        if (s == null) return;

        if (pp != null) {
            s.lastProjectDir = pp.getProjectDir() != null ? pp.getProjectDir() : "";
            s.lastGroupId = pp.getGroupId() != null ? pp.getGroupId() : "";
            s.lastBasePackage = pp.getBasePackage() != null ? pp.getBasePackage() : "";
        }
        CodeProperties cp = codeConfigPanel.toProperties();
        s.lastModuleName = cp.getModuleName() != null ? cp.getModuleName() : "";
        s.lastDbShortUrl = modelConfigPanel.getDbShortUrl();
        s.lastDbUser = modelConfigPanel.getDbUser();
        s.lastTablePrefix = modelConfigPanel.getTablePrefix();
    }

    private void saveConfigFile(ProjectProperties pp) {
        if (pp == null) return;
        String projectDir = pp.getProjectDir();
        String projectName = pp.getProjectName();
        if (projectDir == null || projectDir.isEmpty()) return;

        String fullProjectPath = projectDir;
        if (projectName != null && !projectName.isEmpty()) {
            fullProjectPath = projectDir + File.separator + projectName;
        }

        CodeProperties cp = codeConfigPanel.toProperties();
        cp.setDbType(modelConfigPanel.getSelectedDbType());
        cp.setDbShortUrl(modelConfigPanel.getDbShortUrl());
        cp.setDbUser(modelConfigPanel.getDbUser());
        cp.setDbPwd(modelConfigPanel.getDbPwd());
        cp.setTablePrefix(modelConfigPanel.getTablePrefix());
        cp.setDbTables(modelConfigPanel.getDbTables());
        cp.setModelList(modelConfigPanel.getModelList());

        GeneratorConfigUtil.save(fullProjectPath, pp, cp, currentLoadedConfig != null ? currentLoadedConfig.getRawRoot() : null);
        currentLoadedConfig = GeneratorConfigUtil.load(fullProjectPath);
    }

    // ============ 重置配置 ============

    private void doResetConfig() {
        currentLoadedConfig = null;
        projectConfigPanel.resetToDefaults();
        codeConfigPanel.resetToDefaults();
        modelConfigPanel.resetDbSettings();
        tabbedPane.setSelectedIndex(0);
    }

    // ============ 手动加载配置文件 ============

    private void doLoadConfigFromFile() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                .withFileFilter(file -> GeneratorConfigUtil.isSupportedConfigFile(file.getName()))
                .withTitle("选择配置文件")
                .withDescription("选择 YAML 配置文件（如 generator.yml）");

        VirtualFile projectRoot = null;
        if (ideProject != null && ideProject.getBasePath() != null) {
            projectRoot = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
                    .findFileByPath(ideProject.getBasePath());
        }

        VirtualFile[] files;
        if (projectRoot != null) {
            files = FileChooserFactory.getInstance()
                    .createFileChooser(descriptor, ideProject, null)
                    .choose(ideProject, projectRoot);
        } else {
            files = FileChooserFactory.getInstance()
                    .createFileChooser(descriptor, ideProject, null)
                    .choose(ideProject);
        }

        if (files.length == 0) return;

        GeneratorConfigUtil.LoadedConfig config = GeneratorConfigUtil.loadFromFile(files[0].getPath());
        if (config != null) {
            applyConfig(config);
        } else {
            Messages.showWarningDialog("配置文件解析失败，请检查文件格式", "加载配置");
        }
    }

    // ============ 兼容旧接口 ============

    public ProjectProperties getProjectProperties() {
        return collectProjectProperties();
    }

    public CodeProperties getCodeProperties() {
        return collectCodeProperties();
    }

    public GenerateMode getMode() {
        return mode;
    }

    public boolean isDtoOnly() {
        return codeConfigPanel.isDtoOnly();
    }

}
