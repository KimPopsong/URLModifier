package bigmac.urlmodifierbackend.domain.user.service;

import bigmac.urlmodifierbackend.domain.user.dto.request.UserLoginRequest;
import bigmac.urlmodifierbackend.domain.user.dto.request.UserRegisterRequest;
import bigmac.urlmodifierbackend.domain.user.dto.response.JwtResponse;
import bigmac.urlmodifierbackend.domain.user.dto.response.UserLoginResponse;
import bigmac.urlmodifierbackend.domain.user.exception.EmailAlreadyExistsException;
import bigmac.urlmodifierbackend.domain.user.exception.LoginFailException;
import bigmac.urlmodifierbackend.domain.user.model.User;
import bigmac.urlmodifierbackend.domain.user.repository.UserRepository;
import bigmac.urlmodifierbackend.global.util.JwtUtil;
import bigmac.urlmodifierbackend.global.util.SnowflakeIdGenerator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SnowflakeIdGenerator idGenerator;
    private final PasswordEncoder passwordEncoder;

    /**
     * 이메일의 존재 여부 확인
     *
     * @param email 사용하는 이메일
     * @return 이메일이 존재할 경우 true, 아닐시 false
     */
    @Override
    public Boolean isEmailExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * 회원가입
     *
     * @param userRegisterRequest 이메일과 비밀번호
     */
    @Transactional
    @Override
    public void registerUser(UserRegisterRequest userRegisterRequest) {
        if (isEmailExist(userRegisterRequest.getEmail()))  // 이미 있는 회원이라면
        {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다.");
        }

        User user = new User();

        user.setId(idGenerator.nextId());  // snowflake 활용 id 생성
        user.setEmail(userRegisterRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRegisterRequest.getPassword()));

        userRepository.save(user);
    }

    /**
     * 로그인
     *
     * @param userLoginRequest 이메일과 비밀번호
     * @return UserLoginResponse 토큰 발급 및 반환
     */
    @Transactional
    @Override
    public UserLoginResponse loginUser(UserLoginRequest userLoginRequest) {
        Optional<User> result = userRepository.findByEmail(userLoginRequest.getEmail());

        if (result.isEmpty()) {  // 사용자 없음
            throw new LoginFailException("이메일이 존재하지 않거나, 비밀번호가 일치하지 않습니다.");
        } else if (passwordEncoder.matches(userLoginRequest.getPassword(),
            result.get().getPassword())) {
            User user = result.get();

            JwtResponse jwtResponse = new JwtResponse(jwtUtil.generateAccessToken(user.getEmail()),
                jwtUtil.generateRefreshToken(user.getEmail()));

            UserLoginResponse userLoginResponse = new UserLoginResponse();

            userLoginResponse.setUserId(String.valueOf(user.getId()));
            userLoginResponse.setEmail(user.getEmail());
            userLoginResponse.setJwtResponse(jwtResponse);

            return userLoginResponse;
        } else {  // 비밀번호 불일치
            throw new LoginFailException("이메일이 존재하지 않거나, 비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * 리프레시 토큰으로 JWT 재발급
     *
     * @param request HttpOnly 쿠키
     * @return 새로운 accessToken, refreshToken 발급
     */
    @Transactional
    @Override
    public JwtResponse refreshToken(HttpServletRequest request) {
        String refreshToken = Arrays.stream(
                Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
            .filter(cookie -> "refresh_token".equals(cookie.getName())).map(Cookie::getValue)
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰 없음"));

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰");
        }

        String email = jwtUtil.getUserEmail(refreshToken);

        // Redis에 저장된 refresh 토큰과 일치하는지 확인
        String storedRefreshToken = (String) redisTemplate.opsForValue().get("refresh:" + email);

        if (!refreshToken.equals(storedRefreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "리프레시 토큰 불일치");
        }

        return new JwtResponse(jwtUtil.generateAccessToken(email),
            jwtUtil.generateRefreshToken(email));
    }

    /**
     * 로그아웃
     *
     * @param authorizationHeader JWT
     */
    @Transactional
    @Override
    public void logoutUser(String authorizationHeader) {
        String accessToken = authorizationHeader.substring(7);

        jwtUtil.addToBlackList(accessToken);
        jwtUtil.deleteRefreshToken(jwtUtil.getUserEmail(accessToken));
    }
}
