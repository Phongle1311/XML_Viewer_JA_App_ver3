package com.example.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "xmlFile")
public class XmlFile {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String instanceId;

    public XmlFile(String name, String instanceId) {
        this.name = name;
        this.instanceId = instanceId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
