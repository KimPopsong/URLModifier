package bigmac.urlmodifierbackend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${custom.FE_BASE_URL}")
    private String FRONT_BASE_URL;

    @Bean
    public PasswordEncoder passwordEncoder()  // TODO : SecurityConfig에 둘 시 순환 참조 문제 발생
    {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 애플리케이션의 모든 경로에 대해 CORS 설정을 적용
            .allowedOrigins(FRONT_BASE_URL)  // 요청을 허용할 출처 지정
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")  // 허용할 HTTP 메서드 지정
            .allowedHeaders("*")  // 모든 종류의 HTTP 헤더 허용
            .allowCredentials(true)  // 자격 증명(쿠키 등)을 포함한 요청 허용
            .maxAge(3600);  // Pre-flight 요청의 결과를 캐시할 시간을 초 단위로 지정
    }
}
