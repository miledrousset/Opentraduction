package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.UsersThesaurus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface UserThesaurusRepository extends JpaRepository<UsersThesaurus, Integer> {

    Optional<UsersThesaurus> findByThesaurusIdAndUserId(Integer thesaurusId, Integer userId);

}
