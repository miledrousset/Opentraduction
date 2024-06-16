package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.ReferenceInstances;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReferenceInstanceRepository extends JpaRepository<ReferenceInstances, Integer> {}
