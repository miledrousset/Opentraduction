package com.cnrs.opentraduction.clients;

import com.cnrs.opentraduction.models.client.opentheso.candidat.CandidateModel;
import com.cnrs.opentraduction.models.client.opentheso.collection.CollectionModel;
import com.cnrs.opentraduction.models.client.opentheso.concept.ConceptModel;
import com.cnrs.opentraduction.models.client.opentheso.proposition.PropositionModel;
import com.cnrs.opentraduction.models.client.opentheso.collection.SubCollectionModel;
import com.cnrs.opentraduction.models.client.opentheso.thesaurus.ThesaurusModel;
import com.cnrs.opentraduction.models.dao.NodeIdValue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


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
        log.info("URL : " + url);
        restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    public void saveProposition(String baseUrl, String userApiKey, PropositionModel proposition) {

        var request = new HttpEntity<>(proposition, createHttpHeaders(userApiKey));
        var url = baseUrl + "/openapi/v1/concepts/propositions";
        log.info("URL : " + url);
        restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    public List<NodeIdValue> searchConceptByGroup(String baseUrl, String idThesaurus, String idGroup) {
        try {
            var url = String.format("%s/api/all/group?theso=%s&id=%s&format=rdf", baseUrl, idThesaurus, idGroup);
            log.info("URL : " + url);
            var model = fetchRDFData(url);
            // Query the RDF data to extract skos:prefLabel and idc fields
            var sparqlQuery = """
            PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
            PREFIX dcterms: <http://purl.org/dc/terms/>
            SELECT ?prefLabel ?idc
            WHERE {
                ?s skos:prefLabel ?prefLabel .
                OPTIONAL { ?s dcterms:identifier ?idc . }
            }
        """;
            return executeQuery(model, sparqlQuery);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private Model fetchRDFData(String apiUrl) throws Exception {

        var connection = (HttpURLConnection) new URL(apiUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/rdf+xml");

        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
        }

        Model model = ModelFactory.createDefaultModel();
        model.read(connection.getInputStream(), null);
        connection.disconnect();

        return model;
    }

    private List<NodeIdValue> executeQuery(Model model, String sparqlQuery) {
        List<NodeIdValue> result = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(sparqlQuery), model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                var concept = new NodeIdValue();
                var solution = results.nextSolution();

                var prefLabelNode = solution.getLiteral("prefLabel");
                if (prefLabelNode != null) {
                    concept.setPrefLabel(prefLabelNode.getString());
                }

                var idcNode = solution.get("idc");
                if (idcNode != null) {
                    concept.setIdConcept(idcNode.toString());
                }

                result.add(concept);
            }
        }
        return result;
    }

    private HttpHeaders createHttpHeaders(String userApiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("API-KEY", userApiKey);
        return headers;
    }
}
