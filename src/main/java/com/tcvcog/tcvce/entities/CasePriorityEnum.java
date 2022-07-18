/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 * Represents the second iteration of case phasing
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public enum CasePriorityEnum {
    
    OPENING             ("Opening","Blue", 1, true, "cse-priority-openblue", "caseStageInvestigationIconID"),
    MONITORING          ("Monitoring","Green", 2, true, "cse-priority-monitoringgreen", "caseStageEnforcementIconID"),
    ACTION_REQUIRED     ("Action Required","Yellow", 3, true,  "cse-priority-actionrequiredyellow","caseStageEnforcementIconID"),
    ACTION_PASTDUE      ("Action Past Due","Red", 4, true,  "cse-priority-actionpastduered","caseStagePastdueIconID"),
    CITATION            ("Citation","Purple", 5, true,  "cse-priority-citation","caseStageCitationIconID"),
    REVIEW              ("Review","Dark Green", 6, true,  "cse-priority-review","caseStageReviewIconID"),
    ABANDONMENT_STALL   ("Abandonement Stall","Dark Yellow", 7, true,  "cse-priority-abandoneddarkyellow","caseStageEnforcementIconID"),
    CLOSED              ("Closed","Gray", 8, true,  "cse-priority-closedgray","caseStageClosedIconID"),
    UNKNOWN             ("Unknown","Gray", 9, true,  "cse-priority-unknowngray","caseStageReviewIconID"),
    CONTAINER           ("Container","Gray", -1, true,  "cse-priority-containergray","caseStageUnknownIconID");
    
    private final String label;
    private final String color;
    private final int priorityOrder;
    private final boolean qualifiesAsOpen;
    protected final String rowStyleClass;
    private final String iconPropertyLookup;
     
    private CasePriorityEnum(String label, String color, int ord, boolean os, String rsc, String icon){
        this.label = label;
        this.color = color;
        this.priorityOrder = ord;
        this.qualifiesAsOpen = os;
        this.rowStyleClass = rsc;
        this.iconPropertyLookup = icon;
    }
    
    public String getLabel(){
        return label;
    }
    
    public String getIconPropertyLookup(){
        return iconPropertyLookup;
    }

    /**
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * @return the priorityOrder
     */
    public int getPriorityOrder() {
        return priorityOrder;
    }

    /**
     * @return the qualifiesAsOpen
     */
    public boolean isQualifiesAsOpen() {
        return qualifiesAsOpen;
    }

    /**
     * @return the rowStyleClass
     */
    public String getRowStyleClass() {
        return rowStyleClass;
    }
    
    
    
    
    
    
}
