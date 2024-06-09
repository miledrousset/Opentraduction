package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.exception.BusinessException;
import com.cnrs.opentraduction.repositories.UserRepository;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;


    public Users authentification(String login, String password) throws NotFoundException {

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

    public void saveUser(Users userSelected) {

        if (StringUtils.isEmpty(userSelected.getMail())) {
            throw new BusinessException("Le mail est obligatoire !");
        }

        if (StringUtils.isEmpty(userSelected.getPassword())) {
            throw new BusinessException("Le mot de passe est obligatoire !");
        }

        if (StringUtils.isEmpty(userSelected.getLogin())) {
            throw new BusinessException("Le nom d'utilisateur est obligatoire !");
        }

        if (ObjectUtils.isEmpty(userSelected.getId())) {
            log.info("Cas d'un nouveau utilisateur !");

            log.info("Vérification du mail");
            var user = userRepository.findByMail(userSelected.getMail());
            if (user.isPresent()) {
                throw new BusinessException("Erreur de création d'un nouveau utilisateur : E-mail existe déjà !");
            }

            log.info("Vérification du password");
            user = userRepository.findByPassword(userSelected.getPassword());
            if (user.isPresent()) {
                throw new BusinessException("Erreur de création d'un nouveau utilisateur : Password existe déjà !");
            }

            userSelected.setCreated(LocalDateTime.now());
        } else {
            log.info("Cas d'un utilisateur déjà existant !");
        }

        userSelected.setModified(LocalDateTime.now());

        log.info("Enregistrement dans la base");
        userRepository.save(userSelected);
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }
}
