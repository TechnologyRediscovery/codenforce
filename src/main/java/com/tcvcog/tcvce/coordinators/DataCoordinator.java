/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.CaseStage;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.ViolationStatusEnum;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.model.chart.DonutChartModel;

/**
 *
 * @author sylvia
 */
public class DataCoordinator extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of DataCoordinator
     */
    public DataCoordinator() {
    }
    
    private DonutChartModel violationDonut;

    
    public Map<CasePhase, Integer> getCaseCountsByPhase(List<CECase> caseList) throws IntegrationException {
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
        while (caseIter.hasNext()) {
            CasePhase p = caseIter.next().getCasePhase();
            phaseCountMap.put(p, phaseCountMap.get(p) + 1);
        }
        return phaseCountMap;
    }

    
    public Map<CaseStage, Integer> getCaseCountsByStage(List<CECase> caseList) throws IntegrationException, CaseLifecyleException {
        Map<CaseStage, Integer> stageCountMap = new LinkedHashMap<>();
        List<CaseStage> stageList = Arrays.asList(CaseStage.values());
        CaseCoordinator cc = getCaseCoordinator();
        for (CaseStage cs : stageList) {
            stageCountMap.put(cs, 0);
        }
        for (CECase c : caseList) {
            CaseStage stg = c.getCasePhase().getCaseStage();
            stageCountMap.put(stg, stageCountMap.get(stg) + 1);
        }
        return stageCountMap;
    }
    
    public Map<ViolationStatusEnum, Integer> getViolationCountsByStatus(CECase cse){
        Map<ViolationStatusEnum, Integer> statusCountMap = new LinkedHashMap<>();
        List<ViolationStatusEnum> statusList = Arrays.asList(ViolationStatusEnum.values());
        for (ViolationStatusEnum vs : statusList) {
            statusCountMap.put(vs, 0);
        }
        
        for (CodeViolation cv : cse.getViolationList()) {
            ViolationStatusEnum status = cv.getStatus();
            statusCountMap.put(status, statusCountMap.get(status) + 1);
        }
        return statusCountMap;
        
    }
    
    
    public DonutChartModel generateModelViolationDonut(CECase cse){
        Map<ViolationStatusEnum, Integer> statusCountMap = getViolationCountsByStatus(cse);
        Map<String, Number> chartMap = new LinkedHashMap<>();
        
        List<ViolationStatusEnum> statusList = Arrays.asList(ViolationStatusEnum.values());
        for (ViolationStatusEnum vs : statusList) {
            chartMap.put(vs.getLabel(), statusCountMap.get(vs));
        }
        
        violationDonut = new DonutChartModel();
        violationDonut.addCircle(chartMap);
        
        violationDonut.setTitle("Violations by status");
        violationDonut.setLegendPosition("e");
        
        return violationDonut;
        
    }
    


    
    public Map<String, Number> computeCountsByCEARReason(List<CEActionRequest> reqList){
        CEActionRequest cear;
        Map<String, Number> map = new LinkedHashMap<>();
        for(CEActionRequest req: reqList){
            if(map.containsKey(req.getIssueTypeString())){
                map.put(req.getIssueTypeString(), map.get(req.getIssueTypeString()).intValue() + 1);
            } else {
                map.put(req.getIssueTypeString(), 1);
            }
        }
        return map;
    }
    
    public Map<EnforcableCodeElement, Number> computeViolationFrequency(List<CECase> cseList){
        Map<EnforcableCodeElement, Number> enfCdElMap = new LinkedHashMap<>();
        for(CECase cse: cseList){
            for(CodeViolation cdVl: cse.getViolationList()){
                if(enfCdElMap.containsKey(cdVl.getViolatedEnfElement())){
                    Integer count = ((Integer) enfCdElMap.get(cdVl.getViolatedEnfElement())) + 1;
                    enfCdElMap.put(cdVl.getViolatedEnfElement(), count );
                } else {
                    enfCdElMap.put(cdVl.getViolatedEnfElement(), 1);
                }
            }
        }
        
        return enfCdElMap;
    }
    
    public Map<String, Number> computeViolationFrequencyStringMap(List<CECase> cseList){
        
        Map<EnforcableCodeElement, Number> violationMap = computeViolationFrequency(cseList);
        Map<String, Number> violationStringMap = new LinkedHashMap<>();
        
        for(EnforcableCodeElement enfCdEl: violationMap.keySet()){
            violationStringMap.put(enfCdEl.toString(), violationMap.get(enfCdEl));
        }
        return violationStringMap;
    }
    
}
