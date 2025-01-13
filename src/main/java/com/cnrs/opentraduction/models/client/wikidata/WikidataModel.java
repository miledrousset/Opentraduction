package com.cnrs.opentraduction.models.client.wikidata;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WikidataModel {

    @JsonProperty("searchinfo")
    private SearchInfo searchInfo;

    @JsonProperty("search")
    private List<SearchResult> searchResults;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchInfo {
        private String search;

        public String getSearch() {
            return search;
        }

        public void setSearch(String search) {
            this.search = search;
        }
    }

    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResult {
        private String id;
        private String title;
        private String concepturi;
        private String label;
        private String description;
    }
}

