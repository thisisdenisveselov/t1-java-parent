package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.exception.EntityNotFoundException;
import ru.t1.java.demo.model.User;
import ru.t1.java.demo.repository.UserRepository;
import ru.t1.java.demo.service.UserService;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @LogDataSourceError
    @Override
    public User getUser(Long id) {
        return userRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException(String.format("%s with id = %d not found", "User", id)));
    }

    @Transactional
    @Override
    public User updateUser(Long id, User user) {
        User updatedUser = getUser(id);

        if (user.getClientId() != null)
            updatedUser.setClientId(user.getClientId());

        return userRepository.save(updatedUser);
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No user is authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}
