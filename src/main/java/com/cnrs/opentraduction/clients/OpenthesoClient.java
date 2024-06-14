package com.cnrs.opentraduction.clients;

import com.cnrs.opentraduction.models.CollectionModel;
import com.cnrs.opentraduction.models.ThesaurusModel;
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

    public CollectionModel[] getCollectionsByThesaurus(String baseUrl, String idThesaurus) {
        var url = baseUrl + "/api/info/list?theso=" + idThesaurus + "&group=all";
        return restTemplate.getForObject(url, CollectionModel[].class);
    }
}
