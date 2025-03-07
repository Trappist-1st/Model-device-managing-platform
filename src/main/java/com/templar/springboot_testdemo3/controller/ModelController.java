package com.templar.springboot_testdemo3.controller;

import com.templar.springboot_testdemo3.entity.Model;
import com.templar.springboot_testdemo3.entity.ModelPermission;
import com.templar.springboot_testdemo3.entity.ModelVersion;
import com.templar.springboot_testdemo3.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    @Autowired
    private ModelService modelService;

    @PostMapping("/upload")
    public Model uploadModel(@RequestParam("file") MultipartFile file,
                             @RequestParam("name") String name,
                             @RequestParam("description") String description,
                             @RequestParam("tags") String tags,
                             @RequestParam("category") String category,
                             @RequestParam("version") String version) throws IOException {
        return modelService.uploadModel(file,name,description,tags,category,version);
    }

    @GetMapping
    public List<Model> getAllModels(){
        return modelService.findAll();
    }

    @GetMapping("/get/{id}")
    public Model getModelById(@PathVariable int id){
        return modelService.findById(id);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteModelById(@PathVariable int id){
        modelService.deleteById(id);
    }

    @GetMapping("/search")
    public List<Model> searchModels(@RequestParam("name") String name,
                                    @RequestParam("tags") String tags,
                                    @RequestParam("uploadTime")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime uploadTime) throws IOException{
        if(name!=null && !name.isEmpty()){
            return modelService.findByName(name);
        }else if(tags!=null && !tags.isEmpty()){
            return modelService.findByTags(tags);
        }else if(uploadTime!=null){
            return modelService.findByUploadTime(uploadTime);
        }
        return modelService.findAll();
    }

    @GetMapping("/statistics")
    public Map<String, Object> getModelStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalModels", modelService.getTotalModels());
        statistics.put("categoryDistribution", modelService.getCategoryDistribution());
        statistics.put("downloadStatistics", modelService.getDownloadStatistics());
        return statistics;
    }

    @PostMapping("/increment-downloads/{id}")
    public Model incrementDownloadCount(@PathVariable int id) {
        return modelService.incrementDownloadCount(id);
    }

    @PutMapping("/{id}")
    public Model updateModel(@PathVariable int id,
                           @RequestParam("name") String name,
                           @RequestParam("description") String description,
                           @RequestParam("tags") String tags,
                           @RequestParam("category") String category) {
        return modelService.updateModel(id, name, description, tags, category);
    }

    @PostMapping("/{id}/versions")
    public ModelVersion createVersion(@PathVariable int id,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam("version") String version,
                                      @RequestParam("description") String description) throws IOException {
        return modelService.createVersion(id, file, version, description);
    }

    @GetMapping("/{id}/versions")
    public List<ModelVersion> getModelVersions(@PathVariable int id) {
        return modelService.getModelVersions(id);
    }

    @PostMapping("/{id}/permissions")
    public ModelPermission setModelPermission(@PathVariable int id,
                                              @RequestParam("userId") String userId,
                                              @RequestParam("canView") boolean canView,
                                              @RequestParam("canEdit") boolean canEdit,
                                              @RequestParam("canDelete") boolean canDelete,
                                              @RequestParam("canManagePermissions") boolean canManagePermissions) {
        return modelService.setModelPermission(id, userId, canView, canEdit, canDelete, canManagePermissions);
    }

    @GetMapping("/{id}/permissions")
    public List<ModelPermission> getModelPermissions(@PathVariable int id) {
        return modelService.getModelPermissions(id);
    }

    @GetMapping("/hello")
    public String sayHello() {
        System.out.println("Hello, World!");
        return "Hello, World!";
    }
}
