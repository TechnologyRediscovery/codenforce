/*
 * Copyright (C) 2018 Turtle Creek Valley
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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric C. Darsow
 */
public class CodeElementListBB extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of CodeElementListBB
     */
    public CodeElementListBB() {
        System.out.println("CodeElementListBB.CodeElementListBB | const");
    }
    
    private CodeElement selectedElement;
    private ArrayList<CodeElement> codeElementList;
    private ArrayList<CodeElement> filteredCodeElementList;
    
    
    
    private CodeSource activeCodeSource;
    
    private ArrayList<CodeElement> loadCodeElementList(){
        ArrayList<CodeElement> elList = null;
        CodeSource source = getSessionBean().getActiveCodeSource();
        CodeIntegrator codeIntegrator = getCodeIntegrator();
        try {
            elList = codeIntegrator.getCodeElements(source.getSourceID());
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to populate list of code elements, sorry!", 
                            "This error will require administrator attention"));
        }
        return elList;
        
    }
    
    
    public String updateSelectedCodeElement(){
        System.out.println("CodeElementListBB.moveToUpdateCodeElement | selectedElement: " + selectedElement.getOrdchapterTitle());
        if(selectedElement != null){
            getSessionBean().setActiveCodeElement(selectedElement);
            return "codeElementUpdate";
            
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Hark--No elemented selected. Please click on a code element fi rst.", ""));
            return null;
        }
        
        
    }
    
   
    
    public void deleteCodeElement(ActionEvent event){
        
        
    }
    
    /**
     * Test sorting method: not working as of 28 June 18 -- used the PrimeFaces
     * documentation as a guide but this doesn't get us there
     * @param ob1
     * @param ob2
     * @return -1 ob1 < ob2, 0 ob1==ob2, 1 if ob1 > ob2
     */
    public int sortByChapter(Object ob1, Object ob2){
        CodeElement ele1 = (CodeElement) ob1;
        CodeElement ele2 = (CodeElement) ob2;
        
        if(ele1.getOrdchapterNo() < ele2.getOrdchapterNo()){
            return -1;
        } else if(ele1.getOrdchapterNo() == ele2.getOrdchapterNo()){
            return 0;
        } else {
            return 1;
        }
    }
    
    

    /**
     * @return the selectedElement
     */
    public CodeElement getSelectedElement() {
        return selectedElement;
    }

    /**
     * @param selectedElement the selectedElement to set
     */
    public void setSelectedElement(CodeElement selectedElement) {
        this.selectedElement = selectedElement;
    }

    /**
     * @return the codeElementList
     */
    public ArrayList<CodeElement> getCodeElementList() {
        if(codeElementList == null){
            codeElementList = loadCodeElementList();
        } 
        return codeElementList;
    }

    /**
     * @param codeElementList the codeElementList to set
     */
    public void setCodeElementList(ArrayList<CodeElement> codeElementList) {
        this.codeElementList = codeElementList;
    }

    /**
     * @return the activeCodeSource
     */
    public CodeSource getActiveCodeSource() {
        return activeCodeSource;
    }

    /**
     * @param activeCodeSource the activeCodeSource to set
     */
    public void setActiveCodeSource(CodeSource activeCodeSource) {
        this.activeCodeSource = activeCodeSource;
    }

    /**
     * @return the filteredCodeElementList
     */
    public ArrayList<CodeElement> getFilteredCodeElementList() {
        return filteredCodeElementList;
    }

    /**
     * @param filteredCodeElementList the filteredCodeElementList to set
     */
    public void setFilteredCodeElementList(ArrayList<CodeElement> filteredCodeElementList) {
        this.filteredCodeElementList = filteredCodeElementList;
    }

}
