package com.cnrs.opentraduction.clients;

import com.cnrs.opentraduction.models.client.GeoName.GeoNameModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GeoNamesClient {

    private static final String GEONAMES_API_URL = "http://api.geonames.org/search";
    private static final String USERNAME = "shs.traduction";
    private static final int MAX_ROWS = 10;

    public List<GeoNameModel> searchValue(String searchTerm, String lang)
            throws IOException, InterruptedException, ParserConfigurationException, SAXException {

        searchTerm = searchTerm.replaceAll(" ", "%20");
        var requestUrl = String.format("%s?q=%s&maxRows=%d&style=FULL&lang=%s&username=%s",
                GEONAMES_API_URL, searchTerm, MAX_ROWS, lang, USERNAME);

        var request = HttpRequest.newBuilder().uri(URI.create(requestUrl)).GET().build();
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        List<GeoNameModel> result = new ArrayList<>();
        if (response.statusCode() == 200) {
            var factory = DocumentBuilderFactory.newInstance();
            var document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(response.body().getBytes()));

            // Extraire tous les éléments <geoname>
            var geonames = document.getElementsByTagName("geoname");

            for (int i = 0; i < geonames.getLength(); i++) {
                var geoname = (Element) geonames.item(i);
                // Ajouter l'objet au résultat
                result.add(GeoNameModel.builder()
                        .geonameId(getTextContent(geoname, "geonameId"))
                        .toponymName(getTextContent(geoname, "toponymName"))
                        .alternateNameAr(getAlternateNameByLang(geoname, "ar"))
                        .alternateNameFr(getAlternateNameByLang(geoname, "fr"))
                        .build());
            }
        } else {
            log.error("Error: HTTP " + response.statusCode());
        }

        return result;
    }

    // Méthode pour obtenir le texte d'un élément XML
    private String getTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "N/A";
    }

    // Méthode pour obtenir un alternateName avec un lang spécifique
    private String getAlternateNameByLang(Element parent, String lang) {
        NodeList alternateNames = parent.getElementsByTagName("alternateName");
        for (int j = 0; j < alternateNames.getLength(); j++) {
            Element alternateName = (Element) alternateNames.item(j);
            if (alternateName.hasAttribute("lang") && alternateName.getAttribute("lang").equals(lang)) {
                return alternateName.getTextContent();
            }
        }
        return "N/A";
    }
}
