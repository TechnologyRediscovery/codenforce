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
import com.tcvcog.tcvce.util.Constants;
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
   
            
    /**
     * Creates a new instance of LoggingCoordinator
     */
    public SystemCoordinator() {
    }
    
        
    public String appendNoteBlock(MessageBuilderParams mbp){
        StringBuilder sb = new StringBuilder();
        if(mbp.getExistingContent() != null){
            sb.append(mbp.getExistingContent());
        }
        sb.append(Constants.FMT_HTML_BREAK);
        sb.append(Constants.FMT_NOTE_START);
        if(mbp.getHeader() != null){
            sb.append(Constants.FMT_HTML_BREAK);
            sb.append(mbp.getHeader());
        }
        if(mbp.getNewMessageContent() != null){
            sb.append(Constants.FMT_HTML_BREAK);
            sb.append(mbp.getNewMessageContent());
        }
        sb.append(Constants.FMT_HTML_BREAK);
        if(mbp.getExplanation() != null){
            sb.append(Constants.FMT_HTML_BREAK);
            sb.append(mbp.getExplanation() );
        }
        sb.append(Constants.FMT_HTML_BREAK);
        
        sb.append(Constants.FMT_NOTE_SEP_INTERNAL);
        sb.append(mbp.getUser().getPerson().getFirstName());
        sb.append(Constants.FMT_SPACE_LITERAL);
        sb.append(mbp.getUser().getPerson().getLastName());
        sb.append(Constants.FMT_SPACE_LITERAL);
        sb.append(Constants.FMT_DTYPE_SYMB_USERNAME);
        sb.append(mbp.getUser().getUsername());
        sb.append(Constants.FMT_DTYPE_SYMB_USERNAME);
        sb.append(Constants.FMT_DTYPE_OBJECTID_INLINEOPEN);
        sb.append(mbp.getUser().getUserID());
        sb.append(Constants.FMT_DTYPE_OBJECTID_INLINECLOSED);
        
        sb.append(Constants.FMT_HTML_BREAK);
        
        sb.append(Constants.FMT_DTYPE_KEY_TIMESTAMP_CREATE);
        sb.append(Constants.FMT_DTYPE_KEYVALDESCSEP);
        sb.append(stampCurrentTimeForNote());
        sb.append(Constants.FMT_DTYPE_SYMB_USERNAME);
        sb.append(mbp.getUser().getUsername());
        sb.append(Constants.FMT_DTYPE_OBJECTID_INLINEOPEN);
        sb.append(mbp.getUser().getUserID());
        sb.append(Constants.FMT_DTYPE_OBJECTID_INLINECLOSED);
        
        return sb.toString();
    }
    
    public String stampCurrentTimeForNote(){
        return getPrettyDate(LocalDateTime.now());
    }
    
    
    
    public String formatAndAppendNote(User u, String noteToAppend, String existingText){
        return appendNoteBlock(new MessageBuilderParams(existingText, noteToAppend, null, null, u));
        
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

    @Override
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
