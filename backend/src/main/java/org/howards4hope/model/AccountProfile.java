package org.howards4hope.model;

import java.io.Serializable;
import java.util.List;

/**
 * Cached account profile representing user identity, roles, and metadata.
 * Designed for sub-millisecond retrieval from L1 (Caffeine) and L2 (Redis).
 */
public class AccountProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uid;
    private String email;
    private String name;
    private boolean admin;
    private List<String> roles;
    private long cachedAt;

    public AccountProfile() {
        this.cachedAt = System.currentTimeMillis();
    }

    public AccountProfile(String uid, String email, String name, boolean admin, List<String> roles) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.admin = admin;
        this.roles = roles;
        this.cachedAt = System.currentTimeMillis();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public long getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(long cachedAt) {
        this.cachedAt = cachedAt;
    }
}
