package com.cnrs.opentraduction.clients;

import com.cnrs.opentraduction.models.client.opentheso.users.UserModel;
import com.cnrs.opentraduction.models.dao.ConnexionDao;
import com.cnrs.opentraduction.models.dao.UserDao;

import com.cnrs.opentraduction.utils.MD5Password;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


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

    public Mono<Boolean> deleteUser(String baseUrl, String login, String password, String userApiKey) {

        return webClient.delete()
                .uri(baseUrl + USERS_API + "/" + login + "/" + MD5Password.encodedPassword(password))
                .header(API_KEY, userApiKey)
                .retrieve()
                .toBodilessEntity()
                .map(response -> true) // Retourne `true` si la suppression est réussie
                .onErrorReturn(false); // Retourne `false` en cas d'erreur
    }

    public Mono<UserModel> createUser(String baseUrl, UserDao userDao, String userApiKey) {

        return webClient.post()
                .uri(baseUrl + USERS_API)
                .header(API_KEY, userApiKey)
                .bodyValue(userDao)
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
}
