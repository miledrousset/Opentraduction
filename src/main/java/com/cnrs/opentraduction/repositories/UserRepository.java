package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.User;
import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User, Integer> {}
