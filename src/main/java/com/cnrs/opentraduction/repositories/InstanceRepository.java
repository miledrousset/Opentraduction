package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.Instances;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InstanceRepository extends JpaRepository<Instances, Integer> {}
