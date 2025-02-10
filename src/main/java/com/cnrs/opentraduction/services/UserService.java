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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import jakarta.faces.application.FacesMessage;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Data
@Service
public class UserService {

    @Value("${admin.apikey}")
    private String adminApiKey;

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

                var refInstances = referenceInstanceRepository.findAll();
                if (!CollectionUtils.isEmpty(refInstances)) {
                    var userFromApi = userClient.generateApiKey(refInstances.get(0).getUrl(), userFromOpentheso.getIdUser(), adminApiKey);
                    userFromDB.get().setApiKey(userFromApi.block().getApiKey());
                    userFromDB.get().setIdUserOpenTheso(userFromOpentheso.getIdUser());
                    return userRepository.save(userFromDB.get());
                } else {
                    messageService.showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant la génération de l'API Key, veuillez contacter votre administrateur.");
                    return null;
                }
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
                    .lastName(userFromOpentheso.getName())
                    .active(true)
                    .defaultTargetTraduction("Francais")
                    .mail(userFromOpentheso.getMail())
                    .created(LocalDateTime.now())
                    .modified(LocalDateTime.now())
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
        var refInstances = referenceInstanceRepository.findAll();

        var users = userClient.searchUsers(refInstances.get(0).getUrl(), null, userFromDB.getLogin(), adminApiKey);
        if (!CollectionUtils.isEmpty(users.block())) {
            log.error("Erreur : Login '{}'existe déjà !", userFromDB.getLogin());
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg13");
            return null;
        }

        users = userClient.searchUsers(refInstances.get(0).getUrl(), userFromDB.getMail(), null, adminApiKey);
        if (!CollectionUtils.isEmpty(users.block())) {
            log.error("Erreur : Mail '{}'existe déjà !", userFromDB.getLogin());
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg14");
            return null;
        }

        var userDao = createUserDao(userFromDB, refInstances);
        var userCreated = userClient.createUser(refInstances.get(0).getUrl(), userDao, adminApiKey).block();
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

        boolean createCase = false;
        if (ObjectUtils.isEmpty(userToSave.getId())) {
            log.info("Cas d'un nouveau utilisateur !");

            log.info("Vérification du mail");
            var user = userRepository.findByMail(userToSave.getMail());
            if (user.isPresent()) {
                messageService.showMessage(FacesMessage.SEVERITY_ERROR, "user.settings.error.msg9");
                return false;
            }

            createCase = true;
            userToSave.setCreated(LocalDateTime.now());
        }

        userToSave.setModified(LocalDateTime.now());

        var refInstances = referenceInstanceRepository.findAll();
        var userDao = createUserDao(userToSave, refInstances);

        if (createCase) {
            var users = userClient.searchUsers(refInstances.get(0).getUrl(), null, userToSave.getLogin(), apiKey);
            if (!CollectionUtils.isEmpty(users.block())) {
                log.error("Erreur : Login '{}'existe déjà !", userToSave.getLogin());
                messageService.showMessage(FacesMessage.SEVERITY_ERROR,
                        String.format("L'utilisateur %s ne peut pas être crée car le login existe déjà dans OpenTheso, Veuillez contacter votre adinistrateur.", userToSave.getLogin()));
                return false;
            }

            users = userClient.searchUsers(refInstances.get(0).getUrl(), userToSave.getMail(), null, apiKey);
            if (!CollectionUtils.isEmpty(users.block())) {
                log.error("Erreur : Mail '{}'existe déjà !", userToSave.getLogin());
                messageService.showMessage(FacesMessage.SEVERITY_ERROR,
                        String.format("L'utilisateur %s ne peut pas être crée car le mail existe déjà dans OpenTheso, Veuillez contacter votre adinistrateur.", userToSave.getLogin()));
                return false;
            }

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
        if (userClient.deleteUser(user.getGroup().getReferenceInstances().getUrl(), user.getIdUserOpenTheso(), apiKey).block()) {
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
