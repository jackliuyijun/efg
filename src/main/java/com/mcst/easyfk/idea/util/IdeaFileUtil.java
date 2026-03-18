package com.mcst.easyfk.idea.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.mcst.easyfk.generator.enums.DbType;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class IdeaFileUtil {

    private IdeaFileUtil() {}

    /**
     * IntelliJ 插件类加载器无法通过 SPI 自动发现 JDBC 驱动，需手动加载后再获取连接
     */
    public static Connection getConnection(String url, String user, String password, DbType dbType) throws SQLException {
        try {
            String driverClass = switch (dbType) {
                case MYSQL, MARIADB -> "com.mysql.cj.jdbc.Driver";
                case POSTGRE_SQL -> "org.postgresql.Driver";
                case ORACLE, ORACLE_12C -> "oracle.jdbc.OracleDriver";
                default -> null;
            };
            if (driverClass == null) return DriverManager.getConnection(url, user, password);
            Class.forName(driverClass);
        } catch (ClassNotFoundException ignored) {
        }
        return DriverManager.getConnection(url, user, password);
    }

    public static void refreshProjectDir(String projectDir, String projectName) {
        ApplicationManager.getApplication().invokeLater(() -> {
            String fullPath = projectDir + File.separator + projectName;
            VirtualFile dir = LocalFileSystem.getInstance().refreshAndFindFileByPath(fullPath);
            if (dir != null) {
                dir.refresh(false, true);
            } else {
                VirtualFile parent = LocalFileSystem.getInstance().refreshAndFindFileByPath(projectDir);
                if (parent != null) {
                    parent.refresh(false, true);
                }
            }
        });
    }
}
