package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.User;
import com.cnrs.opentraduction.exception.BusinessException;
import com.cnrs.opentraduction.repositories.UserRepository;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;


    public User authentification(String login, String password) throws NotFoundException {

        var user = userRepository.findByLoginAndPassword(login, password);

        if (user.isPresent()) {
            if (user.get().isActive()) {
                return user.get();
            } else {
                throw new BusinessException("Le compte utilisateur est désactivé !");
            }
        } else {
            throw new NotFoundException("Login et/ou mot de passe erronés !");
        }
    }
}
