package it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao;

public enum StatusType {
    Active(1),
    Suspended(2),
    Banned(3);

    private final int id;
    StatusType(int id){this.id = id;}
    public int getId(){return id;}
}
