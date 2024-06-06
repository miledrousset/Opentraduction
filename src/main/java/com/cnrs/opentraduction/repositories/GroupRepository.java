package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.Groups;
import org.springframework.data.jpa.repository.JpaRepository;


public interface GroupRepository extends JpaRepository<Groups, Integer> {

}
