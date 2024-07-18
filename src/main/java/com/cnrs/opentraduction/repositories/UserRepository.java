package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<Users, Integer> {

    Optional<Users> findByLoginAndPassword(String login, String password);

    Optional<Users> findByMail(String mail);

    List<Users> findAllByGroupId(Integer groupId);

    Optional<Users> findByPassword(String password);

    @Modifying
    @Transactional
    @Query("DELETE users c WHERE c.id = ?1")
    void deleteUserById(Integer userId);
}
