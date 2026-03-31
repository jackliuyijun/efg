package com.mcst.easyfk.idea.config;

import com.mcst.easyfk.generator.enums.*;
import com.mcst.easyfk.generator.properties.CodeProperties;
import com.mcst.easyfk.generator.properties.ProjectProperties;

final class GeneratorConfigMapper {

    private GeneratorConfigMapper() {}

    static GeneratorConfigUtil.LoadedConfig fromLegacyConfig(GeneratorConfig config) {
        ProjectProperties pp = new ProjectProperties();
        CodeProperties cp = new CodeProperties();

        if (config.getProject() != null) {
            GeneratorConfig.ProjectConfig pc = config.getProject();
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
        }

        if (config.getCode() != null) {
            GeneratorConfig.CodeConfig cc = config.getCode();
            cp.setModuleName(cc.getModuleName());
            cp.setAuthor(cc.getAuthor());
            if (cc.getSpringAnnotation() != null) cp.setSpringAnnotation(cc.getSpringAnnotation());
            if (cc.getExtendsSupperClass() != null) cp.setExtendsSupperClass(cc.getExtendsSupperClass());
            if (cc.getCreateResourceAnnotation() != null) cp.setCreateResourceAnnotation(cc.getCreateResourceAnnotation());
            cp.setModelList(cc.getModelList());
        }

        if (config.getDb() != null) {
            GeneratorConfig.DbConfig dc = config.getDb();
            cp.setDbType(safeEnum(DbType.class, dc.getDbType()));
            cp.setDbShortUrl(dc.getDbShortUrl());
            cp.setDbUser(dc.getDbUser());
            cp.setTablePrefix(dc.getTablePrefix());
            cp.setDbTables(dc.getDbTables());
            if (cp.getDbType() != null) {
                pp.setDbType(cp.getDbType());
            }
        }

        return new GeneratorConfigUtil.LoadedConfig(pp, cp, null);
    }

    private static <T extends Enum<T>> T safeEnum(Class<T> clazz, String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return Enum.valueOf(clazz, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
