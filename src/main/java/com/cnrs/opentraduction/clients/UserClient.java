package com.cnrs.opentraduction.clients;

import com.cnrs.opentraduction.models.client.opentheso.users.UserModel;
import com.cnrs.opentraduction.models.dao.ConnexionDao;
import com.cnrs.opentraduction.models.dao.UserDao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
@Data
@Service
@AllArgsConstructor
public class UserClient {

    private final static String API_KEY = "API-KEY";
    private final static String USERS_API = "/openapi/v1/users";

    private final WebClient webClient;


    public Mono<UserModel> authentification(String baseUrl, ConnexionDao connexionDto) {

        return webClient.post()
                .uri(baseUrl + USERS_API + "/authentification")
                .header("Content-Type", "application/json")
                .bodyValue(connexionDto)
                .retrieve()
                .bodyToMono(UserModel.class)
                .onErrorReturn(new UserModel());
    }

    public Mono<Boolean> deleteUser(String baseUrl, Integer userId, String userApiKey) {

        return webClient.delete()
                .uri(baseUrl + USERS_API + "/" + userId)
                .header(API_KEY, userApiKey)
                .retrieve()
                .toBodilessEntity()
                .map(response -> true)
                .onErrorReturn(false);
    }

    public Mono<UserModel> createUser(String baseUrl, UserDao userDao, String userApiKey) {

        return webClient.post()
                .uri(baseUrl + USERS_API)
                .header(API_KEY, userApiKey)
                .bodyValue(userDao)
                .retrieve()
                .bodyToMono(UserModel.class);
    }

    public Mono<UserModel> generateApiKey(String baseUrl, Integer userId, String userApiKey) {

        return webClient.put()
                .uri(baseUrl + USERS_API + "/api-key/" + userId)
                .header(API_KEY, userApiKey)
                .retrieve()
                .bodyToMono(UserModel.class);
    }

    public Boolean updateUser(String baseUrl, Integer userId, UserDao userDao, String userApiKey) {

        return webClient.put()
                .uri(baseUrl + USERS_API + "/" + userId)
                .header(API_KEY, userApiKey)
                .bodyValue(userDao)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode() == HttpStatus.OK)
                .onErrorReturn(false)
                .block();
    }

    public Mono<List<UserModel>> searchUsers(String baseUrl, String email, String username, String userApiKey) {
        var request = "";
        if (!StringUtils.isEmpty(email)) {
            request += "?mail=" + email;
        }
        if (!StringUtils.isEmpty(username)) {
            request =  StringUtils.isEmpty(request) ? "?" : "&";
            request += "username=" + username;
        }
        baseUrl = baseUrl.replace("https://opentheso2.mom.fr/opentheso2", "https://opentheso2.mom.fr");
        return webClient.get()
                .uri(baseUrl + USERS_API + request)
                .header(API_KEY, userApiKey)
                .retrieve()
                .bodyToFlux(UserModel.class)
                .collectList();
    }
}
