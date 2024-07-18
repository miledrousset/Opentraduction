package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<Users, Integer> {

    Optional<Users> findByLoginAndPassword(String login, String password);

    Optional<Users> findByMail(String mail);

    List<Users> findAllByGroupId(Integer groupId);

    Optional<Users> findByPassword(String password);
}
