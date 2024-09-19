package com.esc.lickerz.lickerz_sep.config;

import java.util.UUID;

import com.esc.lickerz.lickerz_sep.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 요청은 인증 없이 통과
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        /**
         * @Note
         * 토큰 검증
         * 토큰이 유효하면 토큰에서 사용자 정보를 추출하여 요청에 추가
         * 토큰이 유효하지 않으면 401 Unauthorized 상태 코드 반환
         */
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsername(token);
                    String role = jwtUtil.getRole(token);
                    UUID userId = jwtUtil.getUserId(token);
                    request.setAttribute("username", username);
                    request.setAttribute("role", role);
                    request.setAttribute("userId", userId);
                    return true;
                }
            } catch (ExpiredJwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("토큰이 만료되었습니다.");
                return false;
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
