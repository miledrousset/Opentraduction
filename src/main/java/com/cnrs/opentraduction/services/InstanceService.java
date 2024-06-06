package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.entities.Instances;
import com.cnrs.opentraduction.exception.BusinessException;
import com.cnrs.opentraduction.repositories.InstanceRepository;
import com.cnrs.opentraduction.utils.MessageUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.faces.application.FacesMessage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;


@Slf4j
@Service
@AllArgsConstructor
public class InstanceService {

    private InstanceRepository instanceRepository;


    public void saveInstance(Instances instanceSelected) {

        if (StringUtils.isEmpty(instanceSelected.getName())) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le nom est absent !");
            throw new BusinessException("Le nom est obligatoire !");
        }

        if (StringUtils.isEmpty(instanceSelected.getUrl())) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "L'URL est absente !");
            throw new BusinessException("L'URL est obligatoire !");
        }


        if (!isValidURL(instanceSelected.getUrl())) {
            throw new BusinessException("Erreur de création d'un nouveau utilisateur : E-mail existe déjà !");
        }

        if (ObjectUtils.isEmpty(instanceSelected.getCreated())) {
            instanceSelected.setCreated(LocalDateTime.now());
        }
        instanceSelected.setModified(LocalDateTime.now());

        log.info("Enregistrement dans la base");
        instanceRepository.save(instanceSelected);
    }

    private boolean isValidURL(String urlString) {
        try {
            var url = new URL(urlString);
            if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) {
                return false;
            }
            url.toURI();

            // Ouvre une connexion à l'URL et vérifie la réponse
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000); // Timeout de connexion
            connection.setReadTimeout(3000); // Timeout de lecture
            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400);
        } catch (MalformedURLException e) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Le format de l'URL n'est pas bon! ");
            return false;
        } catch (IOException e) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "L'URL n'est pas accessible ! ");
            return false;
        } catch (Exception e) {
            MessageUtil.showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant la vérification de l'URL ! ");
            return false;
        }
    }
}
