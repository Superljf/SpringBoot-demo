# Spring Boot 文件下载功能实现指南

本指南详细介绍了在Spring Boot后端中实现文件下载功能的多种方式和最佳实践。

## 🎯 核心实现方式

### 1. ResponseEntity + Resource 方式（推荐）

这是**最推荐的方式**，使用Spring的Resource抽象和ResponseEntity来处理文件下载。

```java
@GetMapping("/download/resource/{fileName}")
@RequirePermission("file:download")
public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
    try {
        // 验证文件名安全性
        if (!fileService.isValidFileName(fileName)) {
            return ResponseEntity.badRequest().build();
        }
        
        // 构建文件路径
        Path filePath = Paths.get(fileService.getDownloadPath(), fileName);
        Resource resource = new FileSystemResource(filePath);
        
        // 检查文件是否存在
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(fileService.getContentType(fileName)));
        headers.setContentLength(resource.contentLength());
        
        // 设置文件名（支持中文）
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
        headers.setContentDispositionFormData("attachment", encodedFileName);
        
        // 支持断点续传
        headers.set("Accept-Ranges", "bytes");
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
                
    } catch (Exception e) {
        log.error("文件下载失败: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
```

**优点**：
- ✅ 代码简洁，Spring自动处理资源释放
- ✅ 自动支持断点续传
- ✅ 内存使用效率高
- ✅ 支持大文件下载

**适用场景**：一般文件下载需求

### 2. HttpServletResponse 直接写入流

直接操作HTTP响应流，适合需要精确控制响应过程的场景。

```java
@GetMapping("/download/stream/{fileName}")
@RequirePermission("file:download")
public void downloadFileByStream(@PathVariable String fileName, HttpServletResponse response) {
    try {
        // 验证文件名安全性
        if (!fileService.isValidFileName(fileName)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // 构建文件路径
        Path filePath = Paths.get(fileService.getDownloadPath(), fileName);
        File file = filePath.toFile();
        
        if (!file.exists() || !file.canRead()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // 设置响应头
        response.setContentType(fileService.getContentType(fileName));
        response.setContentLengthLong(file.length());
        
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        
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
        }
        
    } catch (Exception e) {
        log.error("文件下载失败: {}", e.getMessage(), e);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
```

**优点**：
- ✅ 精确控制响应过程
- ✅ 可以添加自定义下载逻辑
- ✅ 支持流式传输

**缺点**：
- ❌ 代码相对复杂
- ❌ 需要手动管理资源释放

**适用场景**：需要在下载过程中添加自定义逻辑

### 3. StreamingResponseBody 大文件流式下载

专门用于大文件的流式下载，避免内存溢出。

```java
@GetMapping("/download/streaming/{fileName}")
@RequirePermission("file:download")
public ResponseEntity<StreamingResponseBody> downloadLargeFile(@PathVariable String fileName) {
    try {
        // 验证和准备工作...
        Path filePath = Paths.get(fileService.getDownloadPath(), fileName);
        File file = filePath.toFile();
        
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
                    
                    // 可以在这里添加进度监控
                    if (totalBytesRead % (1024 * 1024) == 0) {
                        log.debug("已下载: {} MB", totalBytesRead / (1024 * 1024));
                    }
                }
                
                outputStream.flush();
            }
        };
        
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(fileService.getContentType(fileName)));
        headers.setContentLength(file.length());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(streamingResponseBody);
                
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
```

**优点**：
- ✅ 适合超大文件下载
- ✅ 内存使用量可控
- ✅ 支持下载进度监控
- ✅ 异步处理

**适用场景**：大文件下载（>100MB）

## 🔧 核心配置

### 1. 文件存储配置

在 `application.yml` 中配置文件存储路径：

```yaml
# 文件存储配置
file:
  storage:
    upload-path: ${user.home}/demo-files/upload
    download-path: ${user.home}/demo-files/download
    temp-path: ${user.home}/demo-files/temp
    report-path: ${user.home}/demo-files/report
  upload:
    max-file-size: 10        # MB
    max-request-size: 50     # MB
    allowed-types: 
      - txt
      - pdf
      - doc
      - docx
      - jpg
      - png
      - zip
  download:
    enable-resume: true      # 断点续传
    buffer-size: 8           # KB
    large-buffer-size: 16    # KB
```

### 2. 配置属性类

```java
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {
    private Storage storage = new Storage();
    private Upload upload = new Upload();
    private Download download = new Download();
    
    // 内部类定义...
}
```

### 3. 文件服务类

```java
@Service
public class FileService {
    @Autowired
    private FileProperties fileProperties;
    
    public boolean isValidFileName(String fileName) {
        // 文件名安全性验证
        if (fileName == null || fileName.contains("..") || fileName.contains("/")) {
            return false;
        }
        return getAllowedFileTypes().containsKey(getFileExtension(fileName));
    }
    
    public String getContentType(String fileName) {
        String extension = getFileExtension(fileName);
        return getAllowedFileTypes().getOrDefault(extension, "application/octet-stream");
    }
    
    // 其他工具方法...
}
```

## 🛡️ 安全考虑

### 1. 文件名安全验证

```java
public boolean isValidFileName(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
        return false;
    }
    
    // 防止路径穿越攻击
    if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
        return false;
    }
    
    // 检查文件扩展名白名单
    String extension = getFileExtension(fileName);
    return ALLOWED_FILE_TYPES.containsKey(extension);
}
```

### 2. 权限控制

```java
@RequirePermission("file:download")  // 自定义权限注解
@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")  // Spring Security
public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
    // 下载逻辑...
}
```

### 3. 文件访问日志

```java
@WebLog("文件下载")  // 自定义日志注解
public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
    log.info("用户下载文件: {}, IP: {}", fileName, getClientIP());
    // 下载逻辑...
}
```

## 📊 特殊功能实现

### 1. 动态报表生成和下载

```java
@PostMapping("/download/report")
@RequirePermission("file:report")
public ResponseEntity<Resource> generateAndDownloadReport(
        @RequestParam String reportType,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate) {
    
    try {
        // 生成报表文件
        Path reportFile = fileService.generateReport(reportType, startDate, endDate);
        
        Resource resource = new FileSystemResource(reportFile);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", reportFile.getFileName().toString());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
                
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
```

### 2. 文件列表获取

```java
@GetMapping("/list")
@RequirePermission("file:list")
public ApiResponse<Map<String, Object>> getFileList() {
    try {
        Path storageDir = Paths.get(fileService.getDownloadPath());
        
        Map<String, Object> result = new HashMap<>();
        result.put("files", Files.list(storageDir)
                .filter(Files::isRegularFile)
                .map(path -> {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("name", path.getFileName().toString());
                    fileInfo.put("size", Files.size(path));
                    fileInfo.put("lastModified", Files.getLastModifiedTime(path).toString());
                    return fileInfo;
                })
                .toArray());
        
        return ApiResponse.success(result);
        
    } catch (Exception e) {
        return ApiResponse.error("获取文件列表失败: " + e.getMessage());
    }
}
```

### 3. 支持断点续传

```java
// 在响应头中添加支持
headers.set("Accept-Ranges", "bytes");

// 处理Range请求头
String rangeHeader = request.getHeader("Range");
if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
    // 解析Range头，实现断点续传逻辑
    // 这里可以根据需要实现具体的范围请求处理
}
```

## 🚀 最佳实践

### 1. 异常处理

```java
try {
    // 文件下载逻辑
} catch (FileNotFoundException e) {
    log.warn("文件不存在: {}", fileName);
    return ResponseEntity.notFound().build();
} catch (IOException e) {
    log.error("文件读取失败: {}", e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
} catch (Exception e) {
    log.error("文件下载异常: {}", e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
}
```

### 2. 性能优化

- **缓冲区大小**：根据文件大小选择合适的缓冲区
- **连接池**：使用连接池管理数据库连接
- **缓存**：对于频繁下载的文件可以考虑缓存
- **压缩**：大文件可以考虑压缩后传输

### 3. 监控和日志

```java
@Component
public class FileDownloadMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter downloadCounter;
    private final Timer downloadTimer;
    
    public FileDownloadMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.downloadCounter = Counter.builder("file.download.count")
                .description("文件下载次数")
                .register(meterRegistry);
        this.downloadTimer = Timer.builder("file.download.duration")
                .description("文件下载耗时")
                .register(meterRegistry);
    }
    
    public void recordDownload(String fileName, long duration) {
        downloadCounter.increment(Tags.of("file", fileName));
        downloadTimer.record(duration, TimeUnit.MILLISECONDS);
    }
}
```

## 📋 API 接口总览

您的项目现在支持以下文件下载相关的API：

| 接口 | 方法 | 说明 | 权限要求 |
|------|------|------|----------|
| `/file/download/resource/{fileName}` | GET | Resource方式下载文件 | `file:download` |
| `/file/download/stream/{fileName}` | GET | Stream方式下载文件 | `file:download` |
| `/file/download/streaming/{fileName}` | GET | 流式下载大文件 | `file:download` |
| `/file/download/report` | POST | 生成并下载报表 | `file:report` |
| `/file/list` | GET | 获取文件列表 | `file:list` |

## 🎯 使用示例

### 前端调用示例

```javascript
// 1. 普通文件下载
function downloadFile(fileName) {
    const token = localStorage.getItem('token');
    const url = `/demo/file/download/resource/${fileName}`;
    
    fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.blob())
    .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        a.click();
        window.URL.revokeObjectURL(url);
    });
}

// 2. 生成报表并下载
function downloadReport(reportType, startDate, endDate) {
    const token = localStorage.getItem('token');
    const url = `/demo/file/download/report`;
    
    const params = new URLSearchParams({
        reportType: reportType,
        startDate: startDate,
        endDate: endDate
    });
    
    fetch(`${url}?${params}`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.blob())
    .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${reportType}_report.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
    });
}
```

### cURL 测试示例

```bash
# 1. 先登录获取Token
curl -X POST "http://localhost:8089/demo/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123456"}'

# 2. 下载文件
curl -X GET "http://localhost:8089/demo/file/download/resource/test.txt" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o "downloaded_test.txt"

# 3. 获取文件列表
curl -X GET "http://localhost:8089/demo/file/list" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 4. 生成并下载报表
curl -X POST "http://localhost:8089/demo/file/download/report?reportType=user&startDate=2024-01-01&endDate=2024-12-31" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o "user_report.csv"
```

通过以上实现，您的Spring Boot项目现在具备了完整的文件下载功能，支持多种下载方式、权限控制、安全验证和性能优化！