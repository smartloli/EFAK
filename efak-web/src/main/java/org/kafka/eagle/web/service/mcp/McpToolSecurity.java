package org.kafka.eagle.web.service.mcp;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * MCP 工具权限：管理员拥有全部工具，普通用户仅 USER 级查询工具。
 */
public final class McpToolSecurity {

    private McpToolSecurity() {
    }

    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        String name = authentication.getName();
        return name != null && "admin".equalsIgnoreCase(name);
    }

    public static boolean canInvoke(McpToolAccess access) {
        if (access == null || access == McpToolAccess.USER) {
            return true;
        }
        return isAdmin();
    }

    public static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
