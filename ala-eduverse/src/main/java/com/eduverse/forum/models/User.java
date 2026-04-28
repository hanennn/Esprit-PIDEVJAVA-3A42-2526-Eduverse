package com.eduverse.forum.models;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

public class User {
    private int id;
    private String username;
    private String roles;
    private String nom;
    private String prenom;
    private String email;
    private boolean active;

    public User() {}

    public User(int id, String username, String roles, String nom, String prenom, String email, boolean active) {
        this.id = id;
        this.username = username;
        this.roles = roles;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<String> getRoleList() {
        List<String> result = new ArrayList<>();
        if (roles == null || roles.isBlank()) {
            return result;
        }
        try {
            JSONArray array = new JSONArray(roles);
            for (int index = 0; index < array.length(); index++) {
                result.add(array.getString(index));
            }
        } catch (Exception ignored) {
            for (String role : roles.replace("[", "").replace("]", "").replace("\"", "").split(",")) {
                String trimmed = role.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        }
        return result;
    }

    public boolean hasRole(String role) {
        return getRoleList().contains(role);
    }

    public String getPrimaryRoleLabel() {
        if (hasRole("ROLE_ADMIN")) return "Admin";
        if (hasRole("ROLE_TEACHER")) return "Teacher";
        if (hasRole("ROLE_STUDENT")) return "Student";
        return "User";
    }

    @Override
    public String toString() {
        return username + " (" + getPrimaryRoleLabel() + ")";
    }
}