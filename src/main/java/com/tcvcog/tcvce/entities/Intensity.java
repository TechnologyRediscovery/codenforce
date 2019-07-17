/*
 * Copyright (C) 2019 Nathan Dietz
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

/**
 *
 * @author Nathan Dietz
 */
public class Intensity {
    
    private int classid;
    private String title;
    private int muni_municode;
    private int numericrating;
    private String schemaname;
    private boolean active;
    private int icon_iconid;
    
    public Intensity() {
        
        
    }

    public int getClassid() {
        return classid;
    }

    public void setClassid(int classid) {
        this.classid = classid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMuni_municode() {
        return muni_municode;
    }

    public void setMuni_municode(int muni_municode) {
        this.muni_municode = muni_municode;
    }

    public int getNumericrating() {
        return numericrating;
    }

    public void setNumericrating(int numericrating) {
        this.numericrating = numericrating;
    }

    public String getSchemaname() {
        return schemaname;
    }

    public void setSchemaname(String schemaname) {
        this.schemaname = schemaname;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getIcon_iconid() {
        return icon_iconid;
    }

    public void setIcon_iconid(int icon_iconid) {
        this.icon_iconid = icon_iconid;
    }
    
    
    
}
