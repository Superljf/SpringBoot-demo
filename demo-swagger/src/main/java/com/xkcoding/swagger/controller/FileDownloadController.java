package com.xkcoding.swagger.controller;

import com.xkcoding.swagger.annotation.RequirePermission;
import com.xkcoding.swagger.annotation.WebLog;
import com.xkcoding.swagger.common.ApiResponse;
import com.xkcoding.swagger.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
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
 * 文件下载控制器
 * </p>
 * 
 * 提供多种文件下载方式的API接口
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Slf4j
@RestController
@RequestMapping("/file")
@Tag(name = "文件下载", description = "文件下载相关API接口")
public class FileDownloadController {

    @Autowired
    private FileService fileService;
    
    /**
     * 初始化方法，创建测试文件
     */
    @PostConstruct
    public void init() {
        fileService.createTestFiles();
    }

    /**
     * 方式1：使用 ResponseEntity + Resource 下载文件（推荐）
     */
    @GetMapping("/download/resource/{fileName}")
    @WebLog("使用Resource方式下载文件")
    @RequirePermission("file:download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "Resource方式下载文件", description = "使用Spring Resource进行文件下载，支持断点续传")
    public ResponseEntity<Resource> downloadFileByResource(
            @Parameter(description = "文件名", example = "test.txt")
            @PathVariable String fileName) {
        
        try {
            log.info("开始下载文件: {}", fileName);
            
            // 验证文件名安全性
            if (!fileService.isValidFileName(fileName)) {
                log.warn("非法文件名: {}", fileName);
                return ResponseEntity.badRequest().build();
            }
            
            // 构建文件路径
            Path filePath = Paths.get(fileService.getDownloadPath(), fileName);
            Resource resource = new FileSystemResource(filePath);
            
            // 检查文件是否存在
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("文件不存在或不可读: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 获取文件MIME类型
            String contentType = fileService.getContentType(fileName);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(resource.contentLength());
            
            // 设置文件名（支持中文）
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            headers.setContentDispositionFormData("attachment", encodedFileName);
            
            // 支持断点续传
            headers.set("Accept-Ranges", "bytes");
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            log.info("文件下载成功: {}, 大小: {} bytes", fileName, resource.contentLength());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("文件下载失败: {}, 错误: {}", fileName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 方式2：使用 HttpServletResponse 直接写入流
     */
    @GetMapping("/download/stream/{fileName}")
    @WebLog("使用Stream方式下载文件")
    @RequirePermission("file:download")
    @Operation(summary = "Stream方式下载文件", description = "直接通过HttpServletResponse输出流下载文件")
    public void downloadFileByStream(
            @Parameter(description = "文件名", example = "test.txt")
            @PathVariable String fileName,
            HttpServletResponse response) {
        
        try {
            log.info("开始Stream下载文件: {}", fileName);
            
            // 验证文件名安全性
            if (!fileService.isValidFileName(fileName)) {
                log.warn("非法文件名: {}", fileName);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // 构建文件路径
            Path filePath = Paths.get(fileService.getDownloadPath(), fileName);
            File file = filePath.toFile();
            
            // 检查文件是否存在
            if (!file.exists() || !file.canRead()) {
                log.warn("文件不存在或不可读: {}", filePath);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 获取文件MIME类型
            String contentType = fileService.getContentType(fileName);
            
            // 设置响应头
            response.setContentType(contentType);
            response.setContentLengthLong(file.length());
            
            // 设置文件名（支持中文）
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            
            // 其他响应头
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            
            // 读取文件并写入响应流
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis);
                 OutputStream outputStream = response.getOutputStream()) {
                
                byte[] buffer = new byte[fileService.getBufferSize()];
                int bytesRead;
                
                while ((bytesRead = bis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                outputStream.flush();
                log.info("Stream文件下载成功: {}, 大小: {} bytes", fileName, file.length());
            }
            
        } catch (Exception e) {
            log.error("Stream文件下载失败: {}, 错误: {}", fileName, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 方式3：使用 StreamingResponseBody 大文件流式下载
     */
    @GetMapping("/download/streaming/{fileName}")
    @WebLog("使用StreamingResponseBody方式下载大文件")
    @RequirePermission("file:download")
    @Operation(summary = "流式下载大文件", description = "适用于大文件的流式下载，避免内存溢出")
    public ResponseEntity<StreamingResponseBody> downloadLargeFile(
            @Parameter(description = "文件名", example = "large-test.txt")
            @PathVariable String fileName) {
        
        try {
            log.info("开始流式下载大文件: {}", fileName);
            
            // 验证文件名安全性
            if (!fileService.isValidFileName(fileName)) {
                log.warn("非法文件名: {}", fileName);
                return ResponseEntity.badRequest().build();
            }
            
            // 构建文件路径
            Path filePath = Paths.get(fileService.getDownloadPath(), fileName);
            File file = filePath.toFile();
            
            // 检查文件是否存在
            if (!file.exists() || !file.canRead()) {
                log.warn("文件不存在或不可读: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 获取文件MIME类型
            String contentType = fileService.getContentType(fileName);
            
            // 创建StreamingResponseBody
            StreamingResponseBody streamingResponseBody = outputStream -> {
                try (FileInputStream fis = new FileInputStream(file);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {
                    
                    byte[] buffer = new byte[fileService.getLargeBufferSize()];
                    int bytesRead;
                    long totalBytesRead = 0;
                    
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        
                        // 每1MB记录一次进度
                        if (totalBytesRead % (1024 * 1024) == 0) {
                            log.debug("已下载: {} MB", totalBytesRead / (1024 * 1024));
                        }
                    }
                    
                    outputStream.flush();
                    log.info("流式下载完成: {}, 总大小: {} MB", fileName, totalBytesRead / (1024 * 1024));
                    
                } catch (IOException e) {
                    log.error("流式下载过程中发生错误: {}", e.getMessage(), e);
                    throw e;
                }
            };
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(file.length());
            
            // 设置文件名（支持中文）
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            headers.setContentDispositionFormData("attachment", encodedFileName);
            
            // 流式传输相关头
            headers.set("Accept-Ranges", "bytes");
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(streamingResponseBody);
                    
        } catch (Exception e) {
            log.error("流式文件下载失败: {}, 错误: {}", fileName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 生成并下载报表文件
     */
    @PostMapping("/download/report")
    @WebLog("生成并下载报表")
    @RequirePermission("file:report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "生成并下载报表", description = "动态生成报表文件并下载")
    public ResponseEntity<Resource> generateAndDownloadReport(
            @Parameter(description = "报表类型", example = "user")
            @RequestParam String reportType,
            @Parameter(description = "开始日期", example = "2024-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期", example = "2024-12-31")
            @RequestParam(required = false) String endDate) {
        
        try {
            log.info("开始生成报表: 类型={}, 开始日期={}, 结束日期={}", reportType, startDate, endDate);
            
            // 生成报表文件
            Path reportFile = fileService.generateReport(reportType, startDate, endDate);
            
            // 创建Resource
            Resource resource = new FileSystemResource(reportFile);
            
            if (!resource.exists()) {
                log.error("报表生成失败，文件不存在: {}", reportFile);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentLength(resource.contentLength());
            
            String encodedFileName = URLEncoder.encode(reportFile.getFileName().toString(), StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            headers.setContentDispositionFormData("attachment", encodedFileName);
            
            log.info("报表生成并下载成功: {}", reportFile.getFileName());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("报表生成失败: 类型={}, 错误={}", reportType, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/list")
    @WebLog("获取文件列表")
    @RequirePermission("file:list")
    @Operation(summary = "获取可下载文件列表", description = "获取服务器上可供下载的文件列表")
    public ApiResponse<Map<String, Object>> getFileList() {
        try {
            Path storageDir = Paths.get(fileService.getDownloadPath());
            
            if (!Files.exists(storageDir)) {
                fileService.ensureDirectoryExists(fileService.getDownloadPath());
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("files", Files.list(storageDir)
                    .filter(Files::isRegularFile)
                    .map(path -> { 
                        try {
                            Map<String, Object> fileInfo = new HashMap<>();
                            fileInfo.put("name", path.getFileName().toString());
                            fileInfo.put("size", Files.size(path));
                            fileInfo.put("lastModified", Files.getLastModifiedTime(path).toString());
                            fileInfo.put("extension", fileService.getFileExtension(path.getFileName().toString()));
                            fileInfo.put("contentType", fileService.getContentType(path.getFileName().toString()));
                            return fileInfo;
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(fileInfo -> fileInfo != null)
                    .toArray());
            
            result.put("total", ((Object[]) result.get("files")).length);
            result.put("storagePath", fileService.getDownloadPath());
            
            return ApiResponse.<Map<String, Object>>builder()
                    .code(200)
                    .message("获取文件列表成功")
                    .data(result)
                    .build();
                    
        } catch (Exception e) {
            log.error("获取文件列表失败: {}", e.getMessage(), e);
            return ApiResponse.<Map<String, Object>>builder()
                    .code(500)
                    .message("获取文件列表失败: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }
}