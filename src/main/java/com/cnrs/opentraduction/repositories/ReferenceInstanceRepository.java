package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.ReferenceInstances;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface ReferenceInstanceRepository extends JpaRepository<ReferenceInstances, Integer> {

    Optional<ReferenceInstances> findByName(String name);

    List<ReferenceInstances> findAllByOrderByName();

}
