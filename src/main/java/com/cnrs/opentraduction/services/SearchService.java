package com.cnrs.opentraduction.services;

import com.cnrs.opentraduction.clients.GeoNamesClient;
import com.cnrs.opentraduction.clients.IdRefClient;
import com.cnrs.opentraduction.clients.WikidataClient;
import com.cnrs.opentraduction.entities.ConsultationInstances;
import com.cnrs.opentraduction.entities.Thesaurus;
import com.cnrs.opentraduction.entities.Users;
import com.cnrs.opentraduction.models.client.wikidata.AliasDTO;
import com.cnrs.opentraduction.models.client.wikidata.WikidataModel;
import com.cnrs.opentraduction.models.dao.CollectionElementDao;
import com.cnrs.opentraduction.models.dao.ConceptDao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class SearchService {

    private final static String ALL = "ALL";

    private final IdRefClient rdRefClient;
    private final GeoNamesClient geoNamesClient;
    private final WikidataClient wikidataClient;
    private final ThesaurusService thesaurusService;


    public List<ConceptDao> geoNamesSearch(String termValue, String languageToSearch) throws IOException, ParserConfigurationException, InterruptedException, SAXException {
        var result = geoNamesClient.searchValue(termValue, languageToSearch);

        if (CollectionUtils.isEmpty(result)) {
            return List.of();
        } else {
            return result.stream()
                    .map(element -> ConceptDao.builder()
                            .labelFr(element.getAlternateNameFr())
                            .labelAr(element.getAlternateNameAr())
                            .url(String.format("https://www.geonames.org/%s/%s.html", element.getGeonameId(), element.getAlternateNameFr()))
                            .build())
                    .toList();
        }
    }

    public List<ConceptDao> wikiDataSearch(String termValue, String languageToSearch) {
        var tmp = wikidataClient.searchEntities(termValue, languageToSearch);
        if (!ObjectUtils.isEmpty(tmp) && !CollectionUtils.isEmpty(tmp.getSearchResults())) {
            var result = wikidataClient.getEntitiesDetails(tmp.getSearchResults().stream()
                    .map(WikidataModel.SearchResult::getId)
                    .toList());

            if (!CollectionUtils.isEmpty(result)) {
                return result.stream()
                        .map(element -> ConceptDao.builder()
                                .labelFr(element.getLabels().get("fr"))
                                .labelAr(element.getLabels().get("ar"))
                                .definitionFr(element.getDescriptions().get("fr"))
                                .definitionAr(element.getDescriptions().get("ar"))
                                .varianteFr(CollectionUtils.isEmpty(element.getAliases().get("fr")) ? null :
                                        element.getAliases().get("fr").stream()
                                                .map(AliasDTO::getValue)
                                                .collect(Collectors.joining(", ")))
                                .varianteAr(CollectionUtils.isEmpty(element.getAliases().get("ar")) ? null :
                                        element.getAliases().get("ar").stream()
                                                .map(AliasDTO::getValue)
                                                .collect(Collectors.joining(", ")))
                                .url("www.wikidata.org/wiki/"+element.getId())
                                .build())
                        .toList();
            }
        }

        return List.of();
    }

    public List<ConceptDao> idRefSearch(String termValue, String languageToSearch) {
        var tmp = rdRefClient.searchInRdRefSource(termValue);
        if (CollectionUtils.isEmpty(tmp)) {
            return List.of();
        } else {
            return tmp.stream()
                    .map(element -> "fr".equalsIgnoreCase(languageToSearch)
                            ? ConceptDao.builder().labelAr(element).build()
                            : ConceptDao.builder().labelFr(element).build())
                    .toList();
        }
    }


    public List<ConceptDao> searchInConsultationThesaurus(Users userConnected, List<ConceptDao> conceptsReferenceFoundList,
                                                          String termValue, boolean toArabic) {

        log.info("Vérification s'il y a des thésaurus de consultation");
        var consultationProjects = userConnected.getGroup().getConsultationInstances();
        List<ConceptDao> conceptsConsultationFoundList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(consultationProjects)) {
            log.info("Il existe {} projet de consultation !", consultationProjects.size());
            for (ConsultationInstances project : consultationProjects) {
                project.getThesauruses().forEach(thesaurus -> {
                    var defaultIdGroup = ALL.equalsIgnoreCase(thesaurus.getIdCollection())
                            ? null
                            : userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection();
                    var tmp = searchInThesaurus(thesaurus, project.getUrl(), defaultIdGroup, termValue, toArabic);
                    if (!CollectionUtils.isEmpty(tmp)) {
                        conceptsConsultationFoundList.addAll(tmp.stream()
                                .filter(element -> isAlreadyFound(conceptsConsultationFoundList, conceptsReferenceFoundList,
                                        element.getThesaurusId(), element.getConceptId()))
                                .toList());
                    }
                });
            }
        }
        return conceptsConsultationFoundList;
    }

    public List<ConceptDao> searchInReferenceProject(Users userConnected, String termValue, boolean toArabic) {

        var referenceProject = userConnected.getGroup().getReferenceInstances();
        List<ConceptDao> conceptsReferenceFoundList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(referenceProject)) {
            try {
                var defaultIdGroup = ALL.equalsIgnoreCase(userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection())
                        ? null
                        : userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection();
                var tmp = searchInThesaurus(referenceProject.getThesaurus(), referenceProject.getUrl(), defaultIdGroup, termValue, toArabic);
                conceptsReferenceFoundList.addAll(tmp);
            } catch (Exception ex) {
                log.info("Aucune résultat trouvé pour le thésaurus de référence {}", referenceProject.getThesaurus().getName());
            }
        }
        return conceptsReferenceFoundList;
    }

    public List<CollectionElementDao> searchReferenceCollectionList(Users userConnected) {

        List<CollectionElementDao> referenceCollectionList = new ArrayList<>();
        referenceCollectionList.add(new CollectionElementDao(ALL, "--"));
        if (!ObjectUtils.isEmpty(userConnected.getGroup().getReferenceInstances())) {
            if (ALL.equals(userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection())) {
                referenceCollectionList.addAll(thesaurusService.searchCollections(
                        userConnected.getGroup().getReferenceInstances().getUrl(),
                        userConnected.getGroup().getReferenceInstances().getThesaurus().getIdThesaurus()));
            } else {
                referenceCollectionList.addAll(thesaurusService.searchSubCollections(
                        userConnected.getGroup().getReferenceInstances().getUrl(),
                        userConnected.getGroup().getReferenceInstances().getThesaurus().getIdThesaurus(),
                        userConnected.getGroup().getReferenceInstances().getThesaurus().getIdCollection()));
            }
        }
        return referenceCollectionList;
    }

    private List<ConceptDao> searchInThesaurus(Thesaurus thesaurus, String url, String defaultIdGroup, String termValue, boolean toArabic) {

        log.info("Thésaurus de référence : " + thesaurus.getName());
        var idCollection = ALL.equalsIgnoreCase(thesaurus.getIdCollection())
                ? defaultIdGroup
                : thesaurus.getIdCollection();

        var languageToSearch = toArabic ? "fr" : "ar";
        return thesaurusService.searchConcepts(thesaurus, url, termValue, languageToSearch, idCollection);
    }

    private boolean isAlreadyFound(List<ConceptDao> conceptsConsultationFoundList,
                                   List<ConceptDao> conceptsReferenceFoundList,
                                   String thesaurusId, String conceptId) {

        if (CollectionUtils.isEmpty(conceptsReferenceFoundList)) return true;

        var foundInReferenceThesaurus = conceptsReferenceFoundList.stream()
                .anyMatch(a -> a.getThesaurusId().equals(thesaurusId) && a.getConceptId().equals(conceptId));
        var foundInConsultationThesaurus = conceptsConsultationFoundList.stream()
                .anyMatch(a -> a.getThesaurusId().equals(thesaurusId) && a.getConceptId().equals(conceptId));
        return !foundInReferenceThesaurus && !foundInConsultationThesaurus;
    }

}
