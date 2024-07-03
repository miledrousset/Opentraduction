package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.clients.OpenthesoClient;
import com.cnrs.opentraduction.models.client.CollectionModel;
import com.cnrs.opentraduction.models.client.ThesaurusElementModel;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.thymeleaf.util.ArrayUtils;

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
    }

    public List<CollectionElementDao> searchTopCollections(String baseUrl, String idThesaurus) {
        var topCollectionsResponse = openthesoClient.getTopCollections(baseUrl, idThesaurus);

        if (topCollectionsResponse.length > 0) {
            return Stream.of(topCollectionsResponse)
                    .filter(element -> element.getLabels().stream().anyMatch(tmp -> FR.equals(tmp.getLang())))
                    .map(element -> {
                        var label = element.getLabels().stream()
                                .filter(tmp -> FR.equals(tmp.getLang()))
                                .findFirst().orElse(null);

                        return new CollectionElementDao(element.getIdConcept(),
                                ObjectUtils.isEmpty(label) ? "" : label.getTitle());
                    })
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    public List<CollectionElementDao> searchSubCollections(String baseUrl, String idThesaurus, String idTopCollection) {
        var collectionsResponse = openthesoClient.getSousCollections(baseUrl, idThesaurus, idTopCollection);
        if (collectionsResponse.length > 0) {
            return Stream.of(collectionsResponse)
                    .filter(element -> element.getLabels().stream().anyMatch(tmp -> FR.equals(tmp.getLang())))
                    .map(element -> new CollectionElementDao(element.getIdGroup(), element.getLabels().stream()
                            .filter(tmp -> FR.equals(tmp.getLang()))
                            .findFirst()
                            .get()
                            .getTitle()))
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    private List<CollectionElementDao> getCollectionDatas(CollectionModel[] collectionsResponse) {
        if (collectionsResponse.length > 0) {
            return Stream.of(collectionsResponse)
                    .filter(element -> element.getLabels().stream().anyMatch(tmp -> FR.equals(tmp.getLang())))
                    .map(element -> new CollectionElementDao(element.getIdGroup(), element.getLabels().stream()
                            .filter(tmp -> FR.equals(tmp.getLang()))
                            .findFirst().get()
                            .getTitle()))
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }
}
