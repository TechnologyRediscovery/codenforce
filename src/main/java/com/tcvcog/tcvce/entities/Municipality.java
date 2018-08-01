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
package com.tcvcog.tcvce.entities;

import java.util.Objects;

/**
 *
 * @author Eric C. Darsow
 */
public class Municipality {
    
    private int muniCode;
    private String muniName;
    private String address_street;
    private String address_city;
    private String address_state;
    private String address_zip;
    private String phone;
    private String fax;
    private String email;
    private String managerName;
    private String managerPhone;
    private int population;
    private boolean activeInProgram;
    private int defaultCodeSetID;

    /**
     * @return the muniCode
     */
    public int getMuniCode() {
        return muniCode;
    }

    /**
     * @param muniCode the muniCode to set
     */
    public void setMuniCode(int muniCode) {
        this.muniCode = muniCode;
    }

    /**
     * @return the muniName
     */
    public String getMuniName() {
        return muniName;
    }

    /**
     * @param muniName the muniName to set
     */
    public void setMuniName(String muniName) {
        this.muniName = muniName;
    }

    /**
     * @return the address_street
     */
    public String getAddress_street() {
        return address_street;
    }

    /**
     * @param address_street the address_street to set
     */
    public void setAddress_street(String address_street) {
        this.address_street = address_street;
    }

    /**
     * @return the address_city
     */
    public String getAddress_city() {
        return address_city;
    }

    /**
     * @param address_city the address_city to set
     */
    public void setAddress_city(String address_city) {
        this.address_city = address_city;
    }

    /**
     * @return the address_state
     */
    public String getAddress_state() {
        return address_state;
    }

    /**
     * @param address_state the address_state to set
     */
    public void setAddress_state(String address_state) {
        this.address_state = address_state;
    }

    /**
     * @return the address_zip
     */
    public String getAddress_zip() {
        return address_zip;
    }

    /**
     * @param address_zip the address_zip to set
     */
    public void setAddress_zip(String address_zip) {
        this.address_zip = address_zip;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the fax
     */
    public String getFax() {
        return fax;
    }

    /**
     * @param fax the fax to set
     */
    public void setFax(String fax) {
        this.fax = fax;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the managerName
     */
    public String getManagerName() {
        return managerName;
    }

    /**
     * @param managerName the managerName to set
     */
    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    /**
     * @return the managerPhone
     */
    public String getManagerPhone() {
        return managerPhone;
    }

    /**
     * @param managerPhone the managerPhone to set
     */
    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }

    /**
     * @return the activeInProgram
     */
    public boolean isActiveInProgram() {
        return activeInProgram;
    }

    /**
     * @param activeInProgram the activeInProgram to set
     */
    public void setActiveInProgram(boolean activeInProgram) {
        this.activeInProgram = activeInProgram;
    }

    /**
     * @return the population
     */
    public int getPopulation() {
        return population;
    }

    /**
     * @param population the population to set
     */
    public void setPopulation(int population) {
        this.population = population;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.muniCode;
        hash = 67 * hash + Objects.hashCode(this.muniName);
        hash = 67 * hash + Objects.hashCode(this.address_street);
        hash = 67 * hash + Objects.hashCode(this.address_city);
        hash = 67 * hash + Objects.hashCode(this.address_state);
        hash = 67 * hash + Objects.hashCode(this.address_zip);
        hash = 67 * hash + Objects.hashCode(this.phone);
        hash = 67 * hash + Objects.hashCode(this.fax);
        hash = 67 * hash + Objects.hashCode(this.email);
        hash = 67 * hash + Objects.hashCode(this.managerName);
        hash = 67 * hash + Objects.hashCode(this.managerPhone);
        hash = 67 * hash + this.population;
        hash = 67 * hash + (this.activeInProgram ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Municipality other = (Municipality) obj;
        if (this.muniCode != other.muniCode) {
            return false;
        }
        if (this.population != other.population) {
            return false;
        }
        if (this.activeInProgram != other.activeInProgram) {
            return false;
        }
        if (!Objects.equals(this.muniName, other.muniName)) {
            return false;
        }
        if (!Objects.equals(this.address_street, other.address_street)) {
            return false;
        }
        if (!Objects.equals(this.address_city, other.address_city)) {
            return false;
        }
        if (!Objects.equals(this.address_state, other.address_state)) {
            return false;
        }
        if (!Objects.equals(this.address_zip, other.address_zip)) {
            return false;
        }
        if (!Objects.equals(this.phone, other.phone)) {
            return false;
        }
        if (!Objects.equals(this.fax, other.fax)) {
            return false;
        }
        if (!Objects.equals(this.email, other.email)) {
            return false;
        }
        if (!Objects.equals(this.managerName, other.managerName)) {
            return false;
        }
        if (!Objects.equals(this.managerPhone, other.managerPhone)) {
            return false;
        }
        return true;
    }

    /**
     * @return the defaultCodeSetID
     */
    public int getDefaultCodeSetID() {
        return defaultCodeSetID;
    }


    /**
     * @param defaultCodeSetID the defaultCodeSetID to set
     */
    public void setDefaultCodeSetID(int defaultCodeSetID) {
        this.defaultCodeSetID = defaultCodeSetID;
    }

    
}
