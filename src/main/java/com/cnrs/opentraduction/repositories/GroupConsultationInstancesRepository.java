package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.GroupConsultationInstances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


public interface GroupConsultationInstancesRepository extends JpaRepository<GroupConsultationInstances, Integer> {


    @Modifying
    @Transactional
    @Query("DELETE FROM group_consultation_instances WHERE groupId = ?1")
    void deleteAllByGroupId(Integer groupId);

}
