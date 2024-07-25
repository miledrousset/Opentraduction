package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.models.client.CandidateModel;
import com.cnrs.opentraduction.models.client.ConceptModel;
import com.cnrs.opentraduction.models.client.ElementModel;
import com.cnrs.opentraduction.models.client.PropositionModel;
import com.cnrs.opentraduction.models.client.ThesaurusElementModel;
import com.cnrs.opentraduction.models.dao.CollectionDao;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;

import com.cnrs.opentraduction.models.dao.ConceptDao;
import com.cnrs.opentraduction.utils.MessageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.thymeleaf.util.ArrayUtils;

import javax.faces.application.FacesMessage;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
@AllArgsConstructor
public class ThesaurusService {

    private final static String FR = "fr";

    private final MessageService messageService;
    private final OpenthesoClient openthesoClient;


    public List<ConceptDao> searchConcepts(Thesaurus thesaurus, String url, String termValue, String languageToSearch, String idCollection) {

        try {
            var referenceResult = openthesoClient.searchTerm(url, thesaurus.getIdThesaurus(), termValue, languageToSearch, idCollection);

            log.info("Résultat trouvée dans le Thésaurus de référence : " + referenceResult.length);
            return Arrays.stream(referenceResult)
                    .map(element -> toConceptDao(element, thesaurus.getName(), thesaurus.getIdThesaurus(),
                            StringUtils.isEmpty(thesaurus.getReferenceInstances())
                                    ? thesaurus.getConsultationInstances().getUrl()
                                    : thesaurus.getReferenceInstances().getUrl()))
                    .collect(Collectors.toList());
        } catch (HttpClientErrorException e) {
            handleClientError(e, "application.thesaurus.error.msg5");
            return List.of();
        } catch (HttpServerErrorException e) {
            handleServerError(e, "application.thesaurus.error.msg5");
            return List.of();
        } catch (Exception ex) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.thesaurus.error.msg5");
            return List.of();
        }
    }

    public boolean saveCandidat(CandidateModel candidate, String baseUrl, String userApiKey) {

        try {
            openthesoClient.saveCandidat(baseUrl, userApiKey, candidate);
            return true;
        } catch (HttpClientErrorException e) {
            handleClientError(e, "application.thesaurus.error.msg4");
            return false;
        } catch (HttpServerErrorException e) {
            handleServerError(e, "application.thesaurus.error.msg4");
            return false;
        } catch (Exception ex) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.thesaurus.error.msg4");
            return false;
        }
    }

    public void saveProposition(PropositionModel proposition, String baseUrl, String userApiKey) {

        try {
            openthesoClient.saveProposition(baseUrl, userApiKey, proposition);
        } catch (HttpClientErrorException e) {
            handleClientError(e, "application.thesaurus.error.msg3");
        } catch (HttpServerErrorException e) {
            handleServerError(e, "application.thesaurus.error.msg3");
        } catch (Exception ex) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.thesaurus.error.msg3");
        }
    }

    public List<ThesaurusElementModel> searchThesaurus(String baseUrl) {

        try {
            var thesaurusResponse = openthesoClient.getThesaurusInformations(baseUrl);
            if (!ArrayUtils.isEmpty(thesaurusResponse)) {
                return Stream.of(thesaurusResponse)
                        .filter(element -> !ObjectUtils.isEmpty(element.getLabels()))
                        .filter(element -> element.getLabels().stream().anyMatch(tmp -> FR.equals(tmp.getLang())))
                        .filter(element -> element.getLabels().stream().anyMatch(tmp -> FR.equals(tmp.getLang())))
                        .map(element -> new ThesaurusElementModel(element.getIdTheso(),
                                element.getLabels().stream()
                                        .filter(tmp -> FR.equals(tmp.getLang()))
                                        .findFirst()
                                        .get()
                                        .getTitle()))
                        .collect(Collectors.toList());
            } else {
                return List.of();
            }
        } catch (HttpClientErrorException e) {
            handleClientError(e, "application.thesaurus.error.msg1");
            return List.of();
        } catch (HttpServerErrorException e) {
            handleServerError(e, "application.thesaurus.error.msg1");
            return List.of();
        } catch (Exception ex) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.thesaurus.error.msg1");
            return List.of();
        }
    }

    public List<CollectionElementDao> searchCollections(String baseUrl, String idThesaurus) {

        try {
            var collections = openthesoClient.getCollections(baseUrl, idThesaurus, "fr");

            if (!ArrayUtils.isEmpty(collections)) {
                return Stream.of(collections)
                        .map(element -> new CollectionElementDao(element.getConceptGroup().getIdgroup(), element.getLexicalValue()))
                        .collect(Collectors.toList());
            } else {
                return List.of();
            }
        } catch (HttpClientErrorException e) {
            handleClientError(e, "application.thesaurus.error.msg2");
            return List.of();
        } catch (HttpServerErrorException e) {
            handleServerError(e, "application.thesaurus.error.msg2");
            return List.of();
        } catch (Exception ex) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.thesaurus.error.msg2");
            return List.of();
        }
    }

    public List<CollectionElementDao> searchSubCollections(String baseUrl, String idThesaurus, String idCollection) {

        try {
            var collectionsResponse = openthesoClient.getSousCollections(baseUrl, idThesaurus, idCollection);
            if (collectionsResponse.length > 0) {
                return Stream.of(collectionsResponse)
                        .filter(element -> element.getLabels().stream().anyMatch(tmp -> FR.equals(tmp.getLang())))
                        .map(element -> {
                            var label = element.getLabels().stream()
                                    .filter(tmp -> FR.equals(tmp.getLang()))
                                    .findFirst();

                            if (label.isPresent()) {
                                return new CollectionElementDao(element.getIdGroup(), label.get().getTitle());
                            } else {
                                return new CollectionElementDao(element.getIdGroup(), "");
                            }
                        })
                        .collect(Collectors.toList());
            } else {
                return List.of();
            }
        } catch (HttpClientErrorException e) {
            handleClientError(e, "application.thesaurus.error.msg2");
            return List.of();
        } catch (HttpServerErrorException e) {
            handleServerError(e, "application.thesaurus.error.msg2");
            return List.of();
        } catch (Exception ex) {
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.thesaurus.error.msg2");
            return List.of();
        }
    }

    private ConceptDao toConceptDao(ConceptModel conceptModel, String thesaurusName, String thesaurusId, String baseUrl) {

        return ConceptDao.builder()
                .conceptId(conceptModel.getIdConcept())
                .status(conceptModel.getStatus())
                .thesaurusId(thesaurusId)
                .thesaurusName(thesaurusName)
                .collections(conceptModel.getCollections().stream()
                        .map(element -> CollectionDao.builder().id(element.getId()).name(element.getValue()).build())
                        .collect(Collectors.toList()))
                .labelFr(getConceptTerm(conceptModel, "fr"))
                .labelAr(getConceptTerm(conceptModel, "ar"))
                .definitionFr(getConceptDefinition(conceptModel, "fr"))
                .definitionAr(getConceptDefinition(conceptModel, "ar"))
                .varianteAr(getConceptSynonyme(conceptModel, "ar"))
                .varianteFr(getConceptSynonyme(conceptModel, "fr"))
                .noteFr(getConceptNote(conceptModel, "fr"))
                .noteAr(getConceptNote(conceptModel, "ar"))
                .url(String.format("%s/?idc=%s&idt=%s", baseUrl, conceptModel.getIdConcept(), thesaurusId))
                .build();
    }

    private String getConceptTerm(ConceptModel conceptModel, String lang) {
        return conceptModel.getTerms().stream()
                .filter(element -> lang.equals(element.getLang()))
                .map(ElementModel::getValue)
                .findFirst()
                .orElse("");
    }

    private String getConceptSynonyme(ConceptModel conceptModel, String lang) {
        return conceptModel.getSynonymes().stream()
                .filter(element -> lang.equals(element.getLang()))
                .map(ElementModel::getValue)
                .findFirst()
                .orElse("");
    }

    private String getConceptDefinition(ConceptModel conceptModel, String lang) {
        return conceptModel.getDefinitions().stream()
                .filter(element -> lang.equals(element.getLang()))
                .map(ElementModel::getValue)
                .findFirst()
                .orElse("");
    }

    private String getConceptNote(ConceptModel conceptModel, String lang) {
        return conceptModel.getNotes().stream()
                .filter(element -> lang.equals(element.getLang()))
                .map(ElementModel::getValue)
                .findFirst()
                .orElse("");
    }

    private void handleClientError(HttpClientErrorException e, String genericMessageKey) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.error("Error 404: Resource not found.");
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.thesaurus.error.msg6");
        } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
            log.error("Error 403: Access forbidden.");
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, "application.thesaurus.error.msg7");
        } else {
            log.error("Client error: " + e.getStatusCode());
            messageService.showMessage(FacesMessage.SEVERITY_ERROR, genericMessageKey);
        }
    }

    private void handleServerError(HttpServerErrorException e, String genericMessageKey) {
        log.error("Server error: " + e.getStatusCode());
        messageService.showMessage(FacesMessage.SEVERITY_ERROR, genericMessageKey);
    }
}
