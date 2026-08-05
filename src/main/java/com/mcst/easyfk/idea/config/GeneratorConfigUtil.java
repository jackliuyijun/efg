package com.mcst.easyfk.idea.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.mcst.easyfk.generator.cli.CliConfigLoader;
import com.mcst.easyfk.generator.enums.OrmType;
import com.mcst.easyfk.generator.properties.CodeProperties;
import com.mcst.easyfk.generator.properties.ProjectProperties;
import com.mcst.easyfk.generator.vo.ModelInfo;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GeneratorConfigUtil {

    private static final Logger LOG = Logger.getInstance(GeneratorConfigUtil.class);
    public static final String CONFIG_FILE_NAME = "generator.yml";
    public static final String LEGACY_CONFIG_FILE_NAME = ".easyfk-generator.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private GeneratorConfigUtil() {}

    /**
     * 将配置保存到 {projectDir}/generator.yml
     */
    public static void save(String projectDir, ProjectProperties projectProperties, CodeProperties codeProperties) {
        save(projectDir, projectProperties, codeProperties, null);
    }

    public static void save(String projectDir, ProjectProperties projectProperties, CodeProperties codeProperties, Map<String, Object> baseRoot) {
        if (projectDir == null || projectDir.isEmpty() || projectProperties == null || codeProperties == null) return;
        try {
            Path dir = Paths.get(projectDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path file = dir.resolve(CONFIG_FILE_NAME);
            Map<String, Object> root = Files.exists(file) ? readYamlRoot(file) : copyRoot(baseRoot);
            Map<String, Object> generator = ensureGeneratorMap(root);
            updateProjectMap(getOrCreateMap(generator, "project"), projectProperties);
            updateCodeMap(getOrCreateMap(generator, "code"), codeProperties);
            Files.writeString(file, dumpYaml(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.warn("Failed to save generator config: " + e.getMessage(), e);
        }
    }

    /**
     * 从指定目录加载配置，目录下不存在配置文件则返回 null
     */
    public static LoadedConfig load(String projectDir) {
        if (projectDir == null || projectDir.isEmpty()) return null;
        Path file = Paths.get(projectDir, CONFIG_FILE_NAME);
        LoadedConfig config = readYamlFile(file);
        if (config != null) {
            return config;
        }
        return readLegacyFile(Paths.get(projectDir, LEGACY_CONFIG_FILE_NAME));
    }

    /**
     * 从指定文件路径加载配置
     */
    public static LoadedConfig loadFromFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return null;
        Path file = Paths.get(filePath);
        if (file.getFileName() != null && LEGACY_CONFIG_FILE_NAME.equalsIgnoreCase(file.getFileName().toString())) {
            return readLegacyFile(file);
        }
        LoadedConfig config = readYamlFile(file);
        if (config != null) {
            return config;
        }
        return readLegacyFile(file);
    }

    /**
     * 检查指定目录下是否存在配置文件
     */
    public static boolean exists(String projectDir) {
        if (projectDir == null || projectDir.isEmpty()) return false;
        return Files.exists(Paths.get(projectDir, CONFIG_FILE_NAME))
                || Files.exists(Paths.get(projectDir, LEGACY_CONFIG_FILE_NAME));
    }

    public static boolean isSupportedConfigFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) return false;
        String lowerName = fileName.toLowerCase();
        return CONFIG_FILE_NAME.equalsIgnoreCase(fileName)
                || LEGACY_CONFIG_FILE_NAME.equalsIgnoreCase(fileName)
                || lowerName.endsWith(".yml")
                || lowerName.endsWith(".yaml");
    }

    private static LoadedConfig readYamlFile(Path file) {
        if (!Files.exists(file)) return null;
        try {
            Map<String, Object> rawRoot = readYamlRoot(file);
            CliConfigLoader loader = new CliConfigLoader(file.toString());
            return new LoadedConfig(loader.getProjectProperties(), loader.getCodeProperties(), rawRoot);
        } catch (Exception e) {
            LOG.warn("Failed to load generator config from " + file + ": " + e.getMessage(), e);
            return null;
        }
    }

    private static LoadedConfig readLegacyFile(Path file) {
        if (!Files.exists(file)) return null;
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            GeneratorConfig config = GSON.fromJson(json, GeneratorConfig.class);
            if (config == null) return null;
            return GeneratorConfigMapper.fromLegacyConfig(config);
        } catch (Exception e) {
            LOG.warn("Failed to load legacy generator config from " + file + ": " + e.getMessage(), e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readYamlRoot(Path file) {
        if (!Files.exists(file)) {
            return new LinkedHashMap<>();
        }
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            Object loaded = new Yaml().load(content);
            if (loaded instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }
        } catch (Exception e) {
            LOG.warn("Failed to parse yaml config from " + file + ": " + e.getMessage(), e);
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> copyRoot(Map<String, Object> root) {
        if (root == null || root.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Yaml yaml = new Yaml();
        Object copied = yaml.load(yaml.dump(root));
        if (copied instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new LinkedHashMap<>();
    }

    private static Map<String, Object> ensureGeneratorMap(Map<String, Object> root) {
        Map<String, Object> easyfk = getOrCreateMap(root, "easyfk");
        Map<String, Object> config = getOrCreateMap(easyfk, "config");
        return getOrCreateMap(config, "generator");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
        Object current = parent.get(key);
        if (current instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        Map<String, Object> child = new LinkedHashMap<>();
        parent.put(key, child);
        return child;
    }

    private static void updateProjectMap(Map<String, Object> project, ProjectProperties pp) {
        putOrRemove(project, "project-name", pp.getProjectName());
        putOrRemove(project, "project-dir", pp.getProjectDir());
        putOrRemove(project, "group-id", pp.getGroupId());
        putOrRemove(project, "base-package", pp.getBasePackage());
        putOrRemove(project, "framework-version", pp.getFrameworkVersion());
        putOrRemove(project, "project-version", pp.getProjectVersion());
        putEnumOrRemove(project, "project-type", pp.getProjectType());
        putEnumOrRemove(project, "build-type", pp.getBuildType());
        putEnumOrRemove(project, "gradle-type", pp.getGradleType());
        putEnumOrRemove(project, "orm-type", pp.getOrmType());
        putOrmModulesOrRemove(project, pp.getOrmModules());
        putEnumOrRemove(project, "rpc-type", pp.getRpcType());
        putEnumOrRemove(project, "prd-type", pp.getPrdType());
        putEnumOrRemove(project, "app-type", pp.getAppType());
        putEnumOrRemove(project, "log-type", pp.getLogType());
        putOrRemove(project, "include-auth", pp.getIncludeAuth());
    }

    private static void updateCodeMap(Map<String, Object> code, CodeProperties cp) {
        putOrRemove(code, "module-name", cp.getModuleName());
        putOrRemove(code, "author", cp.getAuthor());
        putOrRemove(code, "spring-annotation", cp.getSpringAnnotation());
        putOrRemove(code, "extends-supper-class", cp.getExtendsSupperClass());
        putOrRemove(code, "create-resource-annotation", cp.getCreateResourceAnnotation());
        putEnumOrRemove(code, "db-type", cp.getDbType());
        putOrRemove(code, "db-short-url", cp.getDbShortUrl());
        putOrRemove(code, "db-user", cp.getDbUser());
        putOrRemove(code, "db-pwd", cp.getDbPwd());
        putOrRemove(code, "table-prefix", cp.getTablePrefix());
        putOrRemove(code, "db-tables", cp.getDbTables());
        updateModelList(code, cp.getModelList());
    }

    private static void updateModelList(Map<String, Object> code, List<ModelInfo> modelList) {
        if (modelList == null || modelList.isEmpty()) {
            code.remove("model-list");
            return;
        }
        List<Map<String, Object>> yamlModelList = new ArrayList<>();
        for (ModelInfo modelInfo : modelList) {
            Map<String, Object> item = new LinkedHashMap<>();
            putOrRemove(item, "model-name", modelInfo.getModelName());
            putOrRemove(item, "model-desc", modelInfo.getModelDesc());
            putOrRemove(item, "table-name", modelInfo.getTableName());
            putOrRemove(item, "id-type", modelInfo.getIdType());
            putOrRemove(item, "only-repository", modelInfo.getOnlyRepository());
            putOrRemove(item, "create-controller", modelInfo.getCreateController());
            yamlModelList.add(item);
        }
        code.put("model-list", yamlModelList);
    }

    private static void putEnumOrRemove(Map<String, Object> map, String key, Enum<?> value) {
        if (value == null) {
            map.remove(key);
            return;
        }
        map.put(key, value.name().toLowerCase());
    }

    private static void putOrmModulesOrRemove(Map<String, Object> map, List<OrmType> ormModules) {
        if (ormModules == null || ormModules.isEmpty()) {
            map.remove("orm-modules");
            return;
        }
        List<String> names = ormModules.stream()
                .map(ormType -> ormType.name().toLowerCase())
                .toList();
        map.put("orm-modules", names);
    }

    private static void putOrRemove(Map<String, Object> map, String key, Object value) {
        if (value == null) {
            map.remove(key);
            return;
        }
        if (value instanceof String str && str.isBlank()) {
            map.remove(key);
            return;
        }
        map.put(key, value);
    }

    private static String dumpYaml(Map<String, Object> root) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setIndicatorIndent(1);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        Yaml yaml = new Yaml(options);
        StringWriter writer = new StringWriter();
        yaml.dump(root, writer);
        return writer.toString();
    }

    public static final class LoadedConfig {
        private final ProjectProperties projectProperties;
        private final CodeProperties codeProperties;
        private final Map<String, Object> rawRoot;

        public LoadedConfig(ProjectProperties projectProperties, CodeProperties codeProperties, Map<String, Object> rawRoot) {
            this.projectProperties = projectProperties;
            this.codeProperties = codeProperties;
            this.rawRoot = rawRoot;
        }

        public ProjectProperties getProjectProperties() {
            return projectProperties;
        }

        public CodeProperties getCodeProperties() {
            return codeProperties;
        }

        public Map<String, Object> getRawRoot() {
            return rawRoot;
        }
    }
}
