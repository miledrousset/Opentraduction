package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.UsersThesaurus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface UserThesaurusRepository extends JpaRepository<UsersThesaurus, Integer> {

    Optional<UsersThesaurus> findByThesaurusIdAndUserId(Integer thesaurusId, Integer userId);


    @Modifying
    @Transactional
    @Query("DELETE FROM user_thesaurus WHERE thesaurusId = ?1 AND userId = ?2")
    void deleteByThesaurusIdAndUserId(Integer thesaurusId, Integer userId);

}
