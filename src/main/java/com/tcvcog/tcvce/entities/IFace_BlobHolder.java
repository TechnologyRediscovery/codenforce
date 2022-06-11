/*
 * Copyright (C) 2020 Technology Rediscovery LLC.
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

import java.util.List;

/**
 * Interface for any object that has attached blob IDs
 * @author Nathan Dietz & Ellen Bascomb
 */
public interface IFace_BlobHolder {
    
    public void setBlobList(List<BlobLight> bl);
    public List<BlobLight> getBlobList();
    
    public BlobLinkEnum getBlobLinkEnum();
    public int getParentObjectID();
    
    public BlobLinkEnum getBlobUpstreamPoolEnum();
    /**
     * This is the DB primary key for the blobHolder
     * that will give birth to the upstream
     * pool for this BlobHolder
     * @return the database primary key that corresponds
     * to the link table named stored in the
     * BlobLinkEnum entry you get with getBlobUpstreamPoolEnum();
     */
    public int getBlobUpstreamPoolEnumPoolFeederID();
}
