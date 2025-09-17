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
 * * 링크 만료 기능 (Link Expiration):
 * * 기능: URL을 생성할 때 만료 날짜/시간을 설정하거나, 특정 클릭 횟수에 도달하면 링크가 비활성화되도록 설정합니다.
 * * 장점: 기간 한정 이벤트나 캠페인, 공유 파일 접근 제어 등에 유용합니다.
 * <p>
 * 3. 보안 및 고급 기능
 * <p>
 * 서비스의 신뢰도를 높이고 전문적인 사용자를 유치하기 위한 기능입니다.
 * <p>
 * * 악성 URL 탐지 (Malicious URL Detection):
 * * 기능: 사용자가 단축하려는 원본 URL이 피싱 사이트나 멀웨어를 유포하는 악성 사이트인지 확인합니다. (예: Google Safe Browsing API 연동)
 * * 장점: 서비스를 이용하는 최종 사용자를 위험한 링크로부터 보호하여 서비스 전체의 신뢰도를 높입니다.
 * <p>
 * * 비밀번호 보호 링크 (Password-Protected Links):
 * * 기능: 단축 URL을 클릭했을 때, 설정된 비밀번호를 입력해야만 원본 URL로 리디렉션되도록 합니다.
 * * 장점: 특정 인원에게만 공유하고 싶은 비공개 문서나 자료를 안전하게 전달할 수 있습니다.
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