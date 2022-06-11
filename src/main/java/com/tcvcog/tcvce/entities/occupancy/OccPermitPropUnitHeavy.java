/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.PropertyUnitWithProp;

/**
 * An inverted data heavy subclass that contains the property and property unit 
 * objects.
 * @author sylvia
 */
public class OccPermitPropUnitHeavy extends OccPermit {
    
    private PropertyUnitWithProp propUnitWithProp;
    
    public OccPermitPropUnitHeavy(OccPermit op){
        super(op);
        
    }

    /**
     * @return the propUnitWithProp
     */
    public PropertyUnitWithProp getPropUnitWithProp() {
        return propUnitWithProp;
    }

    /**
     * @param propUnitWithProp the propUnitWithProp to set
     */
    public void setPropUnitWithProp(PropertyUnitWithProp propUnitWithProp) {
        this.propUnitWithProp = propUnitWithProp;
    }
}
