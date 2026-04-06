package bigmac.urlmodifierbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UrlModifierBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlModifierBackendApplication.class, args);
    }
}
/**
 * TODO
 * <p>
 * * 악성 URL 탐지 (Malicious URL Detection):
 * * 기능: 사용자가 단축하려는 원본 URL이 피싱 사이트나 멀웨어를 유포하는 악성 사이트인지 확인합니다. (예: Google Safe Browsing API 연동)
 * * 장점: 서비스를 이용하는 최종 사용자를 위험한 링크로부터 보호하여 서비스 전체의 신뢰도를 높입니다.
 * <p>
 * * API 제공 (Developer API):
 * * 기능: 다른 개발자들이 자신의 애플리케이션에서 프로그래밍 방식으로 URL을 단축하고 관리할 수 있도록 REST API를 제공합니다. 사용자별로 API 키를 발급하여 인증합니다.
 * * 장점: 서비스의 활용 범위를 크게 넓혀 다른 서비스와의 통합을 가능하게 합니다.
 * <p>
 * 로봇인가요? -> 로그인 한 유저는 체크 안하기
 * <p>
 * 최근 URL 랭킹
 * <p>
 * 테스트 코드 넣기
 */