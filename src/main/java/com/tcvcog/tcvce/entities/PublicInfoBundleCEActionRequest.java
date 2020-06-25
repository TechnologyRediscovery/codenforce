/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @author sylvia
 */
public class PublicInfoBundleCEActionRequest extends PublicInfoBundle implements Serializable {
    
    //************************************************
    //*******Action request case public data********
    //************************************************
    
    private CEActionRequest bundledRequest;
    

    public void setBundledRequest(CEActionRequest input) {
        
        setMuni(input.getMuni());
        setPacc(input.getRequestPublicCC());
        setAddressAssociated(!input.getNotAtAddress());
        if (!input.getNotAtAddress()) {
                setPropertyAddress(input.getRequestProperty().getAddress());
            }
        
        input.setRequestor(new Person()); //TODO: Make bundled person
        
        input.setRequestProperty(new Property()); //TODO: Make bundled property
        input.setMuniCode(0);
        input.setCaseAttachmentUser(new User());
        input.setCogInternalNotes("*****");
        input.setMuniNotes("*****");
        
        bundledRequest = input;
    }

    public CEActionRequest getBundledRequest() {
        return bundledRequest;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + bundledRequest.getRequestID();
    }

}
