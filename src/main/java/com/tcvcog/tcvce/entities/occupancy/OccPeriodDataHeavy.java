/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.entities.occupancy;

/**
 * The Data Intensive subclass of the OccPeriod tree
 * We want to be able to load info about OccPeriods without having to 
 * initialize and configure each and every event and proposal and rule
 * associated with each occ period on each unit on each property
 * that we browse, so we'll only load the DataHeavy version of this
 * Object if we're actually editing that particular OccPeriod
 * @author sylvia
 */
public class OccPeriodDataHeavy {
    
}
