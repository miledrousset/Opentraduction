package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.Groups;
import com.cnrs.opentraduction.entities.ReferenceInstances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface GroupRepository extends JpaRepository<Groups, Integer> {


    @Modifying
    @Transactional
    @Query("UPDATE groups c SET c.referenceInstances = ?2 WHERE c.id = ?1")
    void updateReferenceProject(Integer id, ReferenceInstances referenceInstances);

    List<Groups> findAllByOrderByName();

    Optional<Groups> findGroupsByName(String name);

}
