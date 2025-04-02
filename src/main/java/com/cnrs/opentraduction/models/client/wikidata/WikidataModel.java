package com.cnrs.opentraduction.models.client.wikidata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WikidataModel {

    @JsonProperty("searchinfo")
    private SearchInfo searchInfo;

    @JsonProperty("search")
    private List<SearchResult> searchResults;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchInfo {
        private String search;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResult {
        private String id;
        private String title;
        private String concepturi;
        private String repository;
        private String url;
        private String label;
        private String description;
        private List<String> aliases;
        private Match match;
        private Display display;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Match {
        private String type;
        private String language;
        private String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Display {
        private LangValue label;
        private LangValue description;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LangValue {
        private String value;
        private String language;
    }
}

