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

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Represents a type of binary large object, BLOB
 * 
 * @author Ellen Bascomb
 */
public class BlobType implements Serializable, Comparable<BlobType> {
   
   private int typeID;
   private String title;
   private Icon icon;
   private String contentTypeString;
   private boolean browserViewable;
   private String notes;
   private List<String> fileExtensionsPermitted;
   
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

    /**
     * @return the typeID
     */
    public int getTypeID() {
        return typeID;
    }

    /**
     * @return the contentTypeString
     */
    public String getContentTypeString() {
        return contentTypeString;
    }

    /**
     * @return the browserViewable
     */
    public boolean isBrowserViewable() {
        return browserViewable;
    }

    /**
     * @return the fileExtensionsPermitted
     */
    public List<String> getFileExtensionsPermitted() {
        return fileExtensionsPermitted;
    }

    /**
     * @param typeID the typeID to set
     */
    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    /**
     * @param contentTypeString the contentTypeString to set
     */
    public void setContentTypeString(String contentTypeString) {
        this.contentTypeString = contentTypeString;
    }

    /**
     * @param browserViewable the browserViewable to set
     */
    public void setBrowserViewable(boolean browserViewable) {
        this.browserViewable = browserViewable;
    }

    /**
     * @param fileExtensionsPermitted the fileExtensionsPermitted to set
     */
    public void setFileExtensionsPermitted(List<String> fileExtensionsPermitted) {
        this.fileExtensionsPermitted = fileExtensionsPermitted;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public int compareTo(BlobType o) {
        return this.title.compareTo(o.title);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.typeID;
        hash = 97 * hash + Objects.hashCode(this.title);
        hash = 97 * hash + Objects.hashCode(this.icon);
        hash = 97 * hash + Objects.hashCode(this.contentTypeString);
        hash = 97 * hash + (this.browserViewable ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.notes);
        hash = 97 * hash + Objects.hashCode(this.fileExtensionsPermitted);
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
        final BlobType other = (BlobType) obj;
        if (this.typeID != other.typeID) {
            return false;
        }
        return true;
    }
    
    
}
