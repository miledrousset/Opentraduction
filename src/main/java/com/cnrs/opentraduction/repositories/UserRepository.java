package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByLoginAndPassword(String login, String password);
}
