package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;

import javax.annotation.PostConstruct;
import java.io.Serializable;

/**
 * The premier backing bean for universal payments panel workflow.
 *
 * @author jurplel
 */
public class PaymentsBB extends BackingBeanUtils implements Serializable {

    private EventDomainEnum pageEventDomain;

    private IFace_PaymentHolder currentPaymentHolder;

    public PaymentsBB() {}


    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
        EventCoordinator ec = getEventCoordinator();

        // Find event holder and setup event list
        updatePaymentHolder();
    }

    /**
     * Grabs the current page event type from the sessionBean and uses that to populate
     * the currentPaymentHolder object with something we can grab events from!
     */
    public void updatePaymentHolder() {
        SessionBean sb = getSessionBean();

        pageEventDomain = sb.getSessEventsPageEventDomainRequest();
        switch (pageEventDomain) {
            case CODE_ENFORCEMENT:
                currentPaymentHolder = sb.getSessCECase();
                break;
            case OCCUPANCY:
                currentPaymentHolder = sb.getSessOccPeriod();
                break;
            case UNIVERSAL:
                System.out.println("PaymentsBB reached universal case in updatePaymentHolder()--do something about this maybe?");
                break;
        }
    }

    // Navigation stuff

    /**
     * Redirect to payments page to edit the specified payment.
     */
    public String editPayment(Payment thisPayment) {
        SessionBean sb = getSessionBean();

        sb.setFeeManagementDomain(EventDomainEnum.CODE_ENFORCEMENT);
        sb.setSessPayment(thisPayment);
        sb.getNavStack().pushCurrentPage();

        return "payments";
    }

    /**
     * Redirect to payments page to edit all of PaymentHolder's payments.
     */
    public String editHolderPayments() {
        SessionBean sb = getSessionBean();

        sb.setFeeManagementDomain(EventDomainEnum.CODE_ENFORCEMENT);
        switch (pageEventDomain) {
            case CODE_ENFORCEMENT:
                CECase ceCase = (CECase) currentPaymentHolder;
                sb.setFeeManagementCeCase(ceCase);
                break;
            case OCCUPANCY:
                OccPeriod occPeriod = (OccPeriod) currentPaymentHolder;
                sb.setFeeManagementOccPeriod(occPeriod);
                break;
            case UNIVERSAL:
                System.out.println("PaymentsBB reached universal case in editCECasePayments()--do something about this maybe?");
                break;
        }
        sb.getNavStack().pushCurrentPage();

        return "payments";
    }

    // Other stuff

    public int getPaymentListSize() {
        int size = 0;
        if (currentPaymentHolder != null && currentPaymentHolder.getPaymentList() != null)
            size = currentPaymentHolder.getPaymentList().size();

        return size;
    }

    // Boring getters and setters

    public IFace_PaymentHolder getCurrentPaymentHolder() {
        return currentPaymentHolder;
    }

    public void setCurrentPaymentHolder(IFace_PaymentHolder currentPaymentHolder) {
        this.currentPaymentHolder = currentPaymentHolder;
    }
}
