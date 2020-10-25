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
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.TextBlock;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class TextBlockBB extends BackingBeanUtils implements Serializable{

    private List<TextBlock> blockList;
    private List<TextBlock> filteredBlockList;
    
    private TextBlock selectedBlock;
    
    private HashMap<String, Integer> categoryList;
    
    private Municipality formMuni;
    
    private String formBlockName;
    private String formBlockText;
    private int formCategoryID;
    private int formBlockOrder;
    
    
    /**
     * Creates a new instance of TextBlockBB
     */
    public TextBlockBB() {
    }
    
    public String updateTextBlock(){
        CaseIntegrator ci = getCaseIntegrator();
        if(selectedBlock != null){
            try {
                ci.updateTextBlock(selectedBlock);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,  
                            "Success! Updated text block id " + selectedBlock.getBlockID(), ""));
            } catch (IntegrationException ex) {
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,  
                            "Please select a text block and try again", ""));
            }
        } else {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            "Please select a text block and try again", ""));
            
        }
        // clear block list so the page reload forces a DB SELECT 
        blockList = null;
        return "";
    }
    
    public String addNewTextBlock(){
        CaseIntegrator ci = getCaseIntegrator();
        TextBlock newBlock = new TextBlock();
        newBlock.setMuni(formMuni);
        newBlock.setTextBlockCategoryID(formCategoryID);
        newBlock.setTextBlockName(formBlockName);
        newBlock.setTextBlockText(formBlockText);
        newBlock.setPlacementOrder(formBlockOrder);
        
        try {
            ci.insertTextBlock(newBlock);
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,  
                       "Success! Added a new text block named " + formBlockName + "to the db!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR,  
                       ex.getMessage(), ""));
            
        }
        blockList = null;
        return "";
    }
    
    public String nukeTextBlock(){
        CaseIntegrator ci = getCaseIntegrator();
        if(selectedBlock != null){
            try {
                ci.deleteTextBlock(selectedBlock);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,  
                            "Success! Nuked block id " + selectedBlock.getBlockID(), ""));
            } catch (IntegrationException ex) {
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,  ex.getMessage(), ""));
            }
        } else {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            "Please select a text block and try again", ""));
        }
        blockList = null;
        return "";
    }

    /**
     * @return the blockList
     */
    public List<TextBlock> getBlockList() {
        CaseIntegrator ci = getCaseIntegrator();
        if(blockList == null){
            try {
                blockList = ci.getAllTextBlocks();
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        return blockList;
    }

    /**
     * @return the filteredBlockList
     */
    public List<TextBlock> getFilteredBlockList() {
        return filteredBlockList;
    }

    /**
     * @return the selectedBlock
     */
    public TextBlock getSelectedBlock() {
        if(selectedBlock == null){
            selectedBlock = new TextBlock();
        }
        return selectedBlock;
    }

    /**
     * @return the categoryList
     */
    public HashMap<String, Integer> getCategoryList() {
        CaseIntegrator ci = getCaseIntegrator();
        try {
            categoryList = ci.getTextBlockCategoryMap();
            System.out.println("TextBlockBB.getCategoryMap | isempty: " + categoryList.isEmpty());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return categoryList;
    }

    /**
     * @return the formCategoryID
     */
    public int getFormCategoryID() {
        return formCategoryID;
    }

   

    /**
     * @return the formBlockName
     */
    public String getFormBlockName() {
        return formBlockName;
    }

    /**
     * @return the formBlockText
     */
    public String getFormBlockText() {
        return formBlockText;
    }

    /**
     * @param blockList the blockList to set
     */
    public void setBlockList(ArrayList<TextBlock> blockList) {
        this.blockList = blockList;
    }

    /**
     * @param filteredBlockList the filteredBlockList to set
     */
    public void setFilteredBlockList(ArrayList<TextBlock> filteredBlockList) {
        this.filteredBlockList = filteredBlockList;
    }

    /**
     * @param selectedBlock the selectedBlock to set
     */
    public void setSelectedBlock(TextBlock selectedBlock) {
        this.selectedBlock = selectedBlock;
    }

    /**
     * @param categoryList the categoryList to set
     */
    public void setCategoryList(HashMap<String, Integer> categoryList) {
        this.categoryList = categoryList;
    }

    /**
     * @param formCategoryID the formCategoryID to set
     */
    public void setFormCategoryID(int formCategoryID) {
        this.formCategoryID = formCategoryID;
    }

   

    /**
     * @param formBlockName the formBlockName to set
     */
    public void setFormBlockName(String formBlockName) {
        this.formBlockName = formBlockName;
    }

    /**
     * @param formBlockText the formBlockText to set
     */
    public void setFormBlockText(String formBlockText) {
        this.formBlockText = formBlockText;
    }

    /**
     * @return the formMuni
     */
    public Municipality getFormMuni() {
        return formMuni;
    }

    /**
     * @param formMuni the formMuni to set
     */
    public void setFormMuni(Municipality formMuni) {
        this.formMuni = formMuni;
    }

    /**
     * @return the formBlockOrder
     */
    public int getFormBlockOrder() {
        return formBlockOrder;
    }

    /**
     * @param formBlockOrder the formBlockOrder to set
     */
    public void setFormBlockOrder(int formBlockOrder) {
        this.formBlockOrder = formBlockOrder;
    }

 
    
}
