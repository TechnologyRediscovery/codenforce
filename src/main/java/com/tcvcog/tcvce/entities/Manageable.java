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
public interface Managable {
//NOT IN USE
    
    
    public LocalDateTime getDeactivatedts();
    public void setDeactivatedts(LocalDateTime deactivatedts);
    public ManagedSchemaEnum getMANAGABLE_SCHEMA();
    public String getTitle();
    public void setTitle(String title);
}
