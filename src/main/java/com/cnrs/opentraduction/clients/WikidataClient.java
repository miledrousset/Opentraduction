package com.cnrs.opentraduction.clients;

import com.cnrs.opentraduction.models.client.wikidata.EntityDTO;
import com.cnrs.opentraduction.models.client.wikidata.EntityDetailsDTO;
import com.cnrs.opentraduction.models.client.wikidata.WikidataModel;
import com.cnrs.opentraduction.models.client.wikidata.WikidataResponseDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class WikidataClient {

    private static final String API_URL = "https://www.wikidata.org/w/api.php?action=wbgetentities&ids={ids}&languages=ar|fr&format=json";
    private static final String WIKIDATA_API_URL = "https://www.wikidata.org/w/api.php";
    private static final int MAX_ROWS = 10;

    private final RestTemplate restTemplate;


    public WikidataModel searchEntities(String searchTerm, String language) {

        var url = UriComponentsBuilder.fromHttpUrl(WIKIDATA_API_URL)
                .queryParam("action", "wbsearchentities")
                .queryParam("search", searchTerm)
                .queryParam("language", language)
                .queryParam("format", "json")
                .queryParam("limit", MAX_ROWS)
                .toUriString();
        return restTemplate.getForObject(url, WikidataModel.class);
    }

    public List<EntityDetailsDTO> getEntitiesDetails(List<String> ids) {
        // Construire la chaîne d'IDs séparés par "|"
        String idsParam = String.join("|", ids);

        // Appeler l'API
        WikidataResponseDTO response = restTemplate.getForObject(API_URL, WikidataResponseDTO.class, idsParam);

        // Mapper les résultats dans une liste de DTOs
        List<EntityDetailsDTO> entityDetailsList = new ArrayList<>();
        if (response != null && response.getEntities() != null) {
            for (Map.Entry<String, EntityDTO> entry : response.getEntities().entrySet()) {
                EntityDTO entity = entry.getValue();

                EntityDetailsDTO details = new EntityDetailsDTO();

                // Mapper les labels
                Map<String, String> labelsMap = entity.getLabels().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get("value")));
                details.setLabels(labelsMap);

                // Mapper les descriptions
                Map<String, String> descriptionsMap = entity.getDescriptions().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get("value")));
                details.setDescriptions(descriptionsMap);

                // Mapper les aliases
                details.setAliases(entity.getAliases());

                entityDetailsList.add(details);
            }
        }
        return entityDetailsList;
    }
}
