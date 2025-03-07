package com.templar.springboot_testdemo3.repository;

import com.templar.springboot_testdemo3.entity.Model;
import com.templar.springboot_testdemo3.entity.ModelVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelVersionRepository extends JpaRepository<ModelVersion, Integer> {
    List<ModelVersion> findByModel(Model model);
}