package com.mcst.easyfk.idea.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class GeneratorConfigUtil {

    private static final Logger LOG = Logger.getInstance(GeneratorConfigUtil.class);
    public static final String CONFIG_FILE_NAME = ".easyfk-generator.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private GeneratorConfigUtil() {}

    /**
     * 将配置保存到 {projectDir}/.easyfk-generator.json
     */
    public static void save(String projectDir, GeneratorConfig config) {
        if (projectDir == null || projectDir.isEmpty() || config == null) return;
        try {
            Path dir = Paths.get(projectDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path file = dir.resolve(CONFIG_FILE_NAME);
            String json = GSON.toJson(config);
            Files.writeString(file, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.warn("Failed to save generator config: " + e.getMessage(), e);
        }
    }

    /**
     * 从指定目录加载配置，目录下不存在配置文件则返回 null
     */
    public static GeneratorConfig load(String projectDir) {
        if (projectDir == null || projectDir.isEmpty()) return null;
        Path file = Paths.get(projectDir, CONFIG_FILE_NAME);
        return readFile(file);
    }

    /**
     * 从指定文件路径加载配置
     */
    public static GeneratorConfig loadFromFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return null;
        return readFile(Paths.get(filePath));
    }

    /**
     * 检查指定目录下是否存在配置文件
     */
    public static boolean exists(String projectDir) {
        if (projectDir == null || projectDir.isEmpty()) return false;
        return Files.exists(Paths.get(projectDir, CONFIG_FILE_NAME));
    }

    private static GeneratorConfig readFile(Path file) {
        if (!Files.exists(file)) return null;
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            return GSON.fromJson(json, GeneratorConfig.class);
        } catch (Exception e) {
            LOG.warn("Failed to load generator config from " + file + ": " + e.getMessage(), e);
            return null;
        }
    }
}
