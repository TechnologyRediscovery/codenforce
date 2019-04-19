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
   
    /**
     * This will contain either existing Person objects, new Person objects created by user, or clones of existing Person 
     * objects whose reference persons data was changed as part of the application. The occupancy coordinator will digest this
     * list to determine if the requirements have been satisfied.
     */
    private ArrayList<Person> attachedPersons;
    private String requirementExplanation;
    
}
