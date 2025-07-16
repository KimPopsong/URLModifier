package bigmac.urlmodifierbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UrlModifierBackendApplication {
    public static void main(String[] args)
    {
        SpringApplication.run(UrlModifierBackendApplication.class, args);
    }
}

/**
 * TODO
 * <p>
 *  1. 사용자 및 링크 관리 기능
 * <p>
 * 현재는 누구나 익명으로 URL을 생성하지만, 사용자가 자신의 링크를 체계적으로 관리하게 만들면 서비스의 가치가 크게 올라갑니다.
 * <p>
 * <p>
 * * 사용자 계정 시스템 (User Accounts):
 * * 기능: 이메일/비밀번호나 소셜 로그인(Google, GitHub 등)을 통해 사용자가 가입하고 로그인할 수 있습니다.
 * * 장점: 사용자는 자신이 생성한 모든 단축 URL을 하나의 대시보드에서 모아보고 관리할 수 있습니다. 이는 아래에 설명될 다른 기능들의 기반이 됩니다.
 * <p>
 * <p>
 * * 사용자 지정 단축 URL (Custom Short URLs):
 * * 기능: base-url/abcdef 형태의 무작위 문자열 대신, base-url/my-event-2025 와 같이 사용자가 직접 원하는 문자열로 단축 URL을 만들 수 있게 합니다.
 * * 장점: 브랜드 노출에 유리하고 기억하기 쉬워 마케팅용으로 매우 유용합니다.
 * <p>
 * <p>
 * * 링크 만료 기능 (Link Expiration):
 * * 기능: URL을 생성할 때 만료 날짜/시간을 설정하거나, 특정 클릭 횟수에 도달하면 링크가 비활성화되도록 설정합니다.
 * * 장점: 기간 한정 이벤트나 캠페인, 공유 파일 접근 제어 등에 유용합니다.
 * <p>
 * <p>
 * * 링크 수정 및 삭제 (Edit & Delete Links):
 * * 기능: 사용자가 생성한 단축 URL이 가리키는 원본 URL 주소를 나중에 변경하거나, 필요 없어진 단축 URL을 삭제할 수 있습니다.
 * * 장점: 잘못된 링크를 수정하거나, 캠페인 종료 후 링크를 정리하는 등 유연한 관리가 가능해집니다.
 * <p>
 * 2. 분석 및 통계 기능
 * <p>
 * <p>
 * 단순히 리디렉션만 해주는 것을 넘어, 링크를 통해 발생하는 데이터를 사용자에게 제공하면 매우 강력한 기능이 됩니다.
 * <p>
 * <p>
 * * 클릭 통계 분석 (Click Analytics):
 * * 기능: 각 단축 URL별로 발생한 클릭에 대한 상세한 통계를 제공합니다.
 * * 총 클릭 수: 링크가 몇 번 클릭되었는지 보여줍니다.
 * * 시간대별 클릭 수: 시간, 일, 주, 월 단위로 클릭 수 변화를 그래프로 시각화합니다.
 * * 리퍼러(Referrer) 분석: 사용자들이 어떤 웹사이트(예: Facebook, Twitter, Google 검색)를 통해 유입되었는지 보여줍니다.
 * * 지역별 통계: 어느 국가나 도시에서 클릭이 발생했는지 IP 주소를 기반으로 분석하여 보여줍니다.
 * * 기기/브라우저 통계: 사용자들이 모바일/데스크톱 중 어떤 기기를 사용했는지, 어떤 웹 브라우저(Chrome, Safari 등)를 사용했는지 분석합니다.
 * * 장점: 마케팅 캠페인의 성과를 측정하고 사용자 행동을 파악하는 데 결정적인 데이터를 제공합니다.
 * <p>
 * <p>
 * 3. 보안 및 고급 기능
 * <p>
 * 서비스의 신뢰도를 높이고 전문적인 사용자를 유치하기 위한 기능입니다.
 * <p>
 * <p>
 * * 악성 URL 탐지 (Malicious URL Detection):
 * * 기능: 사용자가 단축하려는 원본 URL이 피싱 사이트나 멀웨어를 유포하는 악성 사이트인지 확인합니다. (예: Google Safe Browsing API 연동)
 * * 장점: 서비스를 이용하는 최종 사용자를 위험한 링크로부터 보호하여 서비스 전체의 신뢰도를 높입니다.
 * <p>
 * <p>
 * * 비밀번호 보호 링크 (Password-Protected Links):
 * * 기능: 단축 URL을 클릭했을 때, 설정된 비밀번호를 입력해야만 원본 URL로 리디렉션되도록 합니다.
 * * 장점: 특정 인원에게만 공유하고 싶은 비공개 문서나 자료를 안전하게 전달할 수 있습니다.
 * <p>
 * <p>
 * * API 제공 (Developer API):
 * * 기능: 다른 개발자들이 자신의 애플리케이션에서 프로그래밍 방식으로 URL을 단축하고 관리할 수 있도록 REST API를 제공합니다. 사용자별로 API 키를 발급하여 인증합니다.
 * * 장점: 서비스의 활용 범위를 크게 넓혀 다른 서비스와의 통합을 가능하게 합니다.
 *
 * 타 단축 사이트 링크는 불가
 * 
 * 로봇인가요? -> 로그인 한 유저는 체크 안하기
 */