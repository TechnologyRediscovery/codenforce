/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.occupancy;

import java.util.List;

/**
 * Java only container for holding a set of
 * code elements for display in an accordion panel.
 * Contains a title member and a list of inspected elements
 * 
 * @author Ellen Bascomb of apartment 31Y
 */
public class OccInsElementGroup {
   private String groupTitle;
   private List<OccInspectedSpaceElement> elementList;

   /**
    * No arg const
    */
   public OccInsElementGroup(){
       
       // no args
   }
   
   /**
    * Injects params into members in the expected way
    * @param t
    * @param elelist 
    */
   public OccInsElementGroup(String t, List<OccInspectedSpaceElement> elelist){
       this.groupTitle = t;
       this.elementList = elelist;
   }
   
    /**
     * @return the groupTitle
     */
    public String getGroupTitle() {
        return groupTitle;
    }

    /**
     * @return the elementList
     */
    public List<OccInspectedSpaceElement> getElementList() {
        return elementList;
    }

    /**
     * @param groupTitle the groupTitle to set
     */
    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    /**
     * @param elementList the elementList to set
     */
    public void setElementList(List<OccInspectedSpaceElement> elementList) {
        this.elementList = elementList;
    }
    
    
}
