package com.templar.springboot_testdemo3.repository;

import com.templar.springboot_testdemo3.entity.Model;
import com.templar.springboot_testdemo3.entity.ModelPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModelPermissionRepository extends JpaRepository<ModelPermission, Integer> {
    List<ModelPermission> findByModel(Model model);
    Optional<ModelPermission> findByModelAndUser_Id(Model model, String userId);
}