package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.models.client.ThesaurusElementModel;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
@AllArgsConstructor
public class ThesaurusService {

    private final static String FR = "fr";

    private final OpenthesoClient openthesoClient;


    public List<ThesaurusElementModel> searchThesaurus(String baseUrl) {
        var thesaurusResponse = openthesoClient.getThesoInfo(baseUrl);
        if (thesaurusResponse.length > 0) {
            return Stream.of(thesaurusResponse)
                    .filter(element -> element.getLabels().stream().anyMatch(tmp -> FR.equals(tmp.getLang())))
                    .map(element -> {
                        var label = element.getLabels().stream()
                                .filter(tmp -> FR.equals(tmp.getLang()))
                                .findFirst().orElse(null);
                        return new ThesaurusElementModel(element.getIdTheso(),
                                ObjectUtils.isEmpty(label) ? "" : label.getTitle());
                    })
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    public List<CollectionElementDao> searchCollections(String baseUrl, String idThesaurus) {
        var collectionsResponse = openthesoClient.getCollectionsByThesaurus(baseUrl, idThesaurus);
        if (collectionsResponse.length > 0) {
            return Stream.of(collectionsResponse)
                    .filter(element -> element.getLabels().stream().anyMatch(tmp -> FR.equals(tmp.getLang())))
                    .map(element -> {
                        var label = element.getLabels().stream()
                                .filter(tmp -> FR.equals(tmp.getLang()))
                                .findFirst().orElse(null);

                        return new CollectionElementDao(element.getIdGroup(),
                                ObjectUtils.isEmpty(label) ? "" : label.getTitle());
                    })
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }
}
