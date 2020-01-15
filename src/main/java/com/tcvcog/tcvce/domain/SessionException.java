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
package com.tcvcog.tcvce.domain;

import com.tcvcog.tcvce.util.SubSysEnum;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public  class   SessionException 
        extends BaseException {
    
    
    private SubSysEnum subSys;
    private ExceptionSeverityEnum severity;
    
    
    public SessionException(){
        super();
        
    }
    
    public SessionException(String message){
        super(message);
    }
    
    public SessionException(Exception e){
        super(e);
    }
    
    public SessionException(String message, Exception e){
        super(message, e);
    }
    
    public SessionException(String message, SubSysEnum ss, ExceptionSeverityEnum sev){
        super(message);
        subSys = ss;
        severity = sev;
        
    }
    
    public SessionException(String message, Exception e, SubSysEnum ss, ExceptionSeverityEnum sev){
        super(message, e);
        subSys = ss;
        severity = sev;
        
    }

    /**
     * @return the subSys
     */
    public SubSysEnum getSubSys() {
        return subSys;
    }

    /**
     * @return the severity
     */
    public ExceptionSeverityEnum getSeverity() {
        return severity;
    }

    /**
     * @param subSys the subSys to set
     */
    public void setSubSys(SubSysEnum subSys) {
        this.subSys = subSys;
    }

    /**
     * @param severity the severity to set
     */
    public void setSeverity(ExceptionSeverityEnum severity) {
        this.severity = severity;
    }
    
}
