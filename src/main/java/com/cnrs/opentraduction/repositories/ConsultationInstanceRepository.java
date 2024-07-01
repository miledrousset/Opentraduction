package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.ConsultationInstances;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ConsultationInstanceRepository extends JpaRepository<ConsultationInstances, Integer> {

    List<ConsultationInstances> findAllByOrderByName();

    Optional<ConsultationInstances> findByName(String name);

}
