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

import java.util.List;

/**
 * Specifies a setter and getter for a list of linked humans
 * @author Ellen Bascomb
 */
public interface IFace_humanListHolder {
    
    public List<HumanLink> getHumanLinkList();
    public void setHumanLinkList(List<HumanLink> hll);
    public LinkedObjectSchemaEnum getHUMAN_LINK_SCHEMA_ENUM();
    public int getHostPK();

    
    
}
