package com.templar.springboot_testdemo3.repository;

import com.templar.springboot_testdemo3.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface ModelRepository extends JpaRepository<Model, Integer> , JpaSpecificationExecutor<Model> {

    List<Model> findByName(String name);

    List<Model> findByTags(String tags);

    List<Model> findByUploadTime(LocalDateTime uploadTime);
}
