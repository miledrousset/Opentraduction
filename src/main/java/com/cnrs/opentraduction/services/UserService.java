package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.clients.UserClient;
import com.cnrs.opentraduction.entities.ReferenceInstances;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.exception.BusinessException;
import com.cnrs.opentraduction.models.client.opentheso.users.UserModel;
import com.cnrs.opentraduction.models.dao.ConnexionDao;
import com.cnrs.opentraduction.models.dao.UserDao;
import com.cnrs.opentraduction.repositories.ReferenceInstanceRepository;
import com.cnrs.opentraduction.repositories.UserRepository;
import com.cnrs.opentraduction.utils.MD5Password;
import com.cnrs.opentraduction.utils.MessageService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import jakarta.faces.application.FacesMessage;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final UserClient userClient;
    private final OpenthesoClient openthesoClient;
    private final ReferenceInstanceRepository referenceInstanceRepository;


    public Users authentification(ConnexionDao connexionModel) {

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

        log.info("Search user in OpenThesorus");
        var userFromOpentheso = searchUserInOpentheso(connexionModel);
        var userFromDB = userRepository.findByLoginAndPassword(connexionModel.getLogin(), connexionModel.getPassword());

        if (ObjectUtils.isEmpty(userFromOpentheso) && userFromDB.isPresent()) {
            log.info("User found in DB only -> creation user in OpenTheso DB");
            return saveUserInOpenTheso(userFromDB.get());
        } else if (ObjectUtils.isEmpty(userFromOpentheso) && userFromDB.isEmpty()) {
            log.error("User not found in Opentheso and DB !");
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg7");
            return null;
        }

        if (userFromDB.isPresent() && userFromOpentheso.isActive()) {
            log.info("User found in opentheso and DB, authentification OK !");
            if (userFromDB.get().getApiKey() == null
                    || !userFromDB.get().getApiKey().equals(userFromOpentheso.getApiKey())
                    || userFromDB.get().getIdUserOpenTheso() == null) {
                userFromDB.get().setApiKey(userFromOpentheso.getApiKey());
                userFromDB.get().setIdUserOpenTheso(userFromOpentheso.getIdUser());
                userRepository.save(userFromDB.get());
            }
            return userFromDB.get();
        } else if (userFromDB.isEmpty() && userFromOpentheso.isActive()) {
            log.info("First connexion for " + userFromOpentheso.getName());
            return userRepository.save(Users.builder()
                    .admin(userFromOpentheso.isSuperAdmin())
                    .login(connexionModel.getLogin())
                    .password(connexionModel.getPassword())
                    .apiKey(userFromOpentheso.getApiKey())
                    .firstName(userFromOpentheso.getName())
                    .mail(userFromOpentheso.getMail())
                    .idUserOpenTheso(userFromOpentheso.getIdUser())
                    .build());
        } else {
            log .info("User exist but not actif !");
            userFromDB.get().setActive(false);
            userRepository.save(userFromDB.get());
            return null;
        }
    }

    private UserModel searchUserInOpentheso(ConnexionDao connexionModel) {
        var refInstances = referenceInstanceRepository.findAll();

        var connexionDto = new ConnexionDao();
        connexionDto.setPassword(MD5Password.encodedPassword(connexionModel.getPassword()));
        connexionDto.setLogin(connexionModel.getLogin());
        if (!CollectionUtils.isEmpty(refInstances)) {
            var user = userClient.authentification(refInstances.get(0).getUrl(), connexionDto).block();
            return user.getIdUser() == 0 ? null : user;
        }
        return null;
    }

    private Users saveUserInOpenTheso(Users userFromDB) {
        var admin = userRepository.findAll().stream().filter(user -> user.isAdmin()).findFirst().get();
        var refInstances = referenceInstanceRepository.findAll();
        var userDao = createUserDao(userFromDB, refInstances);
        var userCreated = userClient.createUser(refInstances.get(0).getUrl(), userDao, admin.getApiKey()).block();
        if (userCreated == null) {
            log.error("Erreur pendant l'enregistrement de l'utilisateur dans OpenTheso !");
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg0");
            return null;
        }
        userFromDB.setApiKey(userCreated.getApiKey());
        userFromDB.setIdUserOpenTheso(userCreated.getIdUser());
        return userRepository.save(userFromDB);
    }

    private UserDao createUserDao(Users userFromDB, List<ReferenceInstances> refInstances) {
        var thesaurusList = openthesoClient.getThesaurusInformations(refInstances.get(0).getUrl());
        var idProject = Arrays.stream(thesaurusList).toList().stream()
                .filter(item -> item.getLabels().stream().anyMatch(label -> "fr".equals(label.getLang()) && "SHS en traduction".equals(label.getTitle())))
                .map(item -> item.getIdTheso())
                .findFirst()
                .orElse(null);
        return UserDao.builder()
                .active(true)
                .alertMail(false)
                .superAdmin(userFromDB.isAdmin())
                .password(MD5Password.encodedPassword(userFromDB.getPassword()))
                .login(userFromDB.getLogin())
                .mail(userFromDB.getMail())
                .idProject(idProject)
                .idRole(userFromDB.isAdmin() ? 2 : 4)
                .idThesaurus(refInstances.get(0).getThesaurus().getIdThesaurus())
                .build();
    }

    public boolean saveUser(Users userToSave, String apiKey) {

        boolean isUpdate = true;
        if (ObjectUtils.isEmpty(userToSave.getId())) {
            log.info("Cas d'un nouveau utilisateur !");

            log.info("Vérification du mail");
            var user = userRepository.findByMail(userToSave.getMail());
            if (user.isPresent()) {
                messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg9");
                return false;
            }

            isUpdate = false;
            userToSave.setCreated(LocalDateTime.now());
        }

        userToSave.setModified(LocalDateTime.now());

        var refInstances = referenceInstanceRepository.findAll();
        var userDao = createUserDao(userToSave, refInstances);

        if (!isUpdate) {
            var userFromOpenTheso = userClient.createUser(refInstances.get(0).getUrl(), userDao, apiKey).block();
            if (userFromOpenTheso != null) {
                log.info("Enregistrement dans la base");
                userToSave.setIdUserOpenTheso(userFromOpenTheso.getIdUser());
                userToSave.setApiKey(userFromOpenTheso.getApiKey());
                userRepository.save(userToSave);
                return true;
            }
        } else {
            var oldUser = userRepository.findById(userToSave.getId()).get();
            if (userClient.updateUser(refInstances.get(0).getUrl(), oldUser.getIdUserOpenTheso(), userDao, apiKey)) {
                log.info("Enregistrement dans la base");
                userRepository.save(userToSave);
                return true;
            }
        }

        return false;
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Users user, String apiKey) {

        if (userClient.deleteUser(user.getGroup().getReferenceInstances().getUrl(), user.getLogin(), user.getPassword(), apiKey).block()) {
            userRepository.deleteUserById(user.getId());
        }
    }

    public Users getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(String.format("Utilisateur id %d n'existe pas !!!", userId)));
    }

    public List<Users> getUsersByGroup(Integer groupId) {
        return userRepository.findAllByGroupId(groupId);
    }
}
