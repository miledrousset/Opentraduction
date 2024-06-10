package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.exception.BusinessException;
import com.cnrs.opentraduction.models.ConnexionModel;
import com.cnrs.opentraduction.repositories.UserRepository;
import com.cnrs.opentraduction.utils.MessageUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.faces.application.FacesMessage;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;


    public Users authentification(ConnexionModel connexionModel) {

        if (StringUtils.isEmpty(connexionModel.getPassword()) && StringUtils.isEmpty(connexionModel.getPassword())) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le login et le mot de passe sont obligatoires !");
            return null;
        }

        if (StringUtils.isEmpty(connexionModel.getPassword())) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le mot de passe est obligatoire !");
            return null;
        }

        if (StringUtils.isEmpty(connexionModel.getLogin())) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le login est obligatoire !");
            return null;
        }

        var user = userRepository.findByLoginAndPassword(connexionModel.getLogin(), connexionModel.getPassword());

        if (user.isPresent()) {
            if (user.get().isActive()) {
                return user.get();
            } else {
                MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le compte utilisateur est désactivé !");
                return null;
            }
        } else {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Login et/ou mot de passe erronés !");
            return null;
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

    public void deleteUser(Users user) {
        userRepository.delete(user);
    }
}
