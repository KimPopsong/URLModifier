package bigmac.urlmodifierbackend.global.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    //    private final RedisTemplate<String, String> redisTemplate;  // Redis
    private final long EXPIRATION_TIME = 1000 * 60 * 60;  // 액세스 토큰 만료 시간 : 1시간
    private final long REFRESH_TIME = 1000 * 60 * 60 * 24 * 7;  // 리프레시 토큰 만료 시간 : 7일

    @Value("${jwt.secret}")
    private String SECRET_KEY;  // JWT 비밀 키

    private SecretKey getSignInKey()
    {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * AccessToken 생성
     *
     * @param email
     * @return
     */
    public String generateAccessToken(String email)
    {
        return Jwts.builder().subject(email).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)).signWith(this.getSignInKey()).compact();
    }

    /**
     * RefreshToken 생성
     *
     * @param email
     * @return
     */
    public String generateRefreshToken(String email)
    {
        String refreshToken = Jwts.builder().subject(email).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + REFRESH_TIME)).signWith(this.getSignInKey()).compact();

        // Redis에 refresh 토큰 저장 (key: refresh:{email}, TTL: 7일)
        //        redisTemplate.opsForValue().set("refresh:" + email, refreshToken, REFRESH_TIME, TimeUnit.MILLISECONDS);

        //        System.out.println("[리프레시 토큰 저장됨] 이메일: " + email + ", 토큰: " + refreshToken);

        return refreshToken;
    }

    /**
     * RefreshToken 삭제
     * <p>
     * 로그아웃시 호출
     *
     * @param email
     */
    public void deleteRefreshToken(String email)
    {
        //        redisTemplate.delete("refresh:" + email);
    }

    /**
     * AccessToken을 BlackList에 등록
     *
     * @param token
     */
    public void addToBlackList(String token)
    {
        long ttl = getRemainingTime(token);

        //        redisTemplate.opsForValue().set("blacklist:" + token, "logout", ttl, TimeUnit.MILLISECONDS);  // Redis에 BlackList 등록
    }

    /**
     * BlackList 확인
     *
     * @param token
     * @return
     */
    public boolean isBlacklisted(String token)
    {
        //        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
        return false;
    }


    /**
     * 사용자 이메일 추출
     *
     * @param token JWT 문자열
     * @return email (subject 필드)
     */
    public String getUserEmail(String token)
    {
        return Jwts.parser().setSigningKey(getSignInKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    /**
     * 토큰 유효성 검사
     *
     * @param token
     * @return
     */
    public boolean validateToken(String token)
    {
        try  // 토큰을 파싱해보고 문제가 없으면 유효하다고 판단
        {
            Jwts.parser().setSigningKey(getSignInKey()).build().parseSignedClaims(token);

            return true;
        }

        catch (ExpiredJwtException e)
        {
            System.out.println("Token Expired : " + e.getMessage());
        }

        catch (UnsupportedJwtException e)
        {
            System.out.println("Unsupported Token : " + e.getMessage());
        }

        catch (MalformedJwtException e)
        {
            System.out.println("Invalid Token Format: " + e.getMessage());
        }

        catch (SecurityException e)
        {
            System.out.println("Token Signature Error : " + e.getMessage());
        }

        catch (IllegalArgumentException e)
        {
            System.out.println("Parameter Error : " + e.getMessage());
        }

        return false;
    }

    /**
     * 토큰의 만료 시간 계산
     *
     * @param token
     * @return
     */
    public long getRemainingTime(String token)
    {
        Date expiration = Jwts.parser().setSigningKey(getSignInKey()).build().parseSignedClaims(token).getPayload().getExpiration();

        return expiration.getTime() - System.currentTimeMillis();
    }
}