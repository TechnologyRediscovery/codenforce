/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.domain;

/**
 *
 * @author noah
 */
public class BlobException extends BaseException{
    
    public BlobException(){
        super();
        
    }
    
    public BlobException(String message){
        super(message);
    }
    
    public BlobException(Exception e){
        super(e);
    }
    
    public BlobException(String message, Exception e){
        super(message, e);
        
    }
}
