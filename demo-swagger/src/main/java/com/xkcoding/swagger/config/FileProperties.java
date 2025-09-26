package com.xkcoding.swagger.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 文件存储配置属性
 * </p>
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    /**
     * 文件存储配置
     */
    private Storage storage = new Storage();

    /**
     * 文件上传配置
     */
    private Upload upload = new Upload();

    /**
     * 文件下载配置
     */
    private Download download = new Download();

    @Data
    public static class Storage {
        /**
         * 上传文件存储路径
         */
        private String uploadPath;

        /**
         * 下载文件存储路径
         */
        private String downloadPath;

        /**
         * 临时文件存储路径
         */
        private String tempPath;

        /**
         * 报表文件存储路径
         */
        private String reportPath;
    }

    @Data
    public static class Upload {
        /**
         * 单个文件大小限制（MB）
         */
        private int maxFileSize = 10;

        /**
         * 请求总大小限制（MB）
         */
        private int maxRequestSize = 50;

        /**
         * 允许的文件类型
         */
        private List<String> allowedTypes;
    }

    @Data
    public static class Download {
        /**
         * 是否启用断点续传
         */
        private boolean enableResume = true;

        /**
         * 缓冲区大小（KB）
         */
        private int bufferSize = 8;

        /**
         * 大文件缓冲区大小（KB）
         */
        private int largeBufferSize = 16;
    }
}