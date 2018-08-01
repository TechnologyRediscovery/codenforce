/*
 * Copyright (C) 2018 Emily
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
package testing;

import com.tcvcog.tcvce.entities.Municipality;

/**
 *
 * @author Emily
 */
public class RoomType {
    private int roomTypeId;
    private String roomName;
    private Municipality roomTypeMuni;

    /**
     * @return the roomTypeId
     */
    public int getRoomTypeId() {
        return roomTypeId;
    }

    /**
     * @param roomTypeId the roomTypeId to set
     */
    public void setRoomTypeId(int roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    /**
     * @return the roomName
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * @param roomName the roomName to set
     */
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    /**
     * @return the municipalityId
     */
    public Municipality getMunicipalityId() {
        return roomTypeMuni;
    }

    /**
     * @param municipalityId the municipalityId to set
     */
    public void setMunicipalityId(Municipality municipalityId) {
        this.roomTypeMuni = municipalityId;
    }
    
    
}
