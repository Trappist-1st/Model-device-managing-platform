package com.templar.springboot_testdemo3.service;

import com.templar.springboot_testdemo3.entity.Model;
import com.templar.springboot_testdemo3.entity.ModelPermission;
import com.templar.springboot_testdemo3.entity.ModelVersion;
import com.templar.springboot_testdemo3.repository.ModelPermissionRepository;
import com.templar.springboot_testdemo3.repository.ModelRepository;
import com.templar.springboot_testdemo3.repository.ModelVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.*;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class ModelService {
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private ModelPermissionRepository modelPermissionRepository;
    @Autowired
    private ModelVersionRepository modelVersionRepository;

    public Model uploadModel(MultipartFile file, String name,String description,String tags,String category,String version) throws IOException {
        Model model=new Model();
        model.setName(name);
        model.setDescription(description);
        model.setTags(tags);
        model.setCategory(category);
        model.setVersion(version);
        model.setFilePath(saveFile(file));
        //model.setUploadTime(System.currentTimeMillis());
        long currentTimeMillis = System.currentTimeMillis();
        LocalDateTime uploadTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis), ZoneId.systemDefault());
        model.setUploadTime(uploadTime);
        return modelRepository.save(model);
    }

    private String saveFile(MultipartFile file) throws IOException {
        //String filePath="models/"+file.getOriginalFilename();
        String filePath=file.getOriginalFilename();
        File destFile=new File(filePath);
        file.transferTo(destFile);
        return filePath;
    }

    public List<Model> findAll(){
        return modelRepository.findAll();
    }

    public Model findById(int id){
        return modelRepository.findById(id).orElse(null);
    }

    public void deleteById(int id){
        modelRepository.deleteById(id);
    }

    public List<Model> findByName(String name){
        return modelRepository.findByName(name);
    }

    public List<Model> findByTags(String tags) {
        return modelRepository.findByTags(tags);
    }

    public List<Model> findByUploadTime(LocalDateTime uploadTime) {
        return modelRepository.findByUploadTime(uploadTime);
    }

    public long getTotalModels() {
        return modelRepository.count();
    }

    public Map<String, Long> getCategoryDistribution() {
        List<Model> models = modelRepository.findAll();
        return models.stream()
            .collect(Collectors.groupingBy(
                Model::getCategory,
                Collectors.counting()
            ));
    }

    public Map<String, Object> getDownloadStatistics() {
        List<Model> models = modelRepository.findAll();
        Map<String, Object> statistics = new HashMap<>();
        
        long totalDownloads = models.stream()
            .mapToInt(Model::getDownloadCount)
            .sum();
            
        OptionalDouble avgDownloads = models.stream()
            .mapToInt(Model::getDownloadCount)
            .average();
            
        Model mostDownloaded = models.stream()
            .max(Comparator.comparingInt(Model::getDownloadCount))
            .orElse(null);

        statistics.put("totalDownloads", totalDownloads);
        statistics.put("averageDownloads", avgDownloads.orElse(0.0));
        statistics.put("mostDownloadedModel", mostDownloaded != null ? mostDownloaded.getName() : null);
        
        return statistics;
    }

    public Model incrementDownloadCount(int id) {
        Model model = findById(id);
        if (model != null) {
            model.setDownloadCount(model.getDownloadCount() + 1);
            return modelRepository.save(model);
        }
        return null;
    }
    public Model updateModel(int id, String name, String description, String tags, String category) {
        Model model = findById(id);
        if (model != null) {
            model.setName(name);
            model.setDescription(description);
            model.setTags(tags);
            model.setCategory(category);
            return modelRepository.save(model);
        }
        return null;
    }

    public ModelVersion createVersion(int id, MultipartFile file, String version, String description) throws IOException {
        Model model = findById(id);
        if (model != null) {
            ModelVersion modelVersion = new ModelVersion();
            modelVersion.setModel(model);
            modelVersion.setVersion(version);
            modelVersion.setDescription(description);
            modelVersion.setFilePath(saveFile(file));
            modelVersion.setCreatedAt(LocalDateTime.now());
            return modelVersionRepository.save(modelVersion);
        }
        return null;
    }

    public List<ModelVersion> getModelVersions(int id) {
        Model model = findById(id);
        if (model != null) {
            return modelVersionRepository.findByModel(model);
        }
        return new ArrayList<>();
    }

    public ModelPermission setModelPermission(int id, String userId, boolean canView, boolean canEdit,
                                              boolean canDelete, boolean canManagePermissions) {
        Model model = findById(id);
        if (model != null) {
            ModelPermission permission = modelPermissionRepository.findByModelAndUser_Id(model, userId)
                .orElse(new ModelPermission());
            permission.setModel(model);
            permission.setUserId(userId);
            permission.setCanView(canView);
            permission.setCanEdit(canEdit);
            permission.setCanDelete(canDelete);
            permission.setCanManagePermissions(canManagePermissions);
            return modelPermissionRepository.save(permission);
        }
        return null;
    }

    public List<ModelPermission> getModelPermissions(int id) {
        Model model = findById(id);
        if (model != null) {
            return modelPermissionRepository.findByModel(model);
        }
        return new ArrayList<>();
    }
}
