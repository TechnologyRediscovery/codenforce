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

import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric C. Darsow
 */
public class PersonConnectBB {
    
    private String simpleAjax;
    private String simpleAjax2;
    private String fetched;

    /**
     * Creates a new instance of PersonConnectBB
     */
    public PersonConnectBB() {
    }
    
    public void iCanListenToo(){
        System.out.println("In action listener!!");
        
    }

    /**
     * @return the simpleAjax
     */
    public String getSimpleAjax() {
        return simpleAjax;
    }

    /**
     * @param simpleAjax the simpleAjax to set
     */
    public void setSimpleAjax(String simpleAjax) {
        System.out.println("PersonConnectBB.setSimpleAjax | setValue: " + simpleAjax);
        this.simpleAjax = simpleAjax;
    }

    /**
     * @return the simpleAjax2
     */
    public String getSimpleAjax2() {
        return simpleAjax2;
    }

    /**
     * @param simpleAjax2 the simpleAjax2 to set
     */
    public void setSimpleAjax2(String simpleAjax2) {
        this.simpleAjax2 = simpleAjax2;
    }

    /**
     * @return the fetched
     */
    public String getFetched() {
        System.out.println("PersonConnectBB.getFetched | getValue: " + fetched);
        fetched = simpleAjax + simpleAjax2;
        return fetched;
    }

    /**
     * @param fetched the fetched to set
     */
    public void setFetched(String fetched) {
        System.out.println("PersonConnectBB.setFetched | setValue: " + fetched);
        
        this.fetched = fetched;
    }
    
}
