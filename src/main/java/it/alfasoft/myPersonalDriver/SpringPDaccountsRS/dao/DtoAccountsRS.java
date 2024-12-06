package it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao;

import it.alfasoft.myPersonalDriver.common.dao.dto.DtoAccounts;

public class DtoAccountsRS extends DtoAccounts {
    private String email;
    private String password;
    private String role;
    private String status;

    public DtoAccountsRS() {

    }

    public DtoAccountsRS(String email, String password, String role, String status) {

        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }
}
