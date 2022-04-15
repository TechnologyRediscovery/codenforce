/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.entities.ManagedSchemaEnum;
import java.time.LocalDateTime;

/**
 *
 * @author Mike-Faux
 */
public class Managed {
    private int ID;
    private int iconID;
    private String title;
    private LocalDateTime deactivatedts;
    private final ManagedSchemaEnum MANAGABLE_SCHEMA;

    /**
     * 
     * @param mse 
     */
    public Managed(ManagedSchemaEnum mse){
        MANAGABLE_SCHEMA = mse;
    }
    
    /**
     * @return the ID
     */
    public int getID() {
        return ID;
    }

    /**
     * @param ID the ID to set
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * @return the iconID
     */
    public int getIconID() {
        return iconID;
    }

    /**
     * @param iconID the iconID to set
     */
    public void setIconID(int iconID) {
        this.iconID = iconID;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the deactivatedts
     */
    public LocalDateTime getDeactivatedts() {
        return deactivatedts;
    }

    /**
     * @param deactivatedts the deactivatedts to set
     */
    public void setDeactivatedts(LocalDateTime deactivatedts) {
        this.deactivatedts = deactivatedts;
    }
}
