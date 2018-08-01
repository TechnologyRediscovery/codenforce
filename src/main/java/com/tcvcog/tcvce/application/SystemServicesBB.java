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
import com.tcvcog.tcvce.entities.ImprovementSuggestion;
import com.tcvcog.tcvce.entities.ListChangeRequest;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class SystemServicesBB extends BackingBeanUtils implements Serializable{

    private String listItemChangeRequestRText;
    
    private String systemImprovementTicketRText;
    private int selectedImprovementType;
    private HashMap<String, Integer> improvementTypeMap;
    
    private ArrayList<ImprovementSuggestion> impList;
    private ArrayList<ImprovementSuggestion> filteredImpList;
    
    private ResultSet impSugs;
    
    
    /**
     * Creates a new instance of SystemServicesBB
     */
    public SystemServicesBB() {
    }
    
    public String closeRS(){
        if(impSugs != null){
            try {
                impSugs.close();
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        }
        return "missionControl";
    }

    public String submitImprovementSuggestion(){
        
        SystemIntegrator si = getSystemIntegrator();

        ImprovementSuggestion is = new ImprovementSuggestion();
        
        is.setSubmitter(getFacesUser());
        is.setImprovementTypeID(selectedImprovementType);
        is.setSuggestionText(systemImprovementTicketRText);
        // back to the hard-coded since I couldn't get the resource bundle lookup
        // to work after 5 tries!!
        is.setStatusID(1);
//        is.setStatusID(Integer.parseInt(getResourceBundle("dbFixedValueLookup")
//                .getString("suggestionSubmittedTypeID")));
        
        try {
            si.insertImprovementSuggestion(is);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                   "Suggesetion Submitted! Thanks!",""));
            return "ticketListing";
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    ex.getLocalizedMessage(), 
                    "Unable to log bug--which is a bug itself! Crazy! This issue will need to be addressed by the system admin, sorry"));
        }
        return "";
    }
    
    public String submitListItemChange(){
        SystemIntegrator si = getSystemIntegrator();
        ListChangeRequest lcr = new ListChangeRequest();
        lcr.setChangeRequestText(listItemChangeRequestRText);
        try {
            si.insertListChangeRequest(lcr);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                   "Change request submitted! Thanks!",""));
            return "missionControl";
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                         ex.getLocalizedMessage(), 
                         "This issue will need to be addressed by the system admin, sorry"));
        }
        return "";
        
    }
    
    /**
     * @return the listItemChangeRequestRText
     */
    public String getListItemChangeRequestRText() {
        return listItemChangeRequestRText;
    }

    /**
     * @return the systemImprovementTicketRText
     */
    public String getSystemImprovementTicketRText() {
        return systemImprovementTicketRText;
    }

    /**
     * @param listItemChangeRequestRText the listItemChangeRequestRText to set
     */
    public void setListItemChangeRequestRText(String listItemChangeRequestRText) {
        this.listItemChangeRequestRText = listItemChangeRequestRText;
    }

    /**
     * @param systemImprovementTicketRText the systemImprovementTicketRText to set
     */
    public void setSystemImprovementTicketRText(String systemImprovementTicketRText) {
        this.systemImprovementTicketRText = systemImprovementTicketRText;
    }

    /**
     * @return the selectedImprovementType
     */
    public int getSelectedImprovementType() {
        return selectedImprovementType;
    }

    /**
     * @param selectedImprovementType the selectedImprovementType to set
     */
    public void setSelectedImprovementType(int selectedImprovementType) {
        this.selectedImprovementType = selectedImprovementType;
    }

    /**
     * @return the improvementTypeMap
     */
    public HashMap<String, Integer> getImprovementTypeMap() {
        SystemIntegrator si = getSystemIntegrator();
        try {
            improvementTypeMap = si.getSuggestionTypeMap();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return improvementTypeMap;
    }

    /**
     * @param improvementTypeMap the improvementTypeMap to set
     */
    public void setImprovementTypeMap(HashMap<String, Integer> improvementTypeMap) {
        this.improvementTypeMap = improvementTypeMap;
    }

    /**
     * @return the impList
     */
    public ArrayList<ImprovementSuggestion> getImpList() {
        SystemIntegrator si = getSystemIntegrator();
        if(impList == null){
            try {
                impList = si.getImprovementSuggestions();
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        return impList;
    }

    /**
     * @param impList the impList to set
     */
    public void setImpList(ArrayList<ImprovementSuggestion> impList) {
        this.impList = impList;
    }

    /**
     * @return the filteredImpList
     */
    public ArrayList<ImprovementSuggestion> getFilteredImpList() {
        return filteredImpList;
    }

    /**
     * @param filteredImpList the filteredImpList to set
     */
    public void setFilteredImpList(ArrayList<ImprovementSuggestion> filteredImpList) {
        this.filteredImpList = filteredImpList;
    }

    /**
     * @return the impSugs
     */
    public ResultSet getImpSugs() {
        String impSugQuery =  "SELECT improvementid, improvementtypeid, improvementsuggestiontext, \n" +
                        "       improvementreply, statusid, statustitle, typetitle, submitterid, submissiontimestamp\n" +
                        "  FROM public.improvementsuggestion INNER JOIN improvementstatus USING (statusid)\n" +
                        "  INNER JOIN improvementtype ON improvementtypeid = typeid;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
 
        try {
            stmt = con.prepareStatement(impSugQuery);
            rs = stmt.executeQuery();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
//             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        impSugs = rs;
        
//        SystemIntegrator si = getSystemIntegrator();
//        try {
//            impSugs = si.getImprovementSuggestionsRS();
//        } catch (IntegrationException ex) {
//            System.out.println(ex);
//        }
        return impSugs;
    }

    /**
     * @param impSugs the impSugs to set
     */
    public void setImpSugs(ResultSet impSugs) {
        this.impSugs = impSugs;
    }
    
    
    
}
