/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CasePhaseEnum;
import com.tcvcog.tcvce.entities.CaseStageEnum;
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

    
    public Map<CasePhaseEnum, Integer> getCaseCountsByPhase(List<CECaseDataHeavy> caseList) throws IntegrationException {
        Map<CasePhaseEnum, Integer> phaseCountMap = null;
        if(caseList != null && !caseList.isEmpty()){

            phaseCountMap = new LinkedHashMap<>();
            phaseCountMap.put(CasePhaseEnum.PrelimInvestigationPending, 0);
            phaseCountMap.put(CasePhaseEnum.IssueNotice, 0);
            phaseCountMap.put(CasePhaseEnum.InsideComplianceWindow, 0);
            phaseCountMap.put(CasePhaseEnum.TimeframeExpiredNotCited, 0);
            phaseCountMap.put(CasePhaseEnum.AwaitingHearingDate, 0);
            phaseCountMap.put(CasePhaseEnum.HearingPreparation, 0);
            phaseCountMap.put(CasePhaseEnum.InsideCourtOrderedComplianceTimeframe, 0);
            phaseCountMap.put(CasePhaseEnum.CourtOrderedComplainceTimeframeExpired, 0);
            phaseCountMap.put(CasePhaseEnum.Closed, 0);
            phaseCountMap.put(CasePhaseEnum.Container, 0);
            phaseCountMap.put(CasePhaseEnum.InactiveHolding, 0);
            //CasePhase[] phaseValuesArray = CasePhaseEnum.values();
            Iterator<CECaseDataHeavy> caseIter = caseList.iterator();
            while (caseIter.hasNext()) {
                CasePhaseEnum p = caseIter.next().getStatusBundle().getPhase();
                phaseCountMap.put(p, phaseCountMap.get(p) + 1);
            }
        }
        return phaseCountMap;
    }

    
    public Map<CaseStageEnum, Integer> getCaseCountsByStage(List<CECaseDataHeavy> caseList) throws IntegrationException, BObStatusException {
        Map<CaseStageEnum, Integer> stageCountMap = null;
        if(caseList != null && !caseList.isEmpty()){

            stageCountMap = new LinkedHashMap<>();
            List<CaseStageEnum> stageList = Arrays.asList(CaseStageEnum.values());
            CaseCoordinator cc = getCaseCoordinator();
            for (CaseStageEnum cs : stageList) {
                stageCountMap.put(cs, 0);
            }
            for (CECaseDataHeavy c : caseList) {
                CaseStageEnum stg = c.getStatusBundle().getPhase().getCaseStage();
                stageCountMap.put(stg, stageCountMap.get(stg) + 1);
            }
        }
        return stageCountMap;
    }
    
    public Map<ViolationStatusEnum, Integer> getViolationCountsByStatus(CECaseDataHeavy cse){
    Map<ViolationStatusEnum, Integer> statusCountMap = null;
    
        if(cse != null){

            statusCountMap = new LinkedHashMap<>();
            List<ViolationStatusEnum> statusList = Arrays.asList(ViolationStatusEnum.values());
            for (ViolationStatusEnum vs : statusList) {
                statusCountMap.put(vs, 0);
            }

            for (CodeViolation cv : cse.getViolationList()) {
                ViolationStatusEnum status = cv.getStatus();
                statusCountMap.put(status, statusCountMap.get(status) + 1);
            }
        }
        return statusCountMap;
        
    }
    
    
    public DonutChartModel generateModelViolationDonut(CECaseDataHeavy cse){
        if(cse != null){

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
        }
        
        return violationDonut;
        
    }
    


    /**
     * Test method for metrics
     * 
     * @param reqList
     * @return 
     */
    public Map<String, Number> computeCountsByCEARReason(List<CEActionRequest> reqList){
        CEActionRequest cear;
        Map<String, Number> map = new LinkedHashMap<>();
//        for(CEActionRequest req: reqList){
//            if(map.containsKey(req.getIssueTypeString())){
//                map.put(req.getIssueTypeString(), map.get(req.getIssueTypeString()).intValue() + 1);
//            } else {
//                map.put(req.getIssueTypeString(), 1);
//            }
//        }
        return map;
    }
    
    public Map<EnforcableCodeElement, Number> computeViolationFrequency(List<CECaseDataHeavy> cseList){
        Map<EnforcableCodeElement, Number> enfCdElMap = null;
        if(cseList != null){

            enfCdElMap = new LinkedHashMap<>();
            for(CECaseDataHeavy cse: cseList){
                for(CodeViolation cdVl: cse.getViolationList()){
                    if(enfCdElMap.containsKey(cdVl.getViolatedEnfElement())){
                        Integer count = ((Integer) enfCdElMap.get(cdVl.getViolatedEnfElement())) + 1;
                        enfCdElMap.put(cdVl.getViolatedEnfElement(), count );
                    } else {
                        enfCdElMap.put(cdVl.getViolatedEnfElement(), 1);
                    }
                }
            }
        }
        
        return enfCdElMap;
    }
    
    public Map<String, Number> computeViolationFrequencyStringMap(List<CECaseDataHeavy> cseList){
        Map<EnforcableCodeElement, Number> violationMap = null;
        Map<String, Number> violationStringMap = null;
        if(cseList != null){

            violationMap = computeViolationFrequency(cseList);
            violationStringMap = new LinkedHashMap<>();

            for(EnforcableCodeElement enfCdEl: violationMap.keySet()){
                violationStringMap.put(enfCdEl.toString(), violationMap.get(enfCdEl));
            }
        }
        return violationStringMap;
    }
    
}
