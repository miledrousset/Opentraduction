package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.exception.BusinessException;
import com.cnrs.opentraduction.models.ConnexionModel;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.repositories.UserRepository;
import com.cnrs.opentraduction.repositories.UserThesaurusRepository;
import com.cnrs.opentraduction.utils.MessageUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private UserThesaurusRepository userThesaurusRepository;


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

    public boolean saveUser(Users userSelected) {

        if (ObjectUtils.isEmpty(userSelected.getId())) {
            log.info("Cas d'un nouveau utilisateur !");

            log.info("Vérification du mail");
            var user = userRepository.findByMail(userSelected.getMail());
            if (user.isPresent()) {
                MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Erreur de création d'un nouveau utilisateur : E-mail existe déjà !");
                return false;
            }

            log.info("Vérification du password");
            user = userRepository.findByPassword(userSelected.getPassword());
            if (user.isPresent()) {
                MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Erreur de création d'un nouveau utilisateur : Password existe déjà !");
                return false;
            }

            userSelected.setCreated(LocalDateTime.now());
        }

        userSelected.setModified(LocalDateTime.now());

        log.info("Enregistrement dans la base");
        userRepository.save(userSelected);

        return true;
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Users user) {
        userRepository.delete(user);
    }

    public Users getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(String.format("Utilisateur id %d n'existe pas !!!", userId)));
    }

    public boolean addThesaurusToUser(Integer userId, Thesaurus thesaurus, CollectionElementDao collection) {

        var userThesaurus = userThesaurusRepository.findByThesaurusIdAndUserId(thesaurus.getId(), userId);

        if (userThesaurus.isEmpty()) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "La collection n'existe pas !");
            return false;
        }

        if (ObjectUtils.isEmpty(collection)) {
            log.info("L'utilisateur n'a pas choisie une sous-collection");
            userThesaurus.get().setCollection(thesaurus.getCollection());
            userThesaurus.get().setCollectionId(thesaurus.getIdCollection());
        } else {
            log.info("L'utilisateur a choisie une sous-collection ");
            userThesaurus.get().setCollectionId(collection.getId());
            userThesaurus.get().setCollection(collection.getLabel());
        }

        userThesaurusRepository.save(userThesaurus.get());

        return true;
    }

    @Transactional
    public boolean deleteConsultationCollection(Integer thesaurusId, Integer userId) {

        userThesaurusRepository.deleteByThesaurusIdAndUserId(thesaurusId, userId);
        return true;
    }
}
