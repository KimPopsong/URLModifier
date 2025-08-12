package bigmac.urlmodifierbackend.domain.user.service;

import bigmac.urlmodifierbackend.domain.user.dto.request.UserLoginRequest;
import bigmac.urlmodifierbackend.domain.user.dto.request.UserRegisterRequest;
import bigmac.urlmodifierbackend.domain.user.exception.EmailNotExistsException;
import bigmac.urlmodifierbackend.domain.user.exception.LoginFailException;
import bigmac.urlmodifierbackend.domain.user.exception.EmailAlreadyExistsException;
import bigmac.urlmodifierbackend.domain.user.model.User;
import bigmac.urlmodifierbackend.domain.user.repository.UserRepository;
import bigmac.urlmodifierbackend.global.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final PasswordEncoder passwordEncoder;

    /**
     * 이메일의 존재 여부 확인
     *
     * @param email 사용하는 이메일
     * @return 이메일이 존재할 경우 true, 아닐시 false
     */
    @Override
    public Boolean isEmailExist(String email)
    {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    @Override
    public void registerUser(UserRegisterRequest userRegisterRequest)
    {
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

    @Override
    public User loginUser(UserLoginRequest userLoginRequest)
    {
        Optional<User> result = userRepository.findByEmail(userLoginRequest.getEmail());

        if (result.isEmpty())
        {
            throw new EmailNotExistsException("존재하지 않는 이메일입니다.");
        }

        else if (passwordEncoder.matches(userLoginRequest.getPassword(), result.get().getPassword()))
        {
            return result.get();
        }

        else
        {
            throw new LoginFailException("비밀번호가 일치하지 않습니다.");
        }
    }
}
