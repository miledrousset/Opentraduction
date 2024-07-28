package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.Thesaurus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


public interface ThesaurusRepository extends JpaRepository<Thesaurus, Integer> {


    @Modifying
    @Transactional
    @Query("delete from thesaurus t where t.consultationInstances.id = ?1")
    void deleteByConsultationInstanceId(Integer consultationProjectId);


    @Modifying
    @Transactional
    @Query("delete from thesaurus t where t.referenceInstances.id = ?1")
    void deleteByReferenceInstanceId(Integer referenceProjectId);

}
