/*
 * Copyright (C) 2017 cedba
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

import com.tcvcog.tcvce.money.coordination.MoneyCoordinator;
import com.tcvcog.tcvce.coordinators.*;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.WorkflowIntegrator;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.CourtEntityIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;

import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.money.integration.MoneyIntegrator;

import com.tcvcog.tcvce.integration.LogIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccChecklistIntegrator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener; 
import javax.servlet.annotation.WebListener;

/**
 *
 * @author cedba
 */
@WebListener
public class Initializer implements ServletContextListener{

    /**
     * Creates a new instance of Initializer
     */
    public Initializer() {
        System.out.println("Creating Initializer Bean");
      
    
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.out.println("Intilizer.contextInitialized -- start");

        ServletContext servletContext = event.getServletContext();
//        UserCoordinator userCoordinator = new UserCoordinator();

//        System.out.println("Intilizer.contextInitialized -- creating DB Connection");
//        PostgresConnectionFactory con = new PostgresConnectionFactory();
//        servletContext.setAttribute("dBConnection", con);
        // this setAttribute system is not working as planned.

        //servletContext.setAttribute(Constants.USER_COORDINATOR_SCOPE, userCoordinator);
//        servletContext.setAttribute(Constants.USER_COORDINATOR_KEY, userCoordinator);


        CodeCoordinator codeCoordinator = new CodeCoordinator();
        servletContext.setAttribute("codeCoordinator", codeCoordinator);

        UserCoordinator uc = new UserCoordinator();
        servletContext.setAttribute("userCoordinator", uc);

        CaseCoordinator cc = new CaseCoordinator();
        servletContext.setAttribute("caseCoordinator", cc);

        EventCoordinator ec = new EventCoordinator();
        servletContext.setAttribute("eventCoordinator", ec);

        PropertyCoordinator pc = new PropertyCoordinator();
        servletContext.setAttribute("propertyCoordinator", pc);


        CodeIntegrator codeIntegrator = new CodeIntegrator();
        servletContext.setAttribute("codeIntegrator", codeIntegrator);

        MunicipalityIntegrator munigrator = new MunicipalityIntegrator();
        servletContext.setAttribute("municipalitygrator", munigrator);

        PersonIntegrator personIntegrator = new PersonIntegrator();
        servletContext.setAttribute("personIntegrator", personIntegrator);

        PropertyIntegrator pi = new PropertyIntegrator();
        servletContext.setAttribute("propertyIntegrator", pi);

        CEActionRequestIntegrator ceActionRI = new CEActionRequestIntegrator();
        servletContext.setAttribute("cEActionRequestIntegrator", ceActionRI);

        UserIntegrator ui = new UserIntegrator();
        servletContext.setAttribute("userIntegrator", ui);

        CaseIntegrator ci = new CaseIntegrator();
        servletContext.setAttribute("caseIntegrator", ci);

        EventIntegrator ei = new EventIntegrator();
        servletContext.setAttribute("eventIntegrator", ei);


        CEActionRequestIntegrator ari = new CEActionRequestIntegrator();
        servletContext.setAttribute("ceActionRequestIntegrator", ari);

        PublicInfoCoordinator picor = new PublicInfoCoordinator();
        servletContext.setAttribute("publicInfoCoordinator", picor);

        CourtEntityIntegrator cei = new CourtEntityIntegrator();
        servletContext.setAttribute("courtEntityIntegrator", cei);

        // occupancy "modules"

        OccInspectionIntegrator inspecInt = new OccInspectionIntegrator();
        servletContext.setAttribute("occInspectionIntegrator", inspecInt);

        OccupancyIntegrator occupancyIntegrator = new OccupancyIntegrator();
        servletContext.setAttribute("occupancyIntegrator", occupancyIntegrator);

        OccChecklistIntegrator oci = new OccChecklistIntegrator();
        servletContext.setAttribute("occChecklistIntegrator", oci);

        OccupancyCoordinator occupancyCoordinator = new OccupancyCoordinator();
        servletContext.setAttribute("occupancyCoordinator", occupancyCoordinator);

        OccInspectionCoordinator occInspectionCoordinator = new OccInspectionCoordinator();
        servletContext.setAttribute("occInspectionCoordinator", occInspectionCoordinator);

        MoneyIntegrator pmtInt = new MoneyIntegrator();
        servletContext.setAttribute("moneyIntegrator", pmtInt);

        MoneyCoordinator pmtCrd = new MoneyCoordinator();
        servletContext.setAttribute("moneyCoordinator", pmtCrd);

        SystemIntegrator sysInt = new SystemIntegrator();
        servletContext.setAttribute("systemIntegrator", sysInt);

        LogIntegrator logInt = new LogIntegrator();
        servletContext.setAttribute("logIntegrator", logInt);

        // this is a session-scoped bean stored in the session map
        SearchCoordinator sc = new SearchCoordinator();
        servletContext.setAttribute("searchCoordinator", sc);

        BlobCoordinator blobCoordinator = new BlobCoordinator();
        servletContext.setAttribute("blobCoordinator", blobCoordinator);

        BlobIntegrator blobIntegrator = new BlobIntegrator();
        servletContext.setAttribute("blobIntegrator", blobIntegrator);

        PDFCoordinator pdfCoordinator = new PDFCoordinator();
        servletContext.setAttribute("pdfCoordinator", pdfCoordinator);

        PersonCoordinator persCoor = new PersonCoordinator();
        servletContext.setAttribute("personCoordinator", persCoor);

        SystemCoordinator ssCoor = new SystemCoordinator();
        servletContext.setAttribute("sessionSystemCoordinator", ssCoor);


        DataCoordinator dc = new DataCoordinator();
        servletContext.setAttribute("dataCoordinator", dc);

        WorkflowCoordinator choiceCoord = new WorkflowCoordinator();
        servletContext.setAttribute("workflowCoordinator", choiceCoord);

        WorkflowIntegrator choiceInt = new WorkflowIntegrator();
        servletContext.setAttribute("workflowIntegrator", choiceInt);

        MunicipalityCoordinator mc = new MunicipalityCoordinator();
        servletContext.setAttribute("muniCoordinator", mc);

//        SessionBean sb = new SessionBean();
//        servletContext.setAttribute("sessionBean", sb);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent event){
        
    }
    
}
