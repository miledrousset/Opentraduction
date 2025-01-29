package com.cnrs.opentraduction.clients;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.primefaces.shaded.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


@Service
@AllArgsConstructor
public class IdRefClient {

    private static final String BASE_URL = "https://www.idref.fr/Sru/Solr";

    private final RestTemplate restTemplate;


    @SneakyThrows
    public List<String> searchInRdRefSource(String searchTerm) {

        List<String> result = new ArrayList<>();
        result.addAll(searchInIdRefPersonnes(searchTerm));
        return result;
    }

    public List<String> searchInIdRefPersonnes(String searchTerm) throws Exception {

        var url = BASE_URL + String.format("?wt=json&q=persname_t:(%s)&fl=ppn_z,affcourt_z,prenom_s,nom_s&start=0&rows=10&version=2.2", searchTerm);
        return search(url);
    }

    public List<String> search(String url) throws Exception {
        var response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCodeValue() == 200) {
            return extractAffcourtZValues(response.getBody());
        } else {
            throw new Exception("Erreur : code de réponse HTTP " + response.getStatusCodeValue());
        }
    }

    private List<String> extractAffcourtZValues(String jsonResponse) {
        List<String> affcourtValues = new ArrayList<>();
        if (!StringUtils.isEmpty(jsonResponse)) {
            var docs = new JSONObject(jsonResponse).getJSONObject("response").getJSONArray("docs");
            for (int i = 0; i < docs.length(); i++) {
                var doc = docs.getJSONObject(i);
                if (doc.has("affcourt_z")) {
                    affcourtValues.add(doc.getString("affcourt_z"));
                }
            }
        }
        return affcourtValues;
    }
}
