package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.entities.UsersThesaurus;
import com.cnrs.opentraduction.exception.BusinessException;
import com.cnrs.opentraduction.models.dao.ConnexionDto;
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

            log.info("Vérification du password");
            user = userRepository.findByPassword(userToSave.getPassword());
            if (user.isPresent()) {
                messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg10");
                return false;
            }

            userToSave.setCreated(LocalDateTime.now());
        }

        userToSave.setModified(LocalDateTime.now());

        boolean createNewThesaurusAssociation = true;
        if (!ObjectUtils.isEmpty(userToSave.getId())) {
            var userTmp = userRepository.findById(userToSave.getId());
            if (userTmp.isPresent()) {
                if (userTmp.get().getGroup().getId().intValue() != userToSave.getId().intValue()) {
                    log.info("Suppression des anciens thésaurus liées à l'utilisateur en vue de lui associés les nouvelles associations !");
                    userThesaurusRepository.deleteByUserId(userToSave.getId());
                } else {
                    log.info("Nous somme dans le cas d'une mise à jour et le group n'a pas changé !");
                    createNewThesaurusAssociation = false;
                }
            }
        }

        log.info("Enregistrement dans la base");
        var userSaved = userRepository.save(userToSave);

        if (createNewThesaurusAssociation) {
            log.info("Nouvel utilisateur ou le groupe de l'utilisateur à changé, donc enregistrement des références...");
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

        log.info("Recherche du projet de référence pour l'utilisateur id {}", userId);
        var userThesaurus = userThesaurusRepository.findByThesaurusIdAndUserId(thesaurus.getId(), userId);

        if (userThesaurus.isEmpty()) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg11");
            return false;
        }

        log.info("Enregistrement du nouveau projet de référence");
        userThesaurus.get().setCollectionId(collection.getId());
        userThesaurus.get().setCollection(collection.getLabel());
        userThesaurus.get().setModified(LocalDateTime.now());

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

    public List<Users> getUsersByGroup(String groupName) {
        return userRepository.findAllByGroupName(groupName);
    }
}
