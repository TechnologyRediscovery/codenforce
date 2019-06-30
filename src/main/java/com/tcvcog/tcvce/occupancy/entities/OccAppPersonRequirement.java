package com.tcvcog.tcvce.occupancy.entities;

import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.PersonType;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Dominic Pimpinella and Eric Darsow
 */
public class OccAppPersonRequirement extends EntityUtils implements Serializable {
   
    private boolean requirementSatisfied;
    private ArrayList<PersonType> requiredPersonTypes;
    private ArrayList<PersonType> optionalPersonTypes;
    private String requirementExplanation;
    
    /**
     * @return the requirementSatisfied
     */
    public boolean isRequirementSatisfied() {
        return requirementSatisfied;
    }

    /**
     * @param requirementSatisfied the requirementSatisfied to set
     */
    public void setRequirementSatisfied(boolean requirementSatisfied) {
        this.requirementSatisfied = requirementSatisfied;
    }

    /**
     * @return the requiredPersonTypes
     */
    public ArrayList<PersonType> getRequiredPersonTypes() {
        return requiredPersonTypes;
    }

    /**
     * @param requiredPersonTypes the requiredPersonTypes to set
     */
    public void setRequiredPersonTypes(ArrayList<PersonType> requiredPersonTypes) {
        this.requiredPersonTypes = requiredPersonTypes;
    }

    /**
     * @return the optionalPersonTypes
     */
    public ArrayList<PersonType> getOptionalPersonTypes() {
        return optionalPersonTypes;
    }

    /**
     * @param optionalPersonTypes the optionalPersonTypes to set
     */
    public void setOptionalPersonTypes(ArrayList<PersonType> optionalPersonTypes) {
        this.optionalPersonTypes = optionalPersonTypes;
    }

    /**
     * @return the requirementExplanation
     */
    public String getRequirementExplanation() {
        return requirementExplanation;
    }

    /**
     * @param requirementExplanation the requirementExplanation to set
     */
    public void setRequirementExplanation(String requirementExplanation) {
        this.requirementExplanation = requirementExplanation;
    }
    
}
