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
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
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
    
    public CodeSource retrieveCodeSourceByID(int sourceID) throws IntegrationException{
        CodeIntegrator integrator = getCodeIntegrator();
        
        
        CodeSource s = integrator.getCodeSource(sourceID);
        System.out.println("CodeCoordinator.retrieveCodeSourceByID: sourceName: " + s.getSourceName());
        if(s.getSourceName() == null){
            
            System.out.println("CodeCoordinator.retrieveCodeSourceByID: codeSource is null--throwing IntegrationException: ");
            throw new IntegrationException("Cannot Find Source by that name");
        }
        return s;
    }
    
    /**
     * Extracts a complete list of code sets from DB for configuration
     * @return
     * @throws IntegrationException 
     */
    public List<CodeSet> getCodeSetListComplete() throws IntegrationException{
        CodeIntegrator ci = getCodeIntegrator();
        List<CodeSet> setList = new ArrayList<>();
        
        setList = ci.getCodeSets();
        return setList;
        
    }
    
    public ArrayList<CodeSource> retrieveAllCodeSources() throws IntegrationException{
        CodeIntegrator integrator = getCodeIntegrator();
        ArrayList<CodeSource> sources = integrator.getCompleteCodeSourceList();
        return sources;
    }
    
    public void updateCodeSource(CodeSource source) throws IntegrationException{
        getCodeIntegrator().updateCodeSource(source);
        
    }
    
    public void addNewCodeSource(CodeSource source) throws IntegrationException{
        System.out.println("CodeCoordinator.addNewCodeSource");
        getCodeIntegrator().insertCodeSource(source);
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
    
}
