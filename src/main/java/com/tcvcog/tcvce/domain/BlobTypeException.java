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
public class BlobTypeException extends BlobException{
    
    public BlobTypeException(){
        super();
        
    }
    
    public BlobTypeException(String message){
        super(message);
    }
    
    public BlobTypeException(Exception e){
        super(e);
    }
    
    public BlobTypeException(String message, Exception e){
        super(message, e);
        
    }
}
