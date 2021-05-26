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

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * Attempt at a bean that gets image and pdf blobs: not working 
 * as of 26 May 2021
 * 
 * @author Ellen Bascomb
 */
public class BlobRetrieveBB extends BackingBeanUtils {

    /**
     * Creates a new instance of BlobRetrieveBB
     */
    public BlobRetrieveBB() {
    }
    
     private StreamedContent blob;

    public void setupBlob() {
        blob = DefaultStreamedContent.builder()
                    .contentType("image/jpeg")
                    .stream(() -> {
                        FacesContext context = FacesContext.getCurrentInstance();
                        String userId = context.getExternalContext().getRequestParameterMap().get("user");
                        return this.getClass().getResourceAsStream("user" + userId + ".jpg");
                    })
                    .build();
    }

    public StreamedContent retrieveBlob(BlobLight b) {
            BlobIntegrator bi = getBlobIntegrator();
            BlobCoordinator bc = getBlobCoordinator();
            blob = DefaultStreamedContent.builder()
                    .contentType("application/pdf")
                    .name("file.pdf")
                    .stream(() -> {
                        return new ByteArrayInputStream(bc.getBlob(b).getBytes());
                    })
                    .build();
    
        return blob;
    }
    
}
