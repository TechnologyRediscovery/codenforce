/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
package com.tcvcog.tcvce.money.coordination;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.money.entities.*;
import com.tcvcog.tcvce.money.integration.MoneyIntegrator;
import java.io.Serializable;
import java.util.List;

/**
 * Implements business logic related to payments.
 *
 * @author Nathan Dietz and SYLVIA and now as of JUNE 2022, Ellen Bascomb
 * of Apartment31Y
 */
public class MoneyCoordinator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of moneyCoordinator
     */
    public MoneyCoordinator() {
    }
    
    
    /**
     * Starting point for creating a new Transaction! Get a skeleton of one
     * with the TransactionDetails object nicely configured for you
     * @param typeEnum
     * @param src
     * @param ua
     * @return 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public Transaction getTransactionSkeleton(TnxTypeEnum typeEnum, TnxSource src, UserAuthorized ua) throws BObStatusException{
        if(typeEnum == null || src == null || ua == null){
            throw new BObStatusException("Cannot create a Transaction Skeleton with null typeEnum or user");
        }
        TransactionDetails trxDet;
        
        
        switch(typeEnum){
            case ADJUSTMENT:
                trxDet = new TransactionAdjustment();
                break;
            case CHARGE:
                trxDet = new TransactionCharge();
                break;
            case PAYMENT:
                trxDet = new TransactionPayment();
                break;
            default:
                throw new BObStatusException("TypeEnum value not supported for transaction skeleton creation");
                
        }
        
        
        
        
    }
    
    
    
    
    
    

    /**
     * Extracts a single Transaction from the DB by ID
     * @param trxid of the Transaction. 0 will return null;
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public Transaction getTransaction(int trxid) throws IntegrationException, BObStatusException{
        
        MoneyIntegrator mi = getMoneyIntegrator();
        
        
        if(trxid == 0){
            return null;
        }
        
        Transaction trx = mi.getTransaction(trxid);
        
        return configureTransaction(trx);
    }
    
    /**
     * Logic block for getting a Transaction all setup--
     * and there's all sorts of tricky class hierarchy stuff
     * to setup and get right
     * @param trx
     * @return 
     */
    private Transaction configureTransaction(Transaction trx) throws BObStatusException, IntegrationException{
        if(trx == null){
            throw new BObStatusException("Cannot configure a null transaction");
        }
        PersonCoordinator pc = getPersonCoordinator();
        trx.setHumanLinkList(pc.getHumanLinkList(trx));
        
        return trx;
        
    }
    
    /**
     * Public entry point for writing in a new transaction. Lots of business rule action happens in here
     * throwing errors with generally helpful messages
     * @param trx
     * @param ua
     * @param lholder
     * @return 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public int insertTransaction(Transaction trx, UserAuthorized ua, IFace_ledgerHolder lholder) throws BObStatusException, IntegrationException{
        if(trx == null || ua == null || lholder == null){
            throw new BObStatusException("Cannot insert transaction with null inputs");
        }
        MoneyIntegrator mi = getMoneyIntegrator();
        
        trx.setCreatedBy(ua);
        trx.setLastUpdatedBy(ua);
        
        auditTransaction(trx);
        return mi.insertTransaction(trx);
        
    }
    
    
    /**
     * Logic rule hub for checking that a transaction doesn't get linked to
     * two different ledgers, that Adjustments are properly addressed, etc. 
     * @param trx 
     */
    private void auditTransaction(Transaction trx) throws BObStatusException{
        if(trx == null){
            throw new BObStatusException("Cannot audit a null transaction");
        }
        if(trx.getCeCaseID() != 0 && trx.getOccPeriodID() != 0){
            throw new BObStatusException("MoneyCoordinator.auditTransaction | FATAL: transactions cannot have non-zero case and period IDs");
        }
        
        if(trx.getTnxDomain() == null){
            throw new BObStatusException("MoneyCoordinator.auditTransaction | FATAL: Transaction cannot have a null domain");
        }
        
        if(trx.getTnxDomain() == DomainEnum.UNIVERSAL){
            throw new BObStatusException("MoneyCoordinator.auditTransaction | FATAL: Transaction cannot have a domain of UNIVERSAL");
        }
        
        if(trx.getCeCaseID() != 0 && trx.getTnxDomain() != DomainEnum.CODE_ENFORCEMENT ){
            throw new BObStatusException("MoneyCoordinator.auditTransaction | FATAL: Transaction has a non-zero case id but is not of domain type: CODE_ENFORCEMENT");
        }
        
        if(trx.getOccPeriodID() != 0 && trx.getTnxDomain() != DomainEnum.OCCUPANCY ){
            throw new BObStatusException("MoneyCoordinator.auditTransaction | FATAL: Transaction has a non-zero period id but is not of domain type: OCCUPANCY");
        }
        
       if(trx.getTnxType() == null){
            throw new BObStatusException("MoneyCoordinator.auditTransaction | FATAL: Transaction cannot have a null transaction type enum");
       }
       
       if(trx.getTnxType() == TnxTypeEnum.CHARGE && !(trx instanceof TransactionCharge)){
            throw new BObStatusException("MoneyCoordinator.auditTransaction | FATAL: Transaction cannot be of type charge and not be a subclass of TransactionCharge");
       }
        
       if(trx.getTnxType() == TnxTypeEnum.PAYMENT && !(trx instanceof TransactionPayment)){
            throw new BObStatusException("MoneyCoordinator.auditTransaction | FATAL: Transaction cannot be of type charge and not be a subclass of TransactionPayment");
       }
       if(trx.getTnxType() == TnxTypeEnum.ADJUSTMENT && !(trx instanceof TransactionAdjustment)){
            throw new BObStatusException("MoneyCoordinator.auditTransaction | FATAL: Transaction cannot be of type charge and not be a subclass of TransactionAdjustment");
       }
        
        
    }
    
    
    public MoneyLedger getMoneyLedger(IFace_ledgerHolder ledgerHolder){
        MoneyLedger ledger = null;
        
        return configureMoneyLedger(ledger);
        
    }
    
    
    private MoneyLedger configureMoneyLedger(MoneyLedger ledger){
        
        
        return ledger;
    }
    
    
    /**
     * Official getter of TransactionSources
     * @param sourceID, 0 yields a null
     * @return the populated object or null if ID == 0
     */
   public TnxSource getTransactionSource(int sourceID) throws IntegrationException{
       MoneyIntegrator mi = getMoneyIntegrator();
       if(sourceID == 0){
           return null;
       }
       
       return mi.getTransactionSource(sourceID);
       
   }
    
    
    
    
    
    
}
