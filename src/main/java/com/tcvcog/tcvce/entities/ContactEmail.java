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
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 *
 * @author sylvia
 */
public  class   ContactEmail 
        extends Contact{
    
    protected int emailid;
    protected String emailaddress;
    protected LocalDateTime bounceTS;

    /**
     * @return the emailid
     */
    public int getEmailid() {
        return emailid;
    }

    /**
     * @return the emailaddress
     */
    public String getEmailaddress() {
        return emailaddress;
    }

    /**
     * @return the bounceTS
     */
    public LocalDateTime getBounceTS() {
        return bounceTS;
    }

    /**
     * @param emailid the emailid to set
     */
    public void setEmailid(int emailid) {
        this.emailid = emailid;
    }

    /**
     * @param emailaddress the emailaddress to set
     */
    public void setEmailaddress(String emailaddress) {
        this.emailaddress = emailaddress;
    }

    /**
     * @param bounceTS the bounceTS to set
     */
    public void setBounceTS(LocalDateTime bounceTS) {
        this.bounceTS = bounceTS;
    }
    
}
