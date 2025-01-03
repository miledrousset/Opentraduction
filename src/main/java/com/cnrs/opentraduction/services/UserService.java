package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.exception.BusinessException;
import com.cnrs.opentraduction.models.dao.ConnexionDto;
import com.cnrs.opentraduction.repositories.UserRepository;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import jakarta.faces.application.FacesMessage;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private MessageService messageService;
    private UserRepository userRepository;


    public Users authentification(ConnexionDto connexionModel) {

        if (StringUtils.isEmpty(connexionModel.getPassword()) && StringUtils.isEmpty(connexionModel.getPassword())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg6");
            return null;
        }

        if (StringUtils.isEmpty(connexionModel.getPassword())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg3");
            return null;
        }

        if (StringUtils.isEmpty(connexionModel.getLogin())) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg2");
            return null;
        }

        var user = userRepository.findByLoginAndPassword(connexionModel.getLogin(), connexionModel.getPassword());

        if (user.isPresent()) {
            if (user.get().isActive()) {
                return user.get();
            } else {
                messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg8");
                return null;
            }
        } else {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg7");
            return null;
        }
    }

    public boolean saveUser(Users userToSave) {

        if (ObjectUtils.isEmpty(userToSave.getId())) {
            log.info("Cas d'un nouveau utilisateur !");

            log.info("Vérification du mail");
            var user = userRepository.findByMail(userToSave.getMail());
            if (user.isPresent()) {
                messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg9");
                return false;
            }

            userToSave.setCreated(LocalDateTime.now());
        }

        userToSave.setModified(LocalDateTime.now());

        log.info("Enregistrement dans la base");
        userRepository.save(userToSave);

        return true;
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Users user) {
        userRepository.deleteUserById(user.getId());
    }

    public Users getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(String.format("Utilisateur id %d n'existe pas !!!", userId)));
    }

    public List<Users> getUsersByGroup(Integer groupId) {
        return userRepository.findAllByGroupId(groupId);
    }
}
