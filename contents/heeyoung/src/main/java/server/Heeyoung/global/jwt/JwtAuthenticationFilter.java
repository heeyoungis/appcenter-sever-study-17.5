package server.Heeyoung.global.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // JWT 검증을 건너뛸 경로들
    private static final String[] EXCLUDE_PATHS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/users/sign-in",
            "/users/sign-up",
            "/users/reissue"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return java.util.Arrays.stream(EXCLUDE_PATHS)
                .anyMatch(exclude -> path.startsWith(exclude.replace("**", "")));
    }

    // header 에서 토큰 추출하기 위한 메서드
    private String getTokenFromRequest(HttpServletRequest req) {
        String token = req.getHeader("Authorization");
        if (token==null) {
            return null;
        }
        return token.substring(7); // "Bearer " 이후 토큰 부분
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // request header 에서 jwt 토큰 추출
            String token = getTokenFromRequest(request);

            // 토큰 유효성 검사
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 토큰 유효하다면 인층 객체 생성
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                // SecurityContext 에 인증 객체 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

//            // 다음 필터로 이동
//            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.warn("JWT 검증 실패: {}", e.getMessage());
        } catch (Exception e) {
            log.error("인증 처리 중 오류 발생: {}", e.getMessage());
        } finally {
            filterChain.doFilter(request, response);
        }
    }
}
