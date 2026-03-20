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
import com.mcst.easyfk.idea.config.GeneratorConfig;
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
        CodeProperties cp = collectCodeProperties();
        if (cp == null) return;

        boolean dtoOnly = codeConfigPanel.isDtoOnly();
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
        CodeProperties cp = collectCodeProperties();
        if (cp == null) return;

        boolean dtoOnly = codeConfigPanel.isDtoOnly();

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
        String fromDbTables = modelConfigPanel.getFromDbTables();
        if (!fromDbTables.isEmpty()) {
            cp.setFromDbTables(fromDbTables);
        }

        if (codeConfigPanel.isDtoOnly()) {
            List<ModelInfo> dtoModelList = codeConfigPanel.getDtoUpdateModelList();
            if (!dtoModelList.isEmpty()) {
                cp.setModelList(dtoModelList);
            }
        } else if (!modelConfigPanel.isFromDbMode()) {
            List<ModelInfo> modelList = modelConfigPanel.getModelList();
            if (!modelList.isEmpty()) {
                cp.setModelList(modelList);
            }
        }
        return cp;
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
            GeneratorConfig config = GeneratorConfigUtil.load(project.getBasePath());
            if (config != null) {
                applyConfig(config);
            }
        }
    }

    private void loadGlobalDefaults() {
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

        modelConfigPanel.loadDbSettings(s.lastDbShortUrl, s.lastDbUser, s.lastTablePrefix);
    }

    private void applyConfig(GeneratorConfig config) {
        if (config.getProject() != null) {
            GeneratorConfig.ProjectConfig pc = config.getProject();
            ProjectProperties pp = new ProjectProperties();
            pp.setProjectName(pc.getProjectName());
            pp.setGroupId(pc.getGroupId());
            pp.setBasePackage(pc.getBasePackage());
            pp.setProjectDir(pc.getProjectDir());
            pp.setFrameworkVersion(pc.getFrameworkVersion());
            pp.setProjectVersion(pc.getProjectVersion());
            pp.setProjectType(safeEnum(ProjectType.class, pc.getProjectType()));
            pp.setBuildType(safeEnum(BuildType.class, pc.getBuildType()));
            pp.setGradleType(safeEnum(GradleType.class, pc.getGradleType()));
            pp.setOrmType(safeEnum(OrmType.class, pc.getOrmType()));
            pp.setRpcType(safeEnum(RpcType.class, pc.getRpcType()));
            pp.setPrdType(safeEnum(PrdType.class, pc.getPrdType()));
            pp.setAppType(safeEnum(AppType.class, pc.getAppType()));
            pp.setLogType(safeEnum(LogType.class, pc.getLogType()));
            if (pc.getIncludeAuth() != null) {
                pp.setIncludeAuth(pc.getIncludeAuth());
            }
            projectConfigPanel.loadFrom(pp);
        }

        if (config.getCode() != null) {
            GeneratorConfig.CodeConfig cc = config.getCode();
            CodeProperties cp = new CodeProperties();
            cp.setModuleName(cc.getModuleName());
            cp.setAuthor(cc.getAuthor());
            if (cc.getSpringAnnotation() != null) cp.setSpringAnnotation(cc.getSpringAnnotation());
            if (cc.getExtendsSupperClass() != null) cp.setExtendsSupperClass(cc.getExtendsSupperClass());
            if (cc.getCreateResourceAnnotation() != null) cp.setCreateResourceAnnotation(cc.getCreateResourceAnnotation());
            codeConfigPanel.loadFrom(cp);
        }

        if (config.getDb() != null) {
            GeneratorConfig.DbConfig dc = config.getDb();
            modelConfigPanel.loadDbSettings(dc.getDbShortUrl(), dc.getDbUser(), dc.getTablePrefix());
            DbType dbType = safeEnum(DbType.class, dc.getDbType());
            if (dbType != null) modelConfigPanel.setDbType(dbType);
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
        GeneratorConfig config = new GeneratorConfig();

        GeneratorConfig.ProjectConfig pc = new GeneratorConfig.ProjectConfig();
        pc.setProjectName(pp.getProjectName());
        pc.setGroupId(pp.getGroupId());
        pc.setBasePackage(pp.getBasePackage());
        pc.setProjectDir(pp.getProjectDir());
        pc.setFrameworkVersion(pp.getFrameworkVersion());
        pc.setProjectVersion(pp.getProjectVersion());
        if (pp.getProjectType() != null) pc.setProjectType(pp.getProjectType().name());
        if (pp.getBuildType() != null) pc.setBuildType(pp.getBuildType().name());
        if (pp.getGradleType() != null) pc.setGradleType(pp.getGradleType().name());
        if (pp.getOrmType() != null) pc.setOrmType(pp.getOrmType().name());
        if (pp.getRpcType() != null) pc.setRpcType(pp.getRpcType().name());
        if (pp.getPrdType() != null) pc.setPrdType(pp.getPrdType().name());
        if (pp.getAppType() != null) pc.setAppType(pp.getAppType().name());
        if (pp.getLogType() != null) pc.setLogType(pp.getLogType().name());
        if (pp.getIncludeAuth() != null) {
            pc.setIncludeAuth(pp.getIncludeAuth());
        }
        config.setProject(pc);

        GeneratorConfig.CodeConfig cc = new GeneratorConfig.CodeConfig();
        cc.setModuleName(cp.getModuleName());
        cc.setAuthor(cp.getAuthor());
        cc.setSpringAnnotation(cp.getSpringAnnotation());
        cc.setExtendsSupperClass(cp.getExtendsSupperClass());
        cc.setCreateResourceAnnotation(cp.getCreateResourceAnnotation());
        config.setCode(cc);

        GeneratorConfig.DbConfig dc = new GeneratorConfig.DbConfig();
        dc.setDbType(modelConfigPanel.getSelectedDbType() != null ? modelConfigPanel.getSelectedDbType().name() : null);
        dc.setDbShortUrl(modelConfigPanel.getDbShortUrl());
        dc.setDbUser(modelConfigPanel.getDbUser());
        dc.setTablePrefix(modelConfigPanel.getTablePrefix());
        config.setDb(dc);

        GeneratorConfigUtil.save(fullProjectPath, config);
    }

    // ============ 重置配置 ============

    private void doResetConfig() {
        projectConfigPanel.resetToDefaults();
        codeConfigPanel.resetToDefaults();
        modelConfigPanel.resetDbSettings();
        tabbedPane.setSelectedIndex(0);
    }

    // ============ 手动加载配置文件 ============

    private void doLoadConfigFromFile() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                .withFileFilter(file -> GeneratorConfigUtil.CONFIG_FILE_NAME.equals(file.getName()))
                .withTitle("选择配置文件")
                .withDescription("选择 " + GeneratorConfigUtil.CONFIG_FILE_NAME + " 配置文件");

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

        GeneratorConfig config = GeneratorConfigUtil.loadFromFile(files[0].getPath());
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

    // ============ 工具方法 ============

    private static <T extends Enum<T>> T safeEnum(Class<T> clazz, String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return Enum.valueOf(clazz, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
