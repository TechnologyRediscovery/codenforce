/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.WorkflowCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Choice;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import org.omg.CORBA.CurrentHolder;

/**
 * Backing Bean serving 
 * <ul>
 * <li>CECaseWorkflow.xhtml</li>
 * <li>OccPeriodWorkflow.xhtml</li>
 * </ul>
 * by providing all viewing and evaluation methods for workflow related objects
 * such as Proposal objects, Choice objects inside them, and the entire EventRule 
 * family
 * @author sylvia
 */
public class WorkflowConfigBB extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of ChoiceProposalBB
     */
    public WorkflowConfigBB() {
    }
    
   
    
    @PostConstruct
    public void initBean(){
       
    }
    
  
    
}
