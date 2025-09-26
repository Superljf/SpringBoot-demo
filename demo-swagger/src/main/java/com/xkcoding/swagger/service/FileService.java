package com.xkcoding.swagger.service;

import com.xkcoding.swagger.config.FileProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 文件服务类
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@Service
public class FileService {

    @Autowired
    private FileProperties fileProperties;

    // 允许的文件类型和对应的MIME类型
    private static final Map<String, String> ALLOWED_FILE_TYPES = new HashMap<>();

    static {
        ALLOWED_FILE_TYPES.put("txt", "text/plain");
        ALLOWED_FILE_TYPES.put("pdf", "application/pdf");
        ALLOWED_FILE_TYPES.put("doc", "application/msword");
        ALLOWED_FILE_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        ALLOWED_FILE_TYPES.put("xls", "application/vnd.ms-excel");
        ALLOWED_FILE_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        ALLOWED_FILE_TYPES.put("jpg", "image/jpeg");
        ALLOWED_FILE_TYPES.put("jpeg", "image/jpeg");
        ALLOWED_FILE_TYPES.put("png", "image/png");
        ALLOWED_FILE_TYPES.put("gif", "image/gif");
        ALLOWED_FILE_TYPES.put("zip", "application/zip");
        ALLOWED_FILE_TYPES.put("rar", "application/x-rar-compressed");
        ALLOWED_FILE_TYPES.put("csv", "text/csv");
    }

    /**
     * 获取下载文件存储路径
     */
    public String getDownloadPath() {
        return StringUtils.hasText(fileProperties.getStorage().getDownloadPath()) ?
                fileProperties.getStorage().getDownloadPath() :
                System.getProperty("user.home") + File.separator + "demo-files" + File.separator + "download";
    }

    /**
     * 获取临时文件存储路径
     */
    public String getTempPath() {
        return StringUtils.hasText(fileProperties.getStorage().getTempPath()) ?
                fileProperties.getStorage().getTempPath() :
                System.getProperty("user.home") + File.separator + "demo-files" + File.separator + "temp";
    }

    /**
     * 获取报表文件存储路径
     */
    public String getReportPath() {
        return StringUtils.hasText(fileProperties.getStorage().getReportPath()) ?
                fileProperties.getStorage().getReportPath() :
                System.getProperty("user.home") + File.separator + "demo-files" + File.separator + "report";
    }

    /**
     * 验证文件名安全性
     */
    public boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        // 防止路径穿越攻击
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return false;
        }

        // 检查文件扩展名是否允许
        String extension = getFileExtension(fileName);
        return ALLOWED_FILE_TYPES.containsKey(extension);
    }

    /**
     * 获取文件扩展名
     */
    public String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 获取文件MIME类型
     */
    public String getContentType(String fileName) {
        String extension = getFileExtension(fileName);
        return ALLOWED_FILE_TYPES.getOrDefault(extension, "application/octet-stream");
    }

    /**
     * 确保目录存在
     */
    public void ensureDirectoryExists(String directoryPath) throws IOException {
        Path dir = Paths.get(directoryPath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            log.info("创建目录: {}", directoryPath);
        }
    }

    /**
     * 获取缓冲区大小
     */
    public int getBufferSize() {
        return fileProperties.getDownload().getBufferSize() * 1024; // 转换为字节
    }

    /**
     * 获取大文件缓冲区大小
     */
    public int getLargeBufferSize() {
        return fileProperties.getDownload().getLargeBufferSize() * 1024; // 转换为字节
    }

    /**
     * 生成报表文件
     */
    public Path generateReport(String reportType, String startDate, String endDate) throws IOException {
        // 生成报表文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("%s_report_%s.csv", reportType, timestamp);

        // 确保报表目录存在
        String reportPath = getReportPath();
        ensureDirectoryExists(reportPath);

        // 创建报表文件
        Path filePath = Paths.get(reportPath, fileName);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath, StandardCharsets.UTF_8))) {
            // CSV头部
            writer.println("序号,名称,类型,创建时间,状态,开始时间,结束时间");

            // 模拟生成报表数据
            for (int i = 1; i <= 100; i++) {
                writer.printf("%d,%s%d,%s,%s,正常,%s,%s%n",
                        i,
                        reportType,
                        i,
                        reportType.toUpperCase(),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        startDate != null ? startDate : "N/A",
                        endDate != null ? endDate : "N/A");
            }

            writer.flush();
            log.info("报表生成成功: {}", filePath);
        }

        return filePath;
    }

    /**
     * 生成学员状态报表
     */
    public Path generateStudentStatusReport(String buId, Map<String, Object> searchForm) throws IOException {
        // 生成报表文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("student_status_report_%s.csv", timestamp);

        // 确保临时目录存在
        String tempPath = getTempPath();
        ensureDirectoryExists(tempPath);

        // 创建报表文件
        Path filePath = Paths.get(tempPath, fileName);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath, StandardCharsets.UTF_8))) {
            // CSV头部
            writer.println("学员ID,学员姓名,状态,业务单元,创建时间,更新时间");

            // 模拟生成学员状态数据（实际项目中这里会查询数据库）
            String[] statuses = {"在读", "休学", "毕业", "退学"};
            String[] businessUnits = {"BU001", "BU002", "BU003", "BU004"};
            String[] names = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"};
            
            // 根据搜索条件生成相应数量的数据
            int recordCount = searchForm != null && searchForm.containsKey("recordCount") ? 
                (Integer) searchForm.get("recordCount") : 50;
                
            for (int i = 1; i <= recordCount; i++) {
                String studentId = String.format("STU%06d", i);
                String name = names[i % names.length];
                String status = statuses[i % statuses.length];
                String businessUnit = buId != null ? buId : businessUnits[i % businessUnits.length];
                String createTime = LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String updateTime = LocalDateTime.now().minusHours(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                writer.printf("%s,%s,%s,%s,%s,%s%n",
                        studentId, name, status, businessUnit, createTime, updateTime);
            }

            writer.flush();
            log.info("学员状态报表生成成功: {}", filePath);
        }

        return filePath;
    }

    /**
     * 创建测试文件（用于演示）
     */
    public void createTestFiles() {
        try {
            String downloadPath = getDownloadPath();
            ensureDirectoryExists(downloadPath);

            // 创建一个测试文本文件
            Path testFile = Paths.get(downloadPath, "test.txt");
            if (!Files.exists(testFile)) {
                try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(testFile, StandardCharsets.UTF_8))) {
                    writer.println("这是一个测试文件");
                    writer.println("用于演示文件下载功能");
                    writer.println("创建时间: " + LocalDateTime.now());
                    writer.println("文件类型: 文本文件");
                    writer.println();
                    writer.println("支持的下载方式:");
                    writer.println("1. ResponseEntity + Resource 方式");
                    writer.println("2. HttpServletResponse 直接写入流");
                    writer.println("3. StreamingResponseBody 大文件流式下载");
                }
                log.info("测试文件创建成功: {}", testFile);
            }

            // 创建一个较大的测试文件
            Path largeTestFile = Paths.get(downloadPath, "large-test.txt");
            if (!Files.exists(largeTestFile)) {
                try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(largeTestFile, StandardCharsets.UTF_8))) {
                    writer.println("这是一个大文件测试");
                    writer.println("用于演示大文件流式下载功能");
                    writer.println("创建时间: " + LocalDateTime.now());
                    writer.println();

                    // 生成大量内容
                    for (int i = 1; i <= 10000; i++) {
                        writer.printf("第%d行数据 - 这是用于测试大文件下载的内容，包含一些示例数据和信息。时间戳: %s%n",
                                i, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
                    }
                }
                log.info("大文件测试文件创建成功: {}", largeTestFile);
            }

        } catch (IOException e) {
            log.error("创建测试文件失败: {}", e.getMessage(), e);
        }
    }
}