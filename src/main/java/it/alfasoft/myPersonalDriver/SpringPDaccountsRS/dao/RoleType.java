package it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao;

public enum RoleType {
    User(1),
    Driver(2),
    Admin(3);

    private final int id;
    RoleType(int id){ this.id = id; }
    public int getId() { return id;}

}
