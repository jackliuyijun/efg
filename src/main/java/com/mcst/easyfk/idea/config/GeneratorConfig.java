package com.mcst.easyfk.idea.config;

public class GeneratorConfig {

    private ProjectConfig project;
    private CodeConfig code;
    private DbConfig db;

    public ProjectConfig getProject() { return project; }
    public void setProject(ProjectConfig project) { this.project = project; }
    public CodeConfig getCode() { return code; }
    public void setCode(CodeConfig code) { this.code = code; }
    public DbConfig getDb() { return db; }
    public void setDb(DbConfig db) { this.db = db; }

    public static class ProjectConfig {
        private String projectName;
        private String groupId;
        private String basePackage;
        private String projectDir;
        private String frameworkVersion;
        private String projectVersion;
        private String projectType;
        private String buildType;
        private String gradleType;
        private String ormType;
        private String rpcType;
        private String prdType;
        private String appType;
        private String logType;
        private Boolean includeAuth;

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }
        public String getBasePackage() { return basePackage; }
        public void setBasePackage(String basePackage) { this.basePackage = basePackage; }
        public String getProjectDir() { return projectDir; }
        public void setProjectDir(String projectDir) { this.projectDir = projectDir; }
        public String getFrameworkVersion() { return frameworkVersion; }
        public void setFrameworkVersion(String frameworkVersion) { this.frameworkVersion = frameworkVersion; }
        public String getProjectVersion() { return projectVersion; }
        public void setProjectVersion(String projectVersion) { this.projectVersion = projectVersion; }
        public String getProjectType() { return projectType; }
        public void setProjectType(String projectType) { this.projectType = projectType; }
        public String getBuildType() { return buildType; }
        public void setBuildType(String buildType) { this.buildType = buildType; }
        public String getGradleType() { return gradleType; }
        public void setGradleType(String gradleType) { this.gradleType = gradleType; }
        public String getOrmType() { return ormType; }
        public void setOrmType(String ormType) { this.ormType = ormType; }
        public String getRpcType() { return rpcType; }
        public void setRpcType(String rpcType) { this.rpcType = rpcType; }
        public String getPrdType() { return prdType; }
        public void setPrdType(String prdType) { this.prdType = prdType; }
        public String getAppType() { return appType; }
        public void setAppType(String appType) { this.appType = appType; }
        public String getLogType() { return logType; }
        public void setLogType(String logType) { this.logType = logType; }
        public Boolean getIncludeAuth() { return includeAuth; }
        public void setIncludeAuth(Boolean includeAuth) { this.includeAuth = includeAuth; }
    }

    public static class CodeConfig {
        private String moduleName;
        private String author;
        private Boolean springAnnotation;
        private Boolean extendsSupperClass;
        private Boolean createResourceAnnotation;

        public String getModuleName() { return moduleName; }
        public void setModuleName(String moduleName) { this.moduleName = moduleName; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public Boolean getSpringAnnotation() { return springAnnotation; }
        public void setSpringAnnotation(Boolean springAnnotation) { this.springAnnotation = springAnnotation; }
        public Boolean getExtendsSupperClass() { return extendsSupperClass; }
        public void setExtendsSupperClass(Boolean extendsSupperClass) { this.extendsSupperClass = extendsSupperClass; }
        public Boolean getCreateResourceAnnotation() { return createResourceAnnotation; }
        public void setCreateResourceAnnotation(Boolean createResourceAnnotation) { this.createResourceAnnotation = createResourceAnnotation; }
    }

    public static class DbConfig {
        private String dbType;
        private String dbShortUrl;
        private String dbUser;
        private String tablePrefix;

        public String getDbType() { return dbType; }
        public void setDbType(String dbType) { this.dbType = dbType; }
        public String getDbShortUrl() { return dbShortUrl; }
        public void setDbShortUrl(String dbShortUrl) { this.dbShortUrl = dbShortUrl; }
        public String getDbUser() { return dbUser; }
        public void setDbUser(String dbUser) { this.dbUser = dbUser; }
        public String getTablePrefix() { return tablePrefix; }
        public void setTablePrefix(String tablePrefix) { this.tablePrefix = tablePrefix; }
    }
}
