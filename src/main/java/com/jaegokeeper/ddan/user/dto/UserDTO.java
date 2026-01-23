package com.jaegokeeper.ddan.user.dto;

public class UserDTO {

    private String userName;
    private String userPass;
    private String userMail;
    private String userPhone;
    private String login_type;

    private String userRole;
    private String userPassChk;


    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPass() { return userPass; }
    public void setUserPass(String userPass) { this.userPass = userPass; }

    public String getUserMail() { return userMail; }
    public void setUserMail(String userMail) { this.userMail = userMail; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getLogin_type() { return login_type; }
    public void setLogin_type(String login_type) { this.login_type = login_type; }


    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getUserPassChk() { return userPassChk; }
    public void setUserPassChk(String userPassChk) { this.userPassChk = userPassChk; }

}
