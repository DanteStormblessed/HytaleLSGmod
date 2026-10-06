package dev.hytalemodding.models;

import java.util.List;

public class Player {
    private int id_players;
    private String name;
    private String email;
    private int age;
    private List<String> roles;

    public int getId() { return id_players; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public List<String> getRoles() { return roles; }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}