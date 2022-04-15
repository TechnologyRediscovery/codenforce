/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.Managed;
import com.tcvcog.tcvce.entities.ManagedSchemaEnum;
import java.util.List;

/**
 *
 * @author Mike-Faux
 */
public class ManageBB {
    private List<Managed> mList;
    private ManagedSchemaEnum currentSchema;
    private Managed current;
    
    /**
     * @return the mList
     */
    public List<Managed> getmList() {
        return mList;
    }

    /**
     * @param mList the mList to set
     */
    public void setmList(List<Managed> mList) {
        this.mList = mList;
    }
    
    
    public void createNew(){
        
    }
}
