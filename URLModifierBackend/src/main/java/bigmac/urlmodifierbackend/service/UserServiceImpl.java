package bigmac.urlmodifierbackend.service;

import bigmac.urlmodifierbackend.dto.UserRegisterRequest;
import bigmac.urlmodifierbackend.exception.UserAlreadyExistsException;
import bigmac.urlmodifierbackend.model.User;
import bigmac.urlmodifierbackend.repository.UserRepository;
import bigmac.urlmodifierbackend.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
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

    @Override
    public User registerUser(UserRegisterRequest userRegisterRequest)
    {
        if (isEmailExist(userRegisterRequest.getEmail()))  // 이미 있는 회원이라면
        {
            throw new UserAlreadyExistsException("이미 존재하는 이메일입니다.");
        }

        User user = new User();

        user.setId(idGenerator.nextId());  // snowflake 활용 id 생성
        user.setEmail(userRegisterRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRegisterRequest.getPassword()));

        userRepository.save(user);

        return user;
    }
}
