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


import com.tcvcog.tcvce.coordinators.CodeCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CodeElementBB extends BackingBeanUtils implements Serializable{

     /**
     * Creates a new instance of CodeElementBB
     */
    public CodeElementBB() {
    }

    private CodeElement currentElement;

    private CodeSource currentCodeSource;

   
    @PostConstruct
    public void initBean() {
        
    }

    public String commitUpdatesToCodeElement() {
        CodeIntegrator integrator = getCodeIntegrator();

        // elemet ID is already in our currentElementObject

        try {
            integrator.updateCodeElement(currentElement);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Code element updated!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to update code element; most sincere apologies!",
                            "This must be corrected by the System Administrator"));

        }

        return "codeElementList";
    }

    public void deleteCodeElement(ActionEvent event) {

    }

    public String insertCodeElement() {

        CodeIntegrator codeIntegrator = getCodeIntegrator();
        CodeElement newCE = new CodeElement();

        newCE.setSource(currentCodeSource);

        try {
            codeIntegrator.insertCodeElement(newCE);
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add code elment to code source",
                            "This must be corrected by the System Administrator"));
        }

        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Successfully added code element to code source", ""));

        return "codeSourceManage";
    }

 

    /**
     * @param currentCodeSource the currentCodeSource to set
     */
    public void setCurrentCodeSource(CodeSource currentCodeSource) {
        this.currentCodeSource = currentCodeSource;
    }

    /**
     * @return the currentElement
     */
    public CodeElement getCurrentElement() {
        currentElement = getSessionBean().getActiveCodeElement();
        return currentElement;
    }

    /**
     * @param currentElement the currentElement to set
     */
    public void setCurrentElement(CodeElement currentElement) {
        this.currentElement = currentElement;
    }

 

    /**
     * Listener for user requests to begin creating a new code element
     */
    public void onElementAddInitButtonChange(){
        CodeCoordinator cc = getCodeCoordinator();
        currentElement = cc.getCodeElementSkeleton();
        
    }
    
    /**
     * Listener for user requests to commit their new element
     */
    public void onElementAddCommitButtonChange() {
        //xiaohogn add
        currentCodeSource = getSessionBean().getSessCodeSource();
        currentElement.setSource(currentCodeSource);

        CodeIntegrator codeIntegrator = getCodeIntegrator();
        try {
            codeIntegrator.insertCodeElement(currentElement);
            
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Successfully added code element to code source", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add code elment to code source",
                            "This must be corrected by the System Administrator"));
        }
    }
}
