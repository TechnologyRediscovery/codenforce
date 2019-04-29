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
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.CaseStage;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

/**
 *
 * @author Eric C. Darsow
 */
public class SessionSystemCoordinator extends BackingBeanUtils implements Serializable{

    private Map<Integer, String> muniCodeNameMap;
    
    /**
     * Creates a new instance of LoggingCoordinator
     */
    public SessionSystemCoordinator() {
    }
    
    public Map<CasePhase, Integer> getCaseCountsByPhase(List<CECase> caseList) throws IntegrationException{
        
        Map<CasePhase, Integer> phaseCountMap = new LinkedHashMap<>();
        
        phaseCountMap.put(CasePhase.PrelimInvestigationPending, 0);
        phaseCountMap.put(CasePhase.NoticeDelivery, 0);
        phaseCountMap.put(CasePhase.InitialComplianceTimeframe, 0);
        phaseCountMap.put(CasePhase.SecondaryComplianceTimeframe, 0);
        phaseCountMap.put(CasePhase.AwaitingHearingDate, 0);
        phaseCountMap.put(CasePhase.HearingPreparation, 0);
        phaseCountMap.put(CasePhase.InitialPostHearingComplianceTimeframe, 0);
        phaseCountMap.put(CasePhase.SecondaryPostHearingComplianceTimeframe, 0);
        phaseCountMap.put(CasePhase.Closed, 0);
        phaseCountMap.put(CasePhase.LegacyImported, 0);
        phaseCountMap.put(CasePhase.InactiveHolding, 0);
        
        //CasePhase[] phaseValuesArray = CasePhase.values();
        
        Iterator<CECase> caseIter = caseList.iterator();
        while(caseIter.hasNext()){
            CasePhase p = caseIter.next().getCasePhase();
            phaseCountMap.put(p, phaseCountMap.get(p) + 1);
        }
        return phaseCountMap;
    }
    
    public Map<CaseStage, Integer> getCaseCountsByStage(List<CECase> caseList) throws IntegrationException, CaseLifecyleException{
        Map<CaseStage, Integer> stageCountMap = new LinkedHashMap<>();
        List<CaseStage> stageList = Arrays.asList(CaseStage.values());
        for(CaseStage cs: stageList){
            stageCountMap.put(cs, 0);
        }
        for(CECase c: caseList){
            CaseStage stg = getCaseStage(c.getCasePhase());
            stageCountMap.put(stg, stageCountMap.get(stg) + 1);
        }
        
        return stageCountMap;
    
    }
    
    
    public CaseStage getCaseStage(CasePhase ph) throws CaseLifecyleException {
        CaseStage stage;
        switch (ph) {
            case PrelimInvestigationPending:
                stage = CaseStage.Investigation;
                break;
            case NoticeDelivery:
                stage = CaseStage.Investigation;
                break;
        // Letter marked with a send date
            case InitialComplianceTimeframe:
                stage = CaseStage.Enforcement;
                break;
        // compliance inspection
            case SecondaryComplianceTimeframe:
                stage = CaseStage.Enforcement;
                break;
        // Filing of citation
            case AwaitingHearingDate:
                stage = CaseStage.Citation;
                break;
        // hearing date scheduled
            case HearingPreparation:
                stage = CaseStage.Citation;
                break;
        // hearing not resulting in a case closing
            case InitialPostHearingComplianceTimeframe:
                stage = CaseStage.Citation;
                break;
            case SecondaryPostHearingComplianceTimeframe:
                stage = CaseStage.Citation;
                break;
            case Closed:
                stage = CaseStage.Closed;
                // TODO deal with this later
                //                throw new CaseLifecyleException("Cannot advance a closed case to any other phase");
                break;
            case InactiveHolding:
                stage = CaseStage.Closed;
                break;
            default:
                stage = CaseStage.Closed;
        }
        
        return stage;
        
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
    
    
    
}
