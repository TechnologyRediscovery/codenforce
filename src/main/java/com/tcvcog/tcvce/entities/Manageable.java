/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tcvcog.tcvce.entities;
import java.time.LocalDateTime;

/**
 *
 * @author Mike-Faux
 */
public interface Manageable {
    public LocalDateTime getDeactivatedts();
    public void setDeactivatedts(LocalDateTime deactivatedts);
    public ManagedSchemaEnum getMANAGEABLE_SCHEMA();
    public String getName();
    public void setName(String name);
    public int getID();
    public void setID(int ID);
    public Icon getIcon();
}
