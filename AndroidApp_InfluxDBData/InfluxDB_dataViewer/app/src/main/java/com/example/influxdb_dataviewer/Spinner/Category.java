package com.example.influxdb_dataviewer.Spinner;

public class Category {

    public String name;
    public String ID;

    public Category(String name,String id) {
        this.name = name;
        this.ID=id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
