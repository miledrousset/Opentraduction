package com.cnrs.opentraduction.clients;

import com.cnrs.opentraduction.models.client.wikidata.EntityDTO;
import com.cnrs.opentraduction.models.client.wikidata.WikidataModel;
import com.cnrs.opentraduction.models.client.wikidata.EntityDetailsDTO;
import com.cnrs.opentraduction.models.client.wikidata.WikidataResponseDTO;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import reactor.netty.http.client.HttpClient;


@Slf4j
@Service
@AllArgsConstructor
public class WikidataClient {

    private static final String API_URL = "https://www.wikidata.org/w/api.php";
    private static final int MAX_ROWS = 10;

    public WikidataModel searchEntities(String searchTerm, String language) {
        String uri = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("action", "wbsearchentities")
                .queryParam("search", searchTerm)
                .queryParam("language", language)
                .queryParam("format", "json")
                .queryParam("limit", MAX_ROWS)
                .build()
                .toUriString();

        return webClient().get()
                .uri(uri)
                .retrieve()
                .bodyToMono(WikidataModel.class)
                .block(); // blocage pour rester dans du code impératif
    }

    public List<EntityDetailsDTO> getEntitiesDetails(List<String> ids) {
        String idsParam = String.join("|", ids);

        String uri = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("action", "wbgetentities")
                .queryParam("ids", idsParam)
                .queryParam("languages", "ar|fr")
                .queryParam("format", "json")
                .build()
                .toUriString();

        WikidataResponseDTO response = webClient().get()
                .uri(uri)
                .retrieve()
                .bodyToMono(WikidataResponseDTO.class)
                .block();

        List<EntityDetailsDTO> entityDetailsList = new ArrayList<>();
        if (response != null && response.getEntities() != null) {
            for (Map.Entry<String, EntityDTO> entry : response.getEntities().entrySet()) {

                var value = entry.getValue();
                var details = new EntityDetailsDTO();
                details.setId(entry.getKey());
                details.setLabels(value.getLabels().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get("value"))));
                details.setDescriptions(value.getDescriptions().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get("value"))));
                details.setAliases(value.getAliases());
                entityDetailsList.add(details);
            }
        }
        return entityDetailsList;
    }

    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(java.time.Duration.ofSeconds(10)); // optionnel

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(5 * 1024 * 1024)) // ← 5 Mo (par exemple)
                .baseUrl("https://www.wikidata.org/w/api.php")
                .build();
    }
}
