package ly.rqmana.huia.java.models;

import ly.rqmana.huia.java.security.Hasher;
import ly.rqmana.huia.java.util.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User extends Person {

    private String username;
    private String password;
    private String hashedPassword;
    private String email;
    private LocalDateTime dateJoined;
    private Boolean isSuperuser;
    private Boolean isStaff;
    private Boolean isActive;
    private LocalDateTime lastLogin;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(LocalDateTime dateJoined) {
        this.dateJoined = dateJoined;
    }

    public Boolean isSuperuser() {
        return isSuperuser;
    }

    public void setSuperuser(Boolean superuser) {
        isSuperuser = superuser;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Boolean isStaff() {
        return isStaff;
    }

    public void setStaff(Boolean staff) {
        isStaff = staff;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    /**
     * This method has been wrote to be used in TableView.
     * @return userType
     */
    public String getUserType() {
        String userType = "";
        if (isSuperuser())
            userType += "SuperUser";
        if (isStaff()) {
            if (!userType.isEmpty())
                userType += ", ";
            userType += "Staff";
        }

        return userType;
    }

    public String getHashedPassword() {
        String newHashPassword = Hasher.encode(getPassword(), Utils.getRandomString(10));
        if (hashedPassword == null || !hashedPassword.equals(newHashPassword))
            hashedPassword = newHashPassword;

        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
}
