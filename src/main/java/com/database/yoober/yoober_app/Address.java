package com.database.yoober.yoober_app;

public class Address {
    private int address_Id;
    private String street;
    private String city;
    private String province;
    private String postal_code;
    //private String customName;

    public Address(String street, String city, String province, String postal_code) {
        this.street = street;
        this.city = city;
        this.province = province;
        this.postal_code = postal_code;
    }

    
    public Address() {
    }


    /*public String getCustomName() {
        return customName;
    }
    public void setCustomName(String customName) {
        this.customName = customName;
    }*/
    public int getAddress_Id() {
        return address_Id;
    }
    public void setAddress_Id(int address_Id) {
        this.address_Id = address_Id;
    }
    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getProvince() {
        return province;
    }
    public void setProvince(String province) {
        this.province = province;
    }
    public String getPostal_code() {
        return postal_code;
    }
    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }



}
