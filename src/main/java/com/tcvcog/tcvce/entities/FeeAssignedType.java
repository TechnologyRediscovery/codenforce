package com.tcvcog.tcvce.entities;

public enum FeeAssignedType {
    OccPeriod("Occupancy Period"),
    CECase("CE Case");
    
    private final String label;

    private FeeAssignedType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
    
}