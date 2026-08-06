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
                default -> null;
            };
            if (driverClass == null) return DriverManager.getConnection(url, user, password);
            Class.forName(driverClass);
        } catch (ClassNotFoundException ignored) {
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * 数据库驱动返回的错误消息可能因服务端字符集与驱动解码不一致而损坏
     * （表现为 U+FFFD 替换字符乱码，常见于中文错误消息），原始内容已不可恢复，
     * 此时返回可读的通用提示。
     */
    public static String sanitizeDbErrorMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "未知错误，请检查数据库配置";
        }
        if (message.indexOf('\uFFFD') >= 0) {
            return "连接失败，数据库返回的错误信息因编码问题无法正常显示。"
                    + "请检查数据库地址、端口、数据库名、账号密码是否正确，"
                    + "以及数据库服务是否可用、网络是否连通。";
        }
        return message;
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
