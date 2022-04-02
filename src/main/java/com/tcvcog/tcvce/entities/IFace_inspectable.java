/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.entities.occupancy.FieldInspection;
import java.util.List;

/**
 * Declares an object as a holder of occupancy inspections
 * as of March 2022, this includes Occupancy Periods and CE Cases
 * @author sylvia
 */
public interface IFace_inspectable {
    public List<FieldInspection> getInspectionList();
    public void setInspectionList(List<FieldInspection> inspectionList);
    public int getHostPK();
    public DomainEnum getDomainEnum();
    public User getManager();
    public boolean isNewInspectionsAllowed();
    
    
}
