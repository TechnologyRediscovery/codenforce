/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.application.interfaces;

import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.EventRuleImplementation;
import com.tcvcog.tcvce.entities.IFace_EventHolder;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import java.util.List;

/**
 * Specifies a unified set of behaviors for business objects whose management
 * is directed by EventRule objects--which in short specify a required or forbidden
 * type or category of EventCnF
 * 
 * For beta launch, these were CECase and OccPeriod objects
 *  
 * @author sylvia
 */
public interface IFace_EventRuleGoverned extends IFace_EventHolder {
    
    public EventDomainEnum discloseEventDomain();
    
    public int getBObID();
    
//    public void setEventList(List<EventCnF> lst);
//
//    public List<EventCnF> assembleEventList(ViewOptionsActiveHiddenListsEnum voahle);
//    
    public void setEventRuleList(List<EventRuleImplementation> lst);

    public List<EventRuleImplementation> assembleEventRuleList(ViewOptionsEventRulesEnum voere);
    
    public boolean isAllRulesPassed();
     
    public void setProposalList(List<Proposal> propList);
    
    public List<Proposal> assembleProposalList(ViewOptionsProposalsEnum vope); 
    
    public boolean isOpen();
    
}
