package com.esc.lickerz.lickerz_sep.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RoleCheckAspect {

    /**
     * @Note
     * 등급 검사
     */
    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String userRole = (String) request.getAttribute("role");

        if (userRole != null && userRole.equals(requireRole.value())) {
            return joinPoint.proceed();
        } else {
            throw new Exception("Access denied");
        }
    }
}
