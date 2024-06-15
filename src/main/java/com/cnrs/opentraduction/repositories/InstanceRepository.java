package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.ConsultationInstances;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InstanceRepository extends JpaRepository<ConsultationInstances, Integer> {}
