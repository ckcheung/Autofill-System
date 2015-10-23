package Autofill;

public class User {
    private String username;
    private String role;
    private String data;
    
    public User() {
        username = null;
        role = null;
        data = null;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getRole() {
        return this.role;
    }
    
    public void setData(String data) {
        this.data = data;
    } 
    
    public String getData() {
        return this.data;
    }
}
