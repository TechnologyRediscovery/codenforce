/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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

/**
 * Container for a title, color, and number
 * used alongside pie charts whose legend
 * is inflexible!!
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class LegendItem {
    protected String title;
    protected String colorRGBString;
    protected int value;
    protected double percent;

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the colorRGBString
     */
    public String getColorRGBString() {
        return colorRGBString;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @return the percent
     */
    public double getPercent() {
        return percent;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param colorRGBString the colorRGBString to set
     */
    public void setColorRGBString(String colorRGBString) {
        this.colorRGBString = colorRGBString;
    }

    /**
     * @param value the value to set
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * @param percent the percent to set
     */
    public void setPercent(double percent) {
        this.percent = percent;
    }
    
}
