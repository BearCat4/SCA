package com.example.sca.report;

import com.example.sca.common.Severity;
import com.example.sca.scan.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class ReportService {
    private final VulnerabilityRepository vulnerabilityRepository;
    private final ComponentRepository componentRepository;
    private final LicenseRepository licenseRepository;
    private final DependencyEdgeRepository dependencyEdgeRepository;

    public ReportService(VulnerabilityRepository vulnerabilityRepository,
                         ComponentRepository componentRepository,
                         LicenseRepository licenseRepository,
                         DependencyEdgeRepository dependencyEdgeRepository) {
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.componentRepository = componentRepository;
        this.licenseRepository = licenseRepository;
        this.dependencyEdgeRepository = dependencyEdgeRepository;
    }

    public String html(ScanTask task) {
        List<Vulnerability> vulnerabilities = vulnerabilityRepository.findByScanTaskId(task.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<ComponentFinding> components = componentRepository.findByScanTaskId(task.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<LicenseFinding> licenses = licenseRepository.findByScanTaskId(task.getId());
        List<DependencyEdge> dependencies = dependencyEdgeRepository.searchByScanTaskId(task.getId(), null, org.springframework.data.domain.Pageable.unpaged()).getContent();
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset=\"utf-8\"><title>SCA 扫描报告</title>");
        html.append("<style>body{font-family:Arial,sans-serif;margin:32px;color:#172033}table{border-collapse:collapse;width:100%;margin:16px 0}th,td{border:1px solid #d8dee8;padding:8px;text-align:left}th{background:#f3f6fa}.failed{color:#b42318}.passed{color:#067647}</style>");
        html.append("</head><body>");
        html.append("<h1>").append(escape(task.getProject().getName())).append(" SCA 扫描报告</h1>");
        html.append("<p>状态：<strong class=\"").append(task.getStatus() == ScanStatus.PASSED ? "passed" : "failed").append("\">")
                .append(label(task.getStatus())).append("</strong></p>");
        html.append("<p>分支：").append(escape(task.getBranch())).append("</p>");
        html.append("<p>漏洞：").append(task.getVulnerabilityCount())
                .append(" | 严重：").append(task.getCriticalCount())
                .append(" | 高危：").append(task.getHighCount())
                .append(" | 中危：").append(task.getMediumCount())
                .append(" | 低危：").append(task.getLowCount()).append("</p>");
        if (task.getFailureReason() != null) {
            html.append("<p>失败原因：").append(escape(task.getFailureReason())).append("</p>");
        }
        html.append("<h2>漏洞</h2><table><tr><th>ID</th><th>包</th><th>当前版本</th><th>修复版本</th><th>级别</th><th>标题</th></tr>");
        for (Vulnerability item : vulnerabilities) {
            html.append("<tr><td>").append(escape(item.getVulnerabilityId())).append("</td><td>").append(escape(item.getPackageName()))
                    .append("</td><td>").append(escape(item.getInstalledVersion())).append("</td><td>").append(escape(item.getFixedVersion()))
                    .append("</td><td>").append(label(item.getSeverity())).append("</td><td>").append(escape(item.getTitle())).append("</td></tr>");
        }
        html.append("</table><h2>组件</h2><table><tr><th>包</th><th>版本</th><th>类型</th><th>目标</th></tr>");
        for (ComponentFinding item : components) {
            html.append("<tr><td>").append(escape(item.getPackageName())).append("</td><td>").append(escape(item.getVersion()))
                    .append("</td><td>").append(escape(item.getType())).append("</td><td>").append(escape(item.getTarget())).append("</td></tr>");
        }
        html.append("</table><h2>依赖关系</h2><table><tr><th>上游组件</th><th>版本</th><th>依赖组件</th><th>版本</th><th>作用域</th></tr>");
        for (DependencyEdge item : dependencies) {
            html.append("<tr><td>").append(escape(item.getSourceName())).append("</td><td>").append(escape(item.getSourceVersion()))
                    .append("</td><td>").append(escape(item.getTargetName())).append("</td><td>").append(escape(item.getTargetVersion()))
                    .append("</td><td>").append(escape(labelScope(item.getScope()))).append("</td></tr>");
        }
        html.append("</table><h2>许可证</h2><table><tr><th>包</th><th>版本</th><th>许可证</th><th>目标</th></tr>");
        for (LicenseFinding item : licenses) {
            html.append("<tr><td>").append(escape(item.getPackageName())).append("</td><td>").append(escape(item.getVersion()))
                    .append("</td><td>").append(escape(item.getLicenseName())).append("</td><td>").append(escape(item.getTarget())).append("</td></tr>");
        }
        html.append("</table></body></html>");
        return html.toString();
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }

    private String label(Severity severity) {
        if (severity == Severity.CRITICAL) {
            return "严重";
        }
        if (severity == Severity.HIGH) {
            return "高危";
        }
        if (severity == Severity.MEDIUM) {
            return "中危";
        }
        if (severity == Severity.LOW) {
            return "低危";
        }
        return "未知";
    }

    private String label(ScanStatus status) {
        if (status == ScanStatus.PENDING) {
            return "等待中";
        }
        if (status == ScanStatus.RUNNING) {
            return "扫描中";
        }
        if (status == ScanStatus.PASSED) {
            return "通过";
        }
        if (status == ScanStatus.FAILED) {
            return "未通过";
        }
        if (status == ScanStatus.ERROR) {
            return "异常";
        }
        return "未知";
    }

    private String labelScope(String scope) {
        if ("compile".equals(scope)) {
            return "编译";
        }
        if ("provided".equals(scope)) {
            return "已提供";
        }
        if ("runtime".equals(scope)) {
            return "运行时";
        }
        if ("test".equals(scope)) {
            return "测试";
        }
        if ("system".equals(scope)) {
            return "系统";
        }
        if ("import".equals(scope)) {
            return "导入";
        }
        return scope == null || scope.trim().isEmpty() ? "-" : scope;
    }
}
