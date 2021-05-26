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
package com.tcvcog.tcvce.entities;

/**
 * Represents a type of binary large object, BLOB
 * 
 * @author Ellen Bascomb
 */
public class BlobType {
   private BlobTypeEnum typeEnum;
   private String title;
   private Icon icon;

    /**
     * @return the typeEnum
     */
    public BlobTypeEnum getTypeEnum() {
        return typeEnum;
    }

    /**
     * @param typeEnum the typeEnum to set
     */
    public void setTypeEnum(BlobTypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }
    
    
}
