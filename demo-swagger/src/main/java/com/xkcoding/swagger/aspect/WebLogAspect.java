package com.xkcoding.swagger.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xkcoding.swagger.annotation.WebLog;
import com.xkcoding.swagger.entity.WebLogInfo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * <p>
 * Web 请求日志记录切面
 * </p>
 * 
 * 这个切面会拦截所有标记了 @WebLog 注解的方法，记录详细的请求信息
 *
 * @author demo
 * @date Created in 2024-12-19
 */
@Aspect
@Component
@Slf4j
public class WebLogAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 定义切点：拦截所有标记了 @WebLog 注解的方法
     */
    @Pointcut("@annotation(com.xkcoding.swagger.annotation.WebLog)")
    public void webLogPointcut() {
    }

    /**
     * 环绕通知：在方法执行前后记录日志
     */
    @Around("webLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        
        // 获取 HttpServletRequest
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        WebLog webLog = method.getAnnotation(WebLog.class);
        
        // 构建日志信息
        WebLogInfo logInfo = WebLogInfo.builder()
                .requestId(requestId)
                .requestTime(LocalDateTime.now())
                .className(joinPoint.getTarget().getClass().getSimpleName())
                .methodName(method.getName())
                .description(webLog.value())
                .build();

        // 记录请求信息
        if (request != null) {
            logInfo.setMethod(request.getMethod());
            logInfo.setUrl(request.getRequestURL().toString());
            logInfo.setUri(request.getRequestURI());
            logInfo.setClientIp(getClientIp(request));
            logInfo.setUserAgent(request.getHeader("User-Agent"));
            
            // 记录请求参数
            if (webLog.logArgs()) {
                String params = getRequestParams(request, joinPoint.getArgs());
                logInfo.setParams(params);
            }
        }

        Object result = null;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            
            // 记录成功信息
            logInfo.setStatus("SUCCESS");
            if (webLog.logResult() && result != null) {
                logInfo.setResult(truncateString(objectMapper.writeValueAsString(result), 1000));
            }
            
        } catch (Exception e) {
            // 记录异常信息
            logInfo.setStatus("ERROR");
            if (webLog.logException()) {
                logInfo.setExceptionMsg(e.getMessage());
            }
            throw e;
        } finally {
            // 记录执行时间
            if (webLog.logTime()) {
                long executeTime = System.currentTimeMillis() - startTime;
                logInfo.setExecuteTime(executeTime);
            }
            
            // 输出日志
            printLog(logInfo);
        }

        return result;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xip = request.getHeader("X-Real-IP");
        String xfor = request.getHeader("X-Forwarded-For");
        
        if (xfor != null && !xfor.isEmpty() && !"unknown".equalsIgnoreCase(xfor)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = xfor.indexOf(",");
            if (index != -1) {
                return xfor.substring(0, index);
            } else {
                return xfor;
            }
        }
        
        if (xip != null && !xip.isEmpty() && !"unknown".equalsIgnoreCase(xip)) {
            return xip;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(HttpServletRequest request, Object[] args) {
        try {
            StringBuilder params = new StringBuilder();
            
            // URL 参数
            if (request.getQueryString() != null) {
                params.append("URL参数: ").append(request.getQueryString());
            }
            
            // 方法参数
            if (args != null && args.length > 0) {
                if (params.length() > 0) {
                    params.append(" | ");
                }
                params.append("方法参数: ").append(Arrays.toString(args));
            }
            
            return truncateString(params.toString(), 500);
        } catch (Exception e) {
            return "参数解析失败: " + e.getMessage();
        }
    }

    /**
     * 截断字符串（避免日志过长）
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "... (截断)";
    }

    /**
     * 打印日志
     */
    private void printLog(WebLogInfo logInfo) {
        StringBuilder logBuilder = new StringBuilder("\n");
        logBuilder.append("========================= Web Log Start =========================\n");
        logBuilder.append("请求ID: ").append(logInfo.getRequestId()).append("\n");
        logBuilder.append("请求时间: ").append(logInfo.getRequestTime()).append("\n");
        logBuilder.append("请求方法: ").append(logInfo.getMethod()).append("\n");
        logBuilder.append("请求URL: ").append(logInfo.getUrl()).append("\n");
        logBuilder.append("请求URI: ").append(logInfo.getUri()).append("\n");
        logBuilder.append("客户端IP: ").append(logInfo.getClientIp()).append("\n");
        logBuilder.append("目标类: ").append(logInfo.getClassName()).append("\n");
        logBuilder.append("目标方法: ").append(logInfo.getMethodName()).append("\n");
        logBuilder.append("方法描述: ").append(logInfo.getDescription()).append("\n");
        
        if (logInfo.getParams() != null) {
            logBuilder.append("请求参数: ").append(logInfo.getParams()).append("\n");
        }
        
        if (logInfo.getExecuteTime() != null) {
            logBuilder.append("执行时间: ").append(logInfo.getExecuteTime()).append("ms\n");
        }
        
        logBuilder.append("执行状态: ").append(logInfo.getStatus()).append("\n");
        
        if ("SUCCESS".equals(logInfo.getStatus()) && logInfo.getResult() != null) {
            logBuilder.append("返回结果: ").append(logInfo.getResult()).append("\n");
        }
        
        if ("ERROR".equals(logInfo.getStatus()) && logInfo.getExceptionMsg() != null) {
            logBuilder.append("异常信息: ").append(logInfo.getExceptionMsg()).append("\n");
        }
        
        logBuilder.append("========================= Web Log End ===========================");
        
        // 根据执行状态选择日志级别
        if ("SUCCESS".equals(logInfo.getStatus())) {
            log.info(logBuilder.toString());
        } else {
            log.error(logBuilder.toString());
        }
    }
}
