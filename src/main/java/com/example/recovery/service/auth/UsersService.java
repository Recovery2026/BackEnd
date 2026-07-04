package com.example.recovery.service.auth;

import com.example.recovery.common.exception.EmailAlreadyExistException;
import com.example.recovery.domain.user.UserCredential;
import com.example.recovery.domain.user.Users;
import com.example.recovery.repository.users.UserCredentialRepository;
import com.example.recovery.repository.users.UsersRepository;
import com.example.recovery.request.auth.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;

    public void signup(SignupRequest request) {
        if (userCredentialRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistException("이미 존재하는 이메일입니다.");
        }

        UserCredential credential = new UserCredential();
        Users users = new Users();
        users.setNickname(request.getNickname());

        credential.setEmail(request.getEmail());
        credential.setPassword(passwordEncoder.encode(request.getPassword()));
        credential.setUsers(users);

        usersRepository.save(users);
        userCredentialRepository.save(credential);
    }
}
