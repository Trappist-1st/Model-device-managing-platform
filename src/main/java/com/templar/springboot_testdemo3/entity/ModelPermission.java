package com.templar.springboot_testdemo3.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ModelPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private boolean canView;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canManagePermissions;

    @PrePersist
    protected void onCreate() {
        // Set default permissions
        if (user.getRole().equals("ADMIN")) {
            canView = true;
            canEdit = true;
            canDelete = true;
            canManagePermissions = true;
        } else {
            canView = true;
            canEdit = false;
            canDelete = false;
            canManagePermissions = false;
        }
    }

    public void setUserId(String userId) {
        User user = new User();
        user.setId(userId);
        this.user = user;
    }
}