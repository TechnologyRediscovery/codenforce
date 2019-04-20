/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.entities;

import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Dominic Pimpinella and Eric Darsow
 */
public class PersonsRequirement extends EntityUtils implements Serializable {
   
    private boolean requirementSatisfied;
    private ArrayList<PersonType> requiredPersons;
    private ArrayList<PersonType> optionalPersons;
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
     * @return the requiredPersons
     */
    public ArrayList<PersonType> getRequiredPersons() {
        return requiredPersons;
    }

    /**
     * @param requiredPersons the requiredPersons to set
     */
    public void setRequiredPersons(ArrayList<PersonType> requiredPersons) {
        this.requiredPersons = requiredPersons;
    }

    /**
     * @return the optionalPersons
     */
    public ArrayList<PersonType> getOptionalPersons() {
        return optionalPersons;
    }

    /**
     * @param optionalPersons the optionalPersons to set
     */
    public void setOptionalPersons(ArrayList<PersonType> optionalPersons) {
        this.optionalPersons = optionalPersons;
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
