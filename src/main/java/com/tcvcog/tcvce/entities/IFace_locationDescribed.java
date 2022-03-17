/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;

/**
 * Represents an object that has a location descriptor in its belly,
 * such as a property unit, a property, an occinspectedspace
 * @author sylvia
 */
public interface IFace_locationDescribed {
    public OccLocationDescriptor getOccLocationDescriptor();
    public void setOccLocationDescriptor();
    
}
