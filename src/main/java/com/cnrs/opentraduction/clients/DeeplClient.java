package com.cnrs.opentraduction.clients;

import com.deepl.api.Translator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;


@Slf4j
@Service
public class DeeplClient {

    @Value("${deepl.key}")
    private String deeplKey;


    public String translate(String value, String fromLang, String toLang) {
        try {
            log.info("Début de la traduction de '{}' du {} en {}", value, fromLang, toLang);

            if (StringUtils.isEmpty(value)) {
                log.info("Le text fourni est absent !");
                return "NaN";
            }

            if (StringUtils.isEmpty(deeplKey)) {
                log.info("La clé Deepl est absente dans la configuration du projet !");
                return "NaN";
            }

            var translator = new Translator(deeplKey);
            var result = translator.translateText(value, fromLang, toLang);
            if (!ObjectUtils.isEmpty(result)) {
                log.info("Traduction effectuée avec succée");
                return result.getText().trim();
            } else {
                log.error("Aucune traduction trouvée pour le mot '{}' du {} en {}", value, fromLang, toLang);
                return "";
            }
        } catch (Exception ex) {
            log.error("Erreur pendant la traduction du '{}' du {} en {}", value, fromLang, toLang);
            return "NaN";
        }
    }
    
}
