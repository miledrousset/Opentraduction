package com.cnrs.opentraduction.clients;

import com.cnrs.opentraduction.models.client.CandidateModel;
import com.cnrs.opentraduction.models.client.CollectionModel;
import com.cnrs.opentraduction.models.client.ConceptModel;
import com.cnrs.opentraduction.models.client.PropositionModel;
import com.cnrs.opentraduction.models.client.ThesaurusModel;
import com.cnrs.opentraduction.models.client.TopCollectionModel;

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

    public ThesaurusModel[] getThesoInfo(String baseUrl) {
        var url = baseUrl + "/api/info/list?theso=all";
        log.info("URL : " + url);
        return restTemplate.getForObject(url, ThesaurusModel[].class);
    }

    public TopCollectionModel[] getTopCollections(String baseUrl, String idThesaurus) {
        var url = baseUrl + "/api/info/list?theso=" + idThesaurus + "&topconcept=all";
        log.info("URL : " + url);
        return restTemplate.getForObject(url, TopCollectionModel[].class);
    }

    public CollectionModel[] getCollections(String baseUrl, String idThesaurus, String topCollectionAll) {
        var url = baseUrl + "/api/info/list?theso=" + idThesaurus + "&topconcept=" + topCollectionAll + "&group=all";
        log.info("URL : " + url);
        return restTemplate.getForObject(url, CollectionModel[].class);
    }

    public CollectionModel[] getSousCollections(String baseUrl, String idThesaurus, String idGroup) {
        var url = baseUrl + "/openapi/v1/group/"+idThesaurus+"/"+idGroup+"/subgroup";
        log.info("URL : " + url);
        return restTemplate.getForObject(url, CollectionModel[].class);
    }

    public ConceptModel[] searchTerm(String baseUrl, String idThesaurus, String termToSearch, String idLang, String idGroup) {
        var groupParam = "";
        if (!StringUtils.isEmpty(idGroup)) {
            groupParam = "&group=" + idGroup;
        }
        var url = String.format("%s/openapi/v1/concept/%s/autocomplete/%s/full?lang=%s" + groupParam,
                baseUrl, idThesaurus, termToSearch, idLang, idGroup);
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
