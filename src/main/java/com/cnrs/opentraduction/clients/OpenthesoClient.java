package com.cnrs.opentraduction.clients;

import com.cnrs.opentraduction.models.client.CollectionModel;
import com.cnrs.opentraduction.models.client.ThesaurusModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class OpenthesoClient {

    @Autowired
    private RestTemplate restTemplate;

    public ThesaurusModel[] getThesoInfo(String baseUrl) {
        var url = baseUrl + "/api/info/list?theso=all";
        return restTemplate.getForObject(url, ThesaurusModel[].class);
    }

    public CollectionModel[] getTopCollections(String baseUrl, String idThesaurus) {
        var url = baseUrl + "/api/info/list?theso=" + idThesaurus + "&topconcept=all";
        return restTemplate.getForObject(url, CollectionModel[].class);
    }

    public CollectionModel[] getCollections(String baseUrl, String idThesaurus, String topCollectionAll) {
        var url = baseUrl + "/api/info/list?theso=" + idThesaurus + "&topconcept=" + topCollectionAll + "&group=all";
        return restTemplate.getForObject(url, CollectionModel[].class);
    }
}
