package ru.t1.java.demo.service;

import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.model.User;

public interface UserService {
    User getUser(Long id);
    User updateUser(Long id, User user);
}
