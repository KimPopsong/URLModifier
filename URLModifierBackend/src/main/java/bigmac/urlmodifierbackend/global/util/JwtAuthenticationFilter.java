package bigmac.urlmodifierbackend.global.util;

import bigmac.urlmodifierbackend.domain.user.model.User;
import bigmac.urlmodifierbackend.domain.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        @Nullable HttpServletResponse response, @Nullable FilterChain filterChain)
        throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        String jwt;
        String userEmail = null;

        // Authorization 헤더가 있고 Bearer로 시작하면 토큰 추출
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);

            // 블랙 리스트 확인
            if (jwtUtil.isBlacklisted(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Access token is blackListed.");

                return;
            }

            // 토큰 유효성 검증 및 사용자 정보 추출
            try {
                if (jwtUtil.validateToken(jwt)) {
                    userEmail = jwtUtil.getUserEmail(jwt);
                }
            } catch (ExpiredJwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"message\": \"Access token expired\"}");

                return;
            }
        }

        // 인증정보가 없고 userEmail 존재하면 인증처리 진행
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // UserRepository에서 유저 정보 로드
            User user = userRepository.findByEmail(userEmail)
                .orElse(null);

            if (user != null) {
                // User 엔티티를 Authentication에 설정
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 다음 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }
}
