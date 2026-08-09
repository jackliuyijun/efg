package com.mcst.easyfk.idea.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.mcst.easyfk.generator.enums.DbType;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
     * （表现为 U+FFFD 替换字符乱码，常见于中文错误消息），含 U+FFFD 的原始内容
     * 已不可恢复，但仍保留可读部分（SQLState、英文关键字等）并给出编码建议；
     * 不含 U+FFFD 的可逆乱码会尝试按常见编码反向修复。
     */
    public static String sanitizeDbErrorMessage(Throwable t) {
        if (t == null) {
            return "未知错误，请检查数据库配置";
        }
        String sqlState = t instanceof SQLException sqlEx ? sqlEx.getSQLState() : null;
        String message = t.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return sqlState != null && !sqlState.isEmpty()
                    ? "未知错误(SQLState: " + sqlState + ")，请检查数据库配置"
                    : "未知错误，请检查数据库配置";
        }

        String body = message;
        if (body.indexOf('\uFFFD') < 0) {
            String repaired = tryRepairMojibake(message);
            if (repaired != null) {
                body = repaired;
            }
        }

        StringBuilder sb = new StringBuilder();
        if (sqlState != null && !sqlState.isEmpty()) {
            sb.append("SQLState: ").append(sqlState).append('\n');
        }
        sb.append(body);
        if (body.indexOf('\uFFFD') >= 0) {
            sb.append("\n\n数据库返回的错误消息含有无法解码的字符(编码不一致)，具体错误无法完整还原。")
                    .append("建议：数据库使用 UTF8 编码，并在连接串加 characterEncoding=UTF-8。");
        }
        return sb.toString();
    }

    /**
     * 尝试修复“可逆乱码”：UTF-8 字节被按 ISO-8859-1/GBK 等编码解码时不会产生 U+FFFD，
     * 但会显示为乱码，可通过反向转码恢复。已含 U+FFFD 的文本不可逆，返回 null。
     */
    private static String tryRepairMojibake(String message) {
        if (message.indexOf('\uFFFD') >= 0 || containsCjk(message)) {
            return null;
        }
        List<String> candidates = new ArrayList<>();
        if (message.chars().allMatch(c -> c < 0x100)) {
            candidates.add(new String(message.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
        }
        try {
            Charset gbk = Charset.forName("GBK");
            candidates.add(new String(message.getBytes(gbk), StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
        for (String candidate : candidates) {
            if (candidate.indexOf('\uFFFD') < 0 && containsCjk(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean containsCjk(String s) {
        return s.codePoints().anyMatch(cp ->
                (cp >= 0x4E00 && cp <= 0x9FFF) || (cp >= 0x3400 && cp <= 0x4DBF));
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
