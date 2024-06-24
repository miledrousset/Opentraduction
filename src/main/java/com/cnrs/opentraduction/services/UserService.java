package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.entities.UsersThesaurus;
import com.cnrs.opentraduction.exception.BusinessException;
import com.cnrs.opentraduction.models.ConnexionModel;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.repositories.UserRepository;
import com.cnrs.opentraduction.repositories.UserThesaurusRepository;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.faces.application.FacesMessage;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private MessageService messageService;
    private UserRepository userRepository;
    private UserThesaurusRepository userThesaurusRepository;


    public Users authentification(ConnexionModel connexionModel) {

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

    public boolean saveUser(Users userSelected) {

        if (ObjectUtils.isEmpty(userSelected.getId())) {
            log.info("Cas d'un nouveau utilisateur !");

            log.info("Vérification du mail");
            var user = userRepository.findByMail(userSelected.getMail());
            if (user.isPresent()) {
                messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg9");
                return false;
            }

            log.info("Vérification du password");
            user = userRepository.findByPassword(userSelected.getPassword());
            if (user.isPresent()) {
                messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg10");
                return false;
            }

            userSelected.setCreated(LocalDateTime.now());
        }

        userSelected.setModified(LocalDateTime.now());

        log.info("Enregistrement dans la base");
        var userSaved = userRepository.save(userSelected);

        if (!ObjectUtils.isEmpty(userSelected.getId())) {
            log.info("Nouvel utilisateur, donc enregistrement des références...");
            if (!ObjectUtils.isEmpty(userSaved.getGroup().getReferenceInstances())) {
                log.info("Associer l'instance de référence au nouvel utilisateur");
                var userThesaurus = new UsersThesaurus();
                userThesaurus.setUserId(userSaved.getId());
                userThesaurus.setThesaurusId(userSaved.getGroup().getReferenceInstances().getThesaurus().getId());
                userThesaurus.setCollectionId(userSaved.getGroup().getReferenceInstances().getThesaurus().getIdCollection());
                userThesaurus.setCollection(userSaved.getGroup().getReferenceInstances().getThesaurus().getCollection());
                userThesaurus.setCreated(LocalDateTime.now());
                userThesaurus.setModified(LocalDateTime.now());
                userThesaurusRepository.save(userThesaurus);
            }

            if (!CollectionUtils.isEmpty(userSaved.getGroup().getConsultationInstances())) {
                log.info("Associer l'instance de consultation au nouvel utilisateur");
                userSaved.getGroup().getConsultationInstances().forEach(consultation -> {
                    if (!CollectionUtils.isEmpty(consultation.getThesauruses())) {
                        consultation.getThesauruses().forEach(thesaurus -> {
                            var userThesaurus = new UsersThesaurus();
                            userThesaurus.setUserId(userSaved.getId());
                            userThesaurus.setThesaurusId(thesaurus.getId());
                            userThesaurus.setCollectionId(thesaurus.getIdCollection());
                            userThesaurus.setCollection(thesaurus.getCollection());
                            userThesaurus.setCreated(LocalDateTime.now());
                            userThesaurus.setModified(LocalDateTime.now());
                            userThesaurusRepository.save(userThesaurus);
                        });
                    }
                });
            }
        }

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
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg11");
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

    public List<UsersThesaurus> getUserConsultationCollections(Integer userId) {

        return userThesaurusRepository.getAllByUserId(userId);
    }
}
