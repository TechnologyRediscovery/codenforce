package com.tcvcog.tcvce.entities;

import java.util.List;

public interface IFace_PaymentHolder {

    public List<Payment> getPaymentList();

    public void setPaymentList(List<Payment> paymentList);

    public void setPaymentListGeneral(List<Payment> paymentList);

    public List<FeeAssigned> getFeeList();

    public void setFeeList(List<FeeAssigned> feeList);

}
