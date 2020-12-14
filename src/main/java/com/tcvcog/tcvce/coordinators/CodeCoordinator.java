/*
 * Copyright (C) 2017 Turtle Creek Valley
Council of Governments, PA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CodeCoordinator extends BackingBeanUtils implements Serializable {

    
    final int ECE_DEFAULT_PENALTY_MAX = 1000;
    final int ECE_DEFAULT_PENALTY_NORM = 50;
    final int ECE_DEFAULT_PENALTY_MIN = 10;
    final String ECE_DEFAULT_PENALTY_NOTES = "Default values";
    final int ECE_DEFAULT_DAYS_TO_COMPLY = 30;
    final String ECE_DEFAULT_DAYSTOCOMPLY_NOTES = "Default values";
    /**
     * Creates a new instance of CodeCoordinator
     */
    public CodeCoordinator() {
    }
    
    // *************************************************************
    // *********************CODE SOURCES****************************
    // *************************************************************
    
        
    /**
     * Factory for CodeSource objects
     * @return 
     */
    public CodeSource getCodeSourceSkeleton(){
        
        return new CodeSource();
        
    }
    
    /**
     * Retrieval method for code sources
     * @param sourceID
     * @return
     * @throws IntegrationException 
     */
    public CodeSource getCodeSource(int sourceID) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        return ci.getCodeSource(sourceID);
        
    }
    /**
     * Main generator for a fully baked code source and all its elements
     * @param sourceID
     * @return
     * @throws IntegrationException 
     */
    public CodeSource retrieveCodeSourceByID(int sourceID) throws IntegrationException{
        if(sourceID == 0) return null;
        CodeIntegrator integrator = getCodeIntegrator();
        CodeSource s = integrator.getCodeSource(sourceID);
       
        return s;
    }
    
     /**
     * Primary getter for lists of CodeSource objects
     * @return
     * @throws IntegrationException 
     */
    public List<CodeSource> getCodeSourceList() throws IntegrationException{
        CodeIntegrator integrator = getCodeIntegrator();
        List<CodeSource> sources = integrator.getCompleteCodeSourceList();
        return sources;
    }
    
    /**
     * Logic pass through for insertion of CodeSource objects
     * @param source
     * @throws IntegrationException 
     */
    public void addNewCodeSource(CodeSource source) throws IntegrationException{
        getCodeIntegrator().insertCodeSource(source);
    }
    
    /**
     * Logic pass through for updates to code sources
     * @param source
     * @throws IntegrationException 
     */
    public void updateCodeSource(CodeSource source) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        
        
        ci.updateCodeSource(source);
        
    }
    
    /**
     * Logic pass through for deactivation of a code source
     * @param source
     * @throws IntegrationException 
     */
    public void deactivateCodeSource(CodeSource source) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        
        ci.deactivateCodeSource(source);
        
        
        
    }

    
    
    
    // *************************************************************
    // **************CODE ELEMENTS (ORDINANCES)*********************
    // *************************************************************
   
    
    /**
     * Primary factory method for CodeElement objects
     * @return 
     */
    public CodeElement getCodeElementSkeleton(){
        return new CodeElement();
    }
    
      
    /**
     * Primary getter for all code elements system-wide
     * @param eleid
     * @return
     * @throws IntegrationException 
     */
    public CodeElement getCodeElement(int eleid) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        return ci.getCodeElement(eleid);
    }
    
    
    /**
     * Extracts all CodeElements in a given Source
     * @param src
     * @return of CodeElements in that source
     * @throws IntegrationException 
     */
    public List<CodeElement> getCodeElemements(CodeSource src) throws IntegrationException{
        if(src ==  null){
            return new ArrayList<>();
        }
        CodeIntegrator ci = getCodeIntegrator();
        return ci.getCodeElements(src.getSourceID());
    }
    
    
    
    
   /**
    * Logic intermediary for insertion of new CodeElements
    * 
    * @param ele to be inserted
    * @param ua doing the insertion
    * @return the ID of the freshly inserted record
    * @throws IntegrationException 
    */
    public int addCodeElement(CodeElement ele, UserAuthorized ua) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        int freshId = 0;
        if(ua == null || ele == null) return 0;
        ele.setCreatedBy(ua);
        ele.setLastupdatedBy(ua);
        freshId = ci.insertCodeElement(ele);
        return freshId;
        
    }
    
    
    /**
     * Logic intermediary for updates to a code element
     * 
     * @param ele with fields updated
     * @param ua doing the updating
     * @throws IntegrationException 
     */
    public void updateCodeElement(CodeElement ele, UserAuthorized ua) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(ua == null || ele == null) return;
        ele.setLastupdatedBy(ua);
        ci.updateCodeElement(ele);
        
    }
    
    
    /**
     * Logic intermediary for deactivation of CodeElements
     * 
     * @param ele to deactivate
     * @param ua doing the deactivation
     * @throws IntegrationException 
     */
    public void deactivateCodeElement(CodeElement ele, UserAuthorized ua) throws IntegrationException{
        
        CodeIntegrator ci = getCodeIntegrator();
        if(ua == null || ele == null) return;
        ele.setDeactivatedBy(ua);
        
        ci.deactivateCodeElement(ele);
        
        
        
    }
    
    
    // *************************************************************
    // **************CODE SETS (CODE BOOKS)*************************
    // *************************************************************

    
    /**
     * Retrieval portal for CodeSet objects
     * @param setID not zero
     * @return the code set object
     * @throws BObStatusException if setID is zero
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public CodeSet getCodeSet(int setID) throws BObStatusException, IntegrationException{
        if(setID == 0){

            throw new BObStatusException("0 is an invalid code set ID");
        }
        CodeIntegrator ci = getCodeIntegrator();
        return ci.getCodeSetBySetID(setID);
        
    }
    
    /**
     * Factory method for CodeSet objects
     * @return the object, with no injected values
     */
    public CodeSet getCodeSetSkeleton(){
        
        return new CodeSet();
    }
    
    
    /**
     * Logic intermediary for insertion of new code sets
     * @param set with the variables set
     * @return the PK of the new set
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int insertCodeSet(CodeSet set) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        return ci.insertCodeSetMetadata(set);
        
    }
    
    
    /**
     * Logic intermediary for updates to CodeSet objects
     * @param set 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updateCodeSetMetadata(CodeSet set) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        ci.updateCodeSetMetadata(set);
        
        
    }
    
    /**
     * Maps a municipality to a given code set, making it the muni's active set
     * @param set
     * @param muni 
     */
    public void activateCodeSetAsMuniDefault(CodeSet set, Municipality muni) throws BObStatusException, IntegrationException{
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        mi.mapCodeSetAsMuniDefault(set, muni);
        
        
    }
    
    /**
     * Logic Intermediary for deactivating a code set; checks to make sure
     * this code set is not the default for any muni
     * @param set 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void deactivateCodeSet(CodeSet set) throws IntegrationException, BObStatusException{
        CodeIntegrator ci = getCodeIntegrator();
        HashMap<Municipality, CodeSet> muniSetMap = ci.getMuniDefaultCodeSetMap();
        if(muniSetMap.containsValue(set)){

            throw new BObStatusException("Cannot deactivate a code set which is a default in ANY muni");
        } else {
            ci.deactivateCodeSet(set);
        }
        
    }
    
    /**
     * Extracts a mapping of municipalities and their default code sets 
     * @return
     * @throws IntegrationException 
     */
    public Map<Municipality, CodeSet> getMuniCodeSetDefaultMap() throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        
        return ci.getMuniDefaultCodeSetMap();
        
        
    }
   
    
    /**
     * Extracts a complete list of code sets from DB for configuration
     * @return
     * @throws IntegrationException 
     */
    public List<CodeSet> getCodeSetListComplete() throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        List<CodeSet> setList = ci.getCodeSets();
        return setList;
        
    }
    
  
    /**
     * Extracts CodeSets by muni
     * @param muniCode
     * @return 
     */
    public List<CodeSet> getCodeSetsFromMuniID(int muniCode) {
        
        CodeIntegrator integrator = getCodeIntegrator();
        
        try {
            return integrator.getCodeSets(muniCode);
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
        }
        
        return new ArrayList<>();
        
    }
    /**
     * Factory method for ECE skeletons
     * @param ele will be injected into ECE constructor if not null
     * @return the skeleton, possibly with injected Element, if passed in
     * and default values hard-coded into coordinator injected
     */
    public EnforcableCodeElement getEnforcableCodeElementSkeleton(CodeElement ele){
        EnforcableCodeElement ece = null;
        if(ele != null){
            ece = new EnforcableCodeElement(ele);
        } else {
            ece = new EnforcableCodeElement();
        }
        
        ece.setMaxPenalty(ECE_DEFAULT_PENALTY_MAX);
        ece.setNormPenalty(ECE_DEFAULT_PENALTY_NORM);
        ece.setMinPenalty(ECE_DEFAULT_PENALTY_MIN);
        
        ece.setNormDaysToComply(ECE_DEFAULT_DAYS_TO_COMPLY);
        ece.setDaysToComplyNotes(ECE_DEFAULT_DAYSTOCOMPLY_NOTES);
        
        return ece;
    }
    
 
    /**
     * Logic pass through for insertion of an ECE
     * 
     * @param ece to insert
     * @param cs to which the ECE should be linked
     * @param ua doing the insertion
     * @return ID of the fresh object in DB
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public int insertEnforcableCodeElement(EnforcableCodeElement ece, CodeSet cs, UserAuthorized ua) throws BObStatusException, IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(ece == null || ua == null || cs == null){
            throw new BObStatusException("Cannot insert ECE with null ECE or User");
        }
        
        ece.setEceCreatedBy(ua);
        ece.setEceLastupdatedBy(ua);
        ece.setCodeSetID(cs.getCodeSetID());
        return ci.insertEnforcableCodeElementToCodeSet(ece);
        
    }
    
    /**
     * Logic pass through for updates to an ECE
     * 
     * @param ece with updated fields populated
     * @param ua doing the updating
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void updateEnforcableCodeElement(EnforcableCodeElement ece, UserAuthorized ua) throws BObStatusException, IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(ece == null || ua == null){
            throw new BObStatusException("Cannot update ECE with null ECE or UA");
        }
        
        ece.setEceLastupdatedBy(ua);
        ci.updateEnforcableCodeElement(ece);
        
    }
    
    /**
     * Logic pass through for deactivation of ECEs
     * 
     * @param ece to deactivate
     * @param ua doing the deactivation
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void deactivateEnforcableCodeElement(EnforcableCodeElement ece, UserAuthorized ua) throws BObStatusException, IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(ece == null || ua == null){
            throw new BObStatusException("Cannot deactivate an ECE with null ECE or UA");
        }
        
        ece.setEceDeactivatedBy(ua);
        System.out.println("CodeCoordinator.deacECE");
        ci.deactivateEnforcableCodeElement(ece);
        
    }
    
    
    /**
     * Logic passthrough for retrieving ECEs
     * @param eceID
     * @return
     * @throws IntegrationException 
     */
    public EnforcableCodeElement getEnforcableCodeElement(int eceID) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        return ci.getEnforcableCodeElement(eceID);
        
    }
    
  
    
    /**
     * Extracts ECEs in a code set (code book)
     * @param setID
     * @return 
     */
    public List<EnforcableCodeElement> getCodeElementsFromCodeSetID(int setID){
        
        CodeIntegrator integrator = getCodeIntegrator();
        
        try {
            return integrator.getEnforcableCodeElementList(setID);
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
        }
     
        return new ArrayList<>();
        
    }
    
    
    
    // *************************************************************
    // ************************ CODE GUIDE *************************
    // *************************************************************
    
    
     
    
    
    
}
