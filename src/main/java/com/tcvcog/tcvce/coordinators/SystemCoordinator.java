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
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.CaseStage;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eric C. Darsow
 */
public class SystemCoordinator extends BackingBeanUtils  implements Serializable{

    private Map<Integer, String> muniCodeNameMap;
    private final String SEPARATOR_LINE_TOP =       ">>>>>>>>>>>> NOTE >>>>>>>>>>>>";
    private final String SEPARATOR_LINE_BOTTOM =    "<<<<<<<<<< END NOTE <<<<<<<<<<";
    private final String INTERNAL_SEPARATOR =       "------------------------------";
    private final String HTML_BREAK = "<br>";
    private final String SPACE = " ";
    
            
    /**
     * Creates a new instance of LoggingCoordinator
     */
    public SystemCoordinator() {
    }
    
        
    public String appendNoteBlock(MessageBuilderParams mcc){
        StringBuilder sb = new StringBuilder();
        sb.append(mcc.existingContent);
        sb.append("<br />******************** NOTE ********************<br />");
        sb.append(mcc.header);
        sb.append("<br />");
        if(mcc.explanation != null){
            sb.append(mcc.explanation);
            sb.append("<br />");
        }
        sb.append("creatd by: ");
        sb.append(mcc.user.getPerson().getFirstName());
        sb.append(" ");
        sb.append(mcc.user.getPerson().getLastName());
        sb.append(" (username:  ");
        sb.append(mcc.user.getUsername());
        sb.append(", id#: ");
        sb.append(mcc.user.getUserID());
        sb.append(")");
        sb.append("<br />");
        sb.append(" at ");
        sb.append(getPrettyDate(LocalDateTime.now()));
        sb.append("<br />----------------note-text-----------------<br />");
        sb.append(mcc.newMessageContent);
        sb.append("<br />**************** END NOTE *****************<br />");
        return sb.toString();
    }
    
    
    public String formatAndAppendNote(User u, String noteToAppend, String existingText){
        StringBuilder sb = new StringBuilder();
        SystemCoordinator sc = SystemCoordinator();
        
       
            
       
        sb.append(HTML_BREAK);
        sb.append(SEPARATOR_LINE_TOP);
        sb.append(HTML_BREAK);

        sb.append(noteToAppend);
        
        sb.append(INTERNAL_SEPARATOR);
        sb.append("creator:");
        sb.append(SPACE);
        sb.append(u.getPerson().getFirstName());
        sb.append(SPACE);
        sb.append(u.getPerson().getLastName());
        sb.append(SPACE);
        sb.append("(user: ");
        sb.append(u.getUsername());
        sb.append(", id: ");
        sb.append(u.getUserID());
        sb.append(")");
        sb.append(HTML_BREAK);
        sb.append("timestamp: ");
        sb.append(getPrettyDate(LocalDateTime.now()));
        sb.append(SEPARATOR_LINE_BOTTOM);
        
        return sb.toString();
    }
    
    

    /**
     * @return the muniCodeNameMap
     */
    public Map<Integer, String> getMuniCodeNameMap() {
        if(muniCodeNameMap == null){
            
            Map<Integer, String> m = null;
            MunicipalityIntegrator mi = getMunicipalityIntegrator();
            try {
                m = mi.getMunicipalityMap();
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
            muniCodeNameMap = m;
        }
        return muniCodeNameMap;
    }
    
   

    /**
     * @param muniCodeNameMap the muniCodeNameMap to set
     */
    public void setMuniCodeNameMap(Map<Integer, String> muniCodeNameMap) {
        this.muniCodeNameMap = muniCodeNameMap;
    }

    public String getPrettyDate(LocalDateTime d) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM yyyy, HH:mm");
        if (d != null) {
            String formattedDateTime = d.format(formatter);
            return formattedDateTime;
        } else {
            return "";
        }
    }
    
    
    
}
