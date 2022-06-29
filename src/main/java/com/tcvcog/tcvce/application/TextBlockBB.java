/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.TextBlock;
import com.tcvcog.tcvce.entities.TextBlockCategory;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Backs UI elements for adding, editing, and deactivating text blocks
 * which form the basis of Letters (NOVs, etc) and canned remarks on permits
 * and inspections
 * @author Ellen Bascomb of Apartment 31Y
 */
public class TextBlockBB extends BackingBeanUtils {
    
    static final String VIOLATION_INJECTION_MARKER = "***VIOLATIONS***";
    
    
    private TextBlock currentTextBlock;
    private List<TextBlock> blockList;
    private boolean editModeTextBlock;
    private boolean loadAllMunisTextBlock;
    private boolean currentTextBlockActive;
    
    private TextBlockCategory currentTextBlockCategory;
    private List<TextBlockCategory> textBlockCategoryList;
    private boolean currentTextBlockCategoryActive;
    private boolean editModeTextBlockCategory;
    private boolean loadAllMunisTextBlockCategory;

    private List<Icon> iconList;
    
    /**
     * Creates a new instance of TextBlockBB
     */
    public TextBlockBB() {
    }
    
    
     @PostConstruct
    public void initBean() {
        SystemCoordinator sc = getSystemCoordinator();
        try {
            iconList = sc.getIconList();
            editModeTextBlock = false;
            editModeTextBlockCategory = false;
            loadAllMunisTextBlock = false;
            loadAllMunisTextBlockCategory = false;
            
            refreshTextBlockAndCatAndLists();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    /**
     * Listener for start of manage process
     * @param ev 
     */
    public void onManageBlocksInit(ActionEvent ev){
        System.out.println("TextBlockBB.onManageBlocksInit");
    }
    
    
    /**
     * JSF Dynamic accessor of the injection marker string for use in Injectable templates
     * @return 
     */
    public String getViolationInjectionMarker(){
        return VIOLATION_INJECTION_MARKER;
    }
    
    /**
     * Internal organ for setting up our master lists
     */
    public void refreshTextBlockAndCatAndLists(){
        SystemCoordinator sc = getSystemCoordinator();
        try {
            if(currentTextBlock != null){
                sc.getTextBlock(currentTextBlock.getBlockID());
            }
            if(currentTextBlockCategory != null){
                currentTextBlockCategory = sc.getTextBlockCategory(currentTextBlockCategory.getCategoryID());
            }
            if(loadAllMunisTextBlock){
                blockList = sc.getTextBlockList(null, null);
            } else {
                blockList = sc.getTextBlockList(null, getSessionBean().getSessMuni());    
            }
            if(loadAllMunisTextBlockCategory){
                textBlockCategoryList = sc.getTextBlockCategoryListComplete();
            } else {
                // TODO: customize for muni-specific view?
                textBlockCategoryList = sc.getTextBlockCategoryListComplete();
            }
            
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        } 
        
        
    }
    
    /**
     * Listener to start the cat manage process
     * @param ev 
     */
    public void onTextBlockCategoryManageInit(ActionEvent ev){
        System.out.println("TextBlockBB.onTextBlockCategoryManageInit");
    }
    
    /**
     * Listener for block select clicking
     * @param tb 
     */
    public void onBlockSelect(TextBlock tb){
        currentTextBlock = tb;
        
    }
    
    /**
     * Listener for block category select clicking
     * @param tbc 
     */
    public void onBlockCategorySelect(TextBlockCategory tbc){
        setCurrentTextBlockCategory(tbc);
    }
    
  
    
    // MIGRATED FROM TEXT BLOCK BB // There used to be one!
    // and now back to a TextBlockBB
    
    
 
    /**
     * lISTENER FOR Toggling of text block categories
     * @param ev 
     */
    public void onEditModeTextBlockToggle(ActionEvent ev){
        System.out.println("TextBlockConfigBB.onToggleTextBlockEditMode | incoming value: " + editModeTextBlock);
        if(editModeTextBlock){
            try {
                if(currentTextBlock != null){
                    if(currentTextBlock.getBlockID()== 0){
                        onTextBlockAddCommitButtonChange();
                    } else {
                        onTextBlockUpdateCommit();
                    }
                } else {
                       getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Event Category page configuration error EC1: currentTextBlockegory null", 
                            ""));
                }
            } catch (BObStatusException ex) {
                System.out.println();
                getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Text block page configuration error EC1",
                        ex.getMessage()));
            }
        } else {
            // nothing to do--toggle on edit mode below
        }
        editModeTextBlock = !editModeTextBlock;
    }
    
    
    /**
     * Listener for abort requests
     * @param ev 
     */
    public void onTextBlockOperationAbort(ActionEvent ev){
        editModeTextBlock = false;
    }
    
    /**
     * Listener for abort requests
     * @param ev 
     */
    public void onTextBlockCategoryOperationAbort(ActionEvent ev){
        editModeTextBlockCategory = false;
    }
    
    /**
     * Listener to start the creation process of a text block
     * @param ev 
     */
    public void onTextBlockAddInit(ActionEvent ev){
        SystemCoordinator sc = getSystemCoordinator();
        currentTextBlock = sc.getTextBlockSkeleton(getSessionBean().getSessMuni());
        editModeTextBlock = true;
    }
    
    
    
    
    /**
     * Listener for user requests to finalize the text block creation process
     * @param ev 
     */
    private void onTextBlockAddCommitButtonChange() throws BObStatusException{
        SystemCoordinator sc = getSystemCoordinator();
        int freshID = 0;
        
        try {
            freshID = sc.insertTextBlock(currentTextBlock, getSessionBean().getSessUser());
            currentTextBlock = sc.getTextBlock(freshID);
            refreshTextBlockAndCatAndLists();
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,  
                       "Success! Added a new text block to the db!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR,  
                       ex.getMessage(), ""));
            
        }
    }
    
    /**
     * Listener for updates to text blocks
     * @throws BObStatusException 
     */
    private void onTextBlockUpdateCommit() throws BObStatusException{
      SystemCoordinator sc = getSystemCoordinator();
        if(currentTextBlock != null){
            try {
                if(currentTextBlock.getDeactivatedTS() == null && !currentTextBlockActive){
                    System.out.println("TextBlockBB.onTextBlockUpdateCommit | deac trigger! Text block ID: " + currentTextBlock.getBlockID());
                    onTextBlockDeactivateCommit();
                }
                if(currentTextBlock.getDeactivatedTS() != null && currentTextBlockActive){
                    currentTextBlock.setDeactivatedTS(null);
                    System.out.println("TextBlockBB.onTextBlockUpdateCommit | reactivating text block ID: " + currentTextBlock.getBlockID());
                }
                sc.updateTextBlock(currentTextBlock, getSessionBean().getSessUser());
                refreshTextBlockAndCatAndLists();
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,  
                            "Success! Updated text block id " + currentTextBlock.getBlockID(), ""));
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,  
                            "Please select a text block and try again", ""));
            }
        } else {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            "Please select a text block and try again", ""));
            
        }
    }
    
    
    
    public void onTextBlockDeactivateInit(ActionEvent ev){
        System.out.println("TextBlockDeacInit");
        
    }
    
    
    private void onTextBlockDeactivateCommit(){
        SystemCoordinator sc = getSystemCoordinator();
        if(currentTextBlock != null){
            try {
                sc.deactivateTextBlock(currentTextBlock, getSessionBean().getSessUser());
                refreshTextBlockAndCatAndLists();
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,  
                            "Success! Deactivated block id " + currentTextBlock.getBlockID(), ""));
            } catch (IntegrationException | BObStatusException ex) {
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,  ex.getMessage(), ""));
            }
        } else {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            "Please select a text block and try again", ""));
        }
    }
    
    /**
     * lISTENER FOR Toggling of text block categories
     * @param ev 
     */
    public void onEditModeTextBlockCategoryToggle(ActionEvent ev){
        System.out.println("TextBlockConfigBB.onToggleTextBlockEditMode | incoming value: " + editModeTextBlock);
        if(editModeTextBlockCategory){
            try {
                if(currentTextBlockCategory != null){
                
                    if(currentTextBlockCategory.getCategoryID() == 0){
                        onTextBlockCategoryAddCommitButtonChange();
                    } else {
                        onTextBlockCategoryUpdateCommit();
                    }
                } else {
                       getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Text block Category page configuration error EC1: currentTextBlockegory null", 
                            ""));
                }
            } catch (BObStatusException ex) {
                System.out.println();
                getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Text block Category page configuration error EC1",
                        ex.getMessage()));
            }
        } else {
            // nothing to do--toggle on edit mode below
        }
        editModeTextBlockCategory = !editModeTextBlockCategory;
    }
    
    
    
    
    /**
     * Listener to start the creation process of a text block
     * @param ev 
     */
    public void onTextBlockCategoryAddInit(ActionEvent ev){
        SystemCoordinator sc = getSystemCoordinator();
        currentTextBlockCategory = sc.getTextBlockCategorySkeleton(getSessionBean().getSessMuni());
        editModeTextBlockCategory = true;
    }
    
    
    
    
    /**
     * Listener for user requests to finalize the text block creation process
     * @param ev 
     */
    private void onTextBlockCategoryAddCommitButtonChange() throws BObStatusException{
        SystemCoordinator sc = getSystemCoordinator();
        int freshID = 0;
        
        try {
            freshID = sc.insertTextBlockCategory(currentTextBlockCategory);
            currentTextBlockCategory = sc.getTextBlockCategory(freshID);
            refreshTextBlockAndCatAndLists();
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,  
                       "Success! Added a new text block Category to the db!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR,  
                       ex.getMessage(), ""));
            
        }
    }
    
    /**
     * Listener for updates to text blocks
     * @throws BObStatusException 
     */
    private void onTextBlockCategoryUpdateCommit() throws BObStatusException{
      SystemCoordinator sc = getSystemCoordinator();
        if(currentTextBlockCategory != null){
            try {
                 // deal with deactivation TS adaptor to boolean box on UI
                if(currentTextBlockCategory.getDeactivatedTS() == null && !currentTextBlockCategoryActive){
                        System.out.println("TextBlockBB.onTextBlockCategoryUpdateCommit | deac trigger! text blockCat ID: " + currentTextBlockCategory.getCategoryID());
                       onTextBlockCategoryDeactivateCommit();
                }
                if(currentTextBlockCategory.getDeactivatedTS() != null && currentTextBlockCategoryActive){
                    currentTextBlockCategory.setDeactivatedTS(null);
                    System.out.println("TextBlockBB.onTextBlockCategoryUpdateCommit | reactivating text blockCat ID: " + currentTextBlockCategory.getCategoryID());
                }
                sc.updateTextBlockCategory(currentTextBlockCategory);
                refreshTextBlockAndCatAndLists();
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,  
                            "Success! Updated text block Category id " + currentTextBlockCategory.getCategoryID(), ""));
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,  
                            "Please select a text block  Category and try again", ""));
            }
        } else {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            "Please select a text block  Category and try again", ""));
            
        }
    }
    
    
    
    public void onTextBlockCategoryDeactivateInit(ActionEvent ev){
        System.out.println("TextBlockCategoryDeacInit");
        
    }
    
    
    private void onTextBlockCategoryDeactivateCommit(){
        SystemCoordinator sc = getSystemCoordinator();
        if(currentTextBlockCategory != null){
            try {
                sc.deactivateTextBlockCategory(currentTextBlockCategory);
                refreshTextBlockAndCatAndLists();
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,  
                            "Success! Nuked block cat id " + currentTextBlockCategory.getCategoryID(), ""));
            } catch (IntegrationException | BObStatusException ex) {
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,  ex.getMessage(), ""));
            }
        } else {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            "Please select a text block category and try again", ""));
        }
    }
    
    
     /**
      * Special getter that looks at the text block deac TS field
     * @return the currentTextBlockActive
     */
    public boolean isCurrentTextBlockActive() {
        currentTextBlockActive = false;
        if(currentTextBlock != null){
            if(currentTextBlock.getDeactivatedTS() == null){
                currentTextBlockActive = true;
            } 
        }
        return currentTextBlockActive;
    }



    /**
     * Special adaptor getter that examines the current text block category's deac field
     * @return the currentTextBlockCategoryActive
     */
    public boolean isCurrentTextBlockCategoryActive() {
        currentTextBlockCategoryActive = false;
        if(currentTextBlockCategory != null){
            if(currentTextBlockCategory.getDeactivatedTS() == null){
                currentTextBlockCategoryActive = true;
            } 
        }
        return currentTextBlockCategoryActive;
    }
    
    
    /**
     * *************************************************************
     *  **************** GETTERS AND SETTERS ***********************
     * *************************************************************
     */
    

    /**
     * @return the currentTextBlock
     */
    public TextBlock getCurrentTextBlock() {
        return currentTextBlock;
    }

    /**
     * @param currentTextBlock the currentTextBlock to set
     */
    public void setCurrentTextBlock(TextBlock currentTextBlock) {
        this.currentTextBlock = currentTextBlock;
    }

    /**
     * @return the blockList
     */
    public List<TextBlock> getBlockList() {
        return blockList;
    }

    /**
     * @return the currentTextBlockCategory
     */
    public TextBlockCategory getCurrentTextBlockCategory() {
        return currentTextBlockCategory;
    }

    /**
     * @return the textBlockCategoryList
     */
    public List<TextBlockCategory> getTextBlockCategoryList() {
        return textBlockCategoryList;
    }

    /**
     * @param blockList the blockList to set
     */
    public void setBlockList(List<TextBlock> blockList) {
        this.blockList = blockList;
    }

    /**
     * @param currentTextBlockCategory the currentTextBlockCategory to set
     */
    public void setCurrentTextBlockCategory(TextBlockCategory currentTextBlockCategory) {
        this.currentTextBlockCategory = currentTextBlockCategory;
    }

    /**
     * @param textBlockCategoryList the textBlockCategoryList to set
     */
    public void setTextBlockCategoryList(List<TextBlockCategory> textBlockCategoryList) {
        this.textBlockCategoryList = textBlockCategoryList;
    }

    /**
     * @return the editModeTextBlockCategory
     */
    public boolean isEditModeTextBlockCategory() {
        return editModeTextBlockCategory;
    }

    /**
     * @param editModeTextBlockCategory the editModeTextBlockCategory to set
     */
    public void setEditModeTextBlockCategory(boolean editModeTextBlockCategory) {
        this.editModeTextBlockCategory = editModeTextBlockCategory;
    }

    /**
     * @return the editModeTextBlock
     */
    public boolean isEditModeTextBlock() {
        return editModeTextBlock;
    }

    /**
     * @param editModeTextBlock the editModeTextBlock to set
     */
    public void setEditModeTextBlock(boolean editModeTextBlock) {
        this.editModeTextBlock = editModeTextBlock;
    }

    /**
     * @return the loadAllMunisTextBlockCategory
     */
    public boolean isLoadAllMunisTextBlockCategory() {
        return loadAllMunisTextBlockCategory;
    }

    /**
     * @param loadAllMunisTextBlockCategory the loadAllMunisTextBlockCategory to set
     */
    public void setLoadAllMunisTextBlockCategory(boolean loadAllMunisTextBlockCategory) {
        this.loadAllMunisTextBlockCategory = loadAllMunisTextBlockCategory;
    }

    /**
     * @return the loadAllMunisTextBlock
     */
    public boolean isLoadAllMunisTextBlock() {
        return loadAllMunisTextBlock;
    }

    /**
     * @param loadAllMunisTextBlock the loadAllMunisTextBlock to set
     */
    public void setLoadAllMunisTextBlock(boolean loadAllMunisTextBlock) {
        this.loadAllMunisTextBlock = loadAllMunisTextBlock;
    }

    /**
     * @return the iconList
     */
    public List<Icon> getIconList() {
        return iconList;
    }

    /**
     * @param iconList the iconList to set
     */
    public void setIconList(List<Icon> iconList) {
        this.iconList = iconList;
    }

   
    
        /**
     * @param currentTextBlockActive the currentTextBlockActive to set
     */
    public void setCurrentTextBlockActive(boolean currentTextBlockActive) {
        this.currentTextBlockActive = currentTextBlockActive;
    }

    /**
     * @param currentTextBlockCategoryActive the currentTextBlockCategoryActive to set
     */
    public void setCurrentTextBlockCategoryActive(boolean currentTextBlockCategoryActive) {
        this.currentTextBlockCategoryActive = currentTextBlockCategoryActive;
    }
}
