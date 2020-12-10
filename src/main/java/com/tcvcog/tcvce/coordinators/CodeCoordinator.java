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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CodeCoordinator extends BackingBeanUtils implements Serializable {

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
    
    
    public List<CodeElement> getCodeElemements(CodeSource src) throws IntegrationException{
        if(src ==  null){
            return null;
        }
        CodeIntegrator ci = getCodeIntegrator();
        return ci.getCodeElements(src.getSourceID());
    }
    
    
    
    
   
    public int addCodeElement(CodeElement ele, UserAuthorized ua) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        int freshId = 0;
        if(ua == null || ele == null) return 0;
        ele.setCreatedBy(ua);
        ele.setLastupdatedBy(ua);
        freshId = ci.insertCodeElement(ele);
        return freshId;
        
    }
    
    
    public void updateCodeElement(CodeElement ele, UserAuthorized ua) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        if(ua == null || ele == null) return;
        ele.setLastupdatedBy(ua);
        ci.updateCodeElement(ele);
        
    }
    
    
    
    public void deactivateCodeElement(CodeElement ele, UserAuthorized ua) throws IntegrationException{
        
        CodeIntegrator ci = getCodeIntegrator();
        if(ua == null || ele == null) return;
        ele.setDeactivatedBy(ua);
        
        ci.deactivateCodeElement(ele);
        
        
        
    }
    
    
    // *************************************************************
    // **************CODE SETS (CODE BOOKS)*************************
    // *************************************************************
    
    public EnforcableCodeElement getEnforcableCodeElement(int eceID) throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        return ci.getEnforcableCodeElement(eceID);
        
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
    
  
    public List<CodeSet> getCodeSetsFromMuniID(int muniCode) {
        
        CodeIntegrator integrator = getCodeIntegrator();
        
        try {
            return integrator.getCodeSets(muniCode);
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
        }
        
        return new ArrayList<>();
        
    }
    
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
