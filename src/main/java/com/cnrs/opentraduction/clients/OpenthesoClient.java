package com.cnrs.opentraduction.clients;

import com.cnrs.opentraduction.models.client.opentheso.candidat.CandidateModel;
import com.cnrs.opentraduction.models.client.opentheso.collection.CollectionModel;
import com.cnrs.opentraduction.models.client.opentheso.concept.ConceptModel;
import com.cnrs.opentraduction.models.client.opentheso.proposition.PropositionModel;
import com.cnrs.opentraduction.models.client.opentheso.collection.SubCollectionModel;
import com.cnrs.opentraduction.models.client.opentheso.thesaurus.ThesaurusModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Data
@Service
@AllArgsConstructor
public class OpenthesoClient {

    private final RestTemplate restTemplate;

    public ThesaurusModel[] getThesaurusInformations(String baseUrl) {

        var url = baseUrl + "/openapi/v1/thesaurus";
        log.info("URL : " + url);
        return restTemplate.getForObject(url, ThesaurusModel[].class);
    }

    public CollectionModel[] getCollections(String baseUrl, String idThesaurus, String idLang) {

        var url = String.format("%s/openapi/v1/concept/search/groups/%s/%s", baseUrl, idThesaurus, idLang);
        log.info("URL : " + url);
        return restTemplate.getForObject(url, CollectionModel[].class);
    }

    public SubCollectionModel[] getSousCollections(String baseUrl, String idThesaurus, String idGroup) {

        var url = baseUrl + "/openapi/v1/group/"+idThesaurus+"/"+idGroup+"/subgroup";
        log.info("URL : " + url);
        return restTemplate.getForObject(url, SubCollectionModel[].class);
    }

    public ConceptModel[] searchTerm(String baseUrl, String idThesaurus, String termToSearch, String idLang, String idGroup) {
        var groupParam = "";
        if (!StringUtils.isEmpty(idGroup)) {
            groupParam = "&group=" + idGroup;
        }
        var url = String.format("%s/openapi/v1/concept/search/%s/%s?lang=%s" + groupParam,
                baseUrl, idThesaurus, termToSearch, idLang);
        log.info("URL : " + url);
        return restTemplate.getForObject(url, ConceptModel[].class);
    }

    public void saveCandidat(String baseUrl, String userApiKey, CandidateModel candidate) {

        var request = new HttpEntity<>(candidate, createHttpHeaders(userApiKey));
        var url = baseUrl + "/openapi/v1/candidate";
        restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    public void saveProposition(String baseUrl, String userApiKey, PropositionModel proposition) {

        var request = new HttpEntity<>(proposition, createHttpHeaders(userApiKey));
        var url = baseUrl + "/openapi/v1/concepts/propositions";
        restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    private HttpHeaders createHttpHeaders(String userApiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("API-KEY", userApiKey);
        return headers;
    }
}
