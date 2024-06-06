package com.cnrs.opentraduction.repositories;

import com.cnrs.opentraduction.entities.Thesaurus;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ThesaurusRepository extends JpaRepository<Thesaurus, Integer> {}
