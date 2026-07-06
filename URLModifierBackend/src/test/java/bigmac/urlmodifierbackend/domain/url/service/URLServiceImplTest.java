package bigmac.urlmodifierbackend.domain.url.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bigmac.urlmodifierbackend.domain.url.dto.request.URLRequest;
import bigmac.urlmodifierbackend.domain.url.exception.URLException;
import bigmac.urlmodifierbackend.domain.url.exception.URLExpiredException;
import bigmac.urlmodifierbackend.domain.url.model.URL;
import bigmac.urlmodifierbackend.domain.url.repository.ClickEventRepository;
import bigmac.urlmodifierbackend.domain.url.repository.URLRepository;
import bigmac.urlmodifierbackend.domain.user.model.User;
import bigmac.urlmodifierbackend.domain.user.repository.UserRepository;
import bigmac.urlmodifierbackend.global.util.Base62;
import bigmac.urlmodifierbackend.global.util.SnowflakeIdGenerator;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class URLServiceImplTest {

    @Mock
    private URLValidateServiceImpl urlValidateService;
    @Mock
    private URLRepository urlRepository;
    @Mock
    private ClickEventRepository clickEventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private ValueOperations<String, String> stringValueOperations;

    private URLServiceImpl urlService;

    @BeforeEach
    void setUp() {
        urlService = new URLServiceImpl(urlValidateService, urlRepository, clickEventRepository,
            userRepository, idGenerator, redisTemplate, stringRedisTemplate);
        ReflectionTestUtils.setField(urlService, "BE_BASE_URL", "http://localhost:8080/");
        ReflectionTestUtils.setField(urlService, "FE_BASE_URL", "http://localhost:5173/");
    }

    @Test
    @DisplayName("단축 문자열이 이미 존재하면 새 ID를 발급해 재생성한다")
    void makeURLShort_regeneratesIdOnSlugCollision() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(urlRepository.findFirstByOriginURLAndUserIsNull("https://example.com"))
            .thenReturn(Optional.empty());
        when(idGenerator.nextId()).thenReturn(1L, 2L);
        when(urlRepository.findByShortenedURL(Base62.encode(1L)))
            .thenReturn(Optional.of(new URL()));
        when(urlRepository.findByShortenedURL(Base62.encode(2L)))
            .thenReturn(Optional.empty());
        when(urlRepository.save(any(URL.class))).thenAnswer(inv -> inv.getArgument(0));

        URLRequest request = new URLRequest();
        request.setUrl("https://example.com");

        URL saved = urlService.makeURLShort(null, request);

        assertThat(saved.getId()).isEqualTo(2L);
        assertThat(saved.getShortenedURL()).isEqualTo(Base62.encode(2L));
        verify(idGenerator, times(2)).nextId();
    }

    @Test
    @DisplayName("만료 시각이 지난 URL로 리다이렉트하면 URLExpiredException이 발생한다")
    void redirectToOriginal_throwsWhenExpired() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);  // 캐시 미스

        URL url = new URL(1L, null, "https://example.com", "abc", "qr");
        url.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(urlRepository.findByShortenedURL("abc")).thenReturn(Optional.of(url));

        assertThatThrownBy(() -> urlService.redirectToOriginal(null, null, null, "abc"))
            .isInstanceOf(URLExpiredException.class);
    }

    @Test
    @DisplayName("최대 클릭 수에 도달한 URL로 리다이렉트하면 URLExpiredException이 발생한다")
    void redirectToOriginal_throwsWhenMaxClicksReached() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);  // 캐시 미스
        when(stringRedisTemplate.opsForValue()).thenReturn(stringValueOperations);
        when(stringValueOperations.get("url:clicks:1")).thenReturn("3");

        URL url = new URL(1L, null, "https://example.com", "abc", "qr");
        url.setMaxClicks(3);
        when(urlRepository.findByShortenedURL("abc")).thenReturn(Optional.of(url));

        assertThatThrownBy(() -> urlService.redirectToOriginal(null, null, null, "abc"))
            .isInstanceOf(URLExpiredException.class);
    }

    @Test
    @DisplayName("유효한 URL로 리다이렉트하면 클릭 이벤트가 저장된다")
    void redirectToOriginal_savesClickEvent() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);  // 캐시 미스

        URL url = new URL(1L, null, "https://example.com", "abc", "qr");
        when(urlRepository.findByShortenedURL("abc")).thenReturn(Optional.of(url));
        when(urlRepository.getReferenceById(1L)).thenReturn(url);

        URL result = urlService.redirectToOriginal("ref", "agent", "127.0.0.1", "abc");

        assertThat(result.getOriginURL()).isEqualTo("https://example.com");
        verify(clickEventRepository).save(any());
    }

    @Test
    @DisplayName("다른 사용자의 URL을 삭제하려 하면 URLException이 발생한다")
    void deleteUrl_throwsWhenNotOwner() {
        User owner = new User(1L, "owner", "owner@test.com", "pw");
        User requester = new User(2L, "other", "other@test.com", "pw");

        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));

        URL url = new URL(10L, owner, "https://example.com", "abc", "qr");
        when(urlRepository.findById(10L)).thenReturn(Optional.of(url));

        assertThatThrownBy(() -> urlService.deleteUrl(requester, 10L))
            .isInstanceOf(URLException.class);
    }
}
