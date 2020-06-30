/*
 * Copyright (C) 2018 Adam Gutonski
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
 *git 
* You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.occupancy.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.Fee;
import com.tcvcog.tcvce.entities.FeeAssigned;
import com.tcvcog.tcvce.entities.MoneyCECaseFeeAssigned;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.PaymentType;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Adam Gutonski
 */
public class PaymentIntegrator extends BackingBeanUtils implements Serializable {

    public PaymentIntegrator() {

    }

    public FeeAssigned getFeeAssigned(int feeID, EventDomainEnum selectedDomain) throws IntegrationException {

        String query = "";
        FeeAssigned skeleton = new FeeAssigned();
        switch (selectedDomain) {

            case OCCUPANCY:
                query = "SELECT * FROM moneyoccperiodfeeassigned WHERE moneyoccperassignedfeeid = ?;";
                break;

            case CODE_ENFORCEMENT:
                query = "SELECT * FROM moneycecasefeeassigned WHERE cecaseassignedfeeid = ?;";
                break;

            default:
                throw new IntegrationException("A domain must be specified to retrieve an assigned fee by ID. UNIVERSAL is not acceptable.");

        }
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, feeID);
            rs = stmt.executeQuery();
            while (rs.next()) {

                switch (selectedDomain) {

                    case OCCUPANCY:
                        skeleton = generateOccPeriodFeeAssigned(rs);
                        break;

                    case CODE_ENFORCEMENT:
                        skeleton = generateCECaseFeeAssigned(rs);
                        break;

                    default:
                        throw new IntegrationException("A domain must be specified to retrieve an assigned fee by ID. UNIVERSAL is not acceptable.");

                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PaymentIntegrator.getFeeAssigned | Unable to retrieve fees according to feeAssigned ID", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        } // close finally

        return skeleton;
    }

    public List<MoneyOccPeriodFeeAssigned> getFeeAssigned(OccPeriod period) throws IntegrationException {

        List<MoneyOccPeriodFeeAssigned> assignedFees = new ArrayList<>();

        String query = "SELECT * FROM moneyoccperiodfeeassigned WHERE occperiod_periodid = ?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, period.getPeriodID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                assignedFees.add(generateOccPeriodFeeAssigned(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PaymentIntegrator.getFeeAssigned | Unable to retrieve fees assigned to OccPeriod", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        } // close finally

        return assignedFees;
    }

    public List<MoneyCECaseFeeAssigned> getFeeAssigned(CECase cse) throws IntegrationException {

        List<MoneyCECaseFeeAssigned> assignedFees = new ArrayList<>();

        String query = "SELECT * FROM moneycecasefeeassigned WHERE cecase_caseid = ?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cse.getCaseID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                assignedFees.add(generateCECaseFeeAssigned(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PaymentIntegrator.getFeeAssigned | Unable to retrieve fees assigned to CECase", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        } // close finally

        return assignedFees;

    }

    public Fee getFee(int feeID) throws IntegrationException {
        Fee skeleton = new Fee();

        String query = "SELECT feeid, muni_municode, feename, feeamount, effectivedate, expirydate, notes\n"
                + "FROM moneyfee\n"
                + "WHERE feeid = ?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, feeID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                skeleton = generateFee(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PaymentIntegrator.getFee | Unable to retrieve fee of ID" + feeID, ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        } // close finally

        return skeleton;
    }

    public List<Fee> getFeeTypeList(Municipality muni) throws IntegrationException {

        List<Fee> feeList = new ArrayList<>();

        String query = "SELECT feeid, muni_municode, feename, feeamount, effectivedate, expirydate, notes\n"
                + "FROM moneyfee\n"
                + "WHERE muni_municode = ?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, muni.getMuniCode());
            rs = stmt.executeQuery();
            while (rs.next()) {
                feeList.add(generateFee(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PaymentIntegrator.getFeeTypeList| Unable to retrieve fees associated with muni", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        } // close finally

        return feeList;
    }

    public List<Fee> getFeeList(OccPeriodType type) throws IntegrationException {

        List<Fee> feeList = new ArrayList<>();

        String query = "SELECT moneyfee.feeid, muni_municode, feename, feeamount, effectivedate, expirydate, notes, autoassign\n"
                + "FROM moneyfee, moneyoccperiodtypefee\n"
                + "WHERE moneyfee.feeid = moneyoccperiodtypefee.fee_feeid AND moneyoccperiodtypefee.occperiodtype_typeid = ? AND moneyoccperiodtypefee.active = true;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, type.getTypeID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                feeList.add(generateFee(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PaymentIntegrator.getFeeAssigned | Unable to retrieve fees associated with occPeriodType", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        } // close finally

        return feeList;
    }

    public List<Fee> getFeeList(EnforcableCodeElement element) throws IntegrationException {

        List<Fee> feeList = new ArrayList<>();

        String query = "SELECT moneyfee.feeid, muni_municode, feename, feeamount, effectivedate, expirydate, notes, autoassign\n"
                + "FROM moneyfee, moneycodesetelementfee\n"
                + "WHERE moneyfee.feeid = moneycodesetelementfee.fee_feeid AND moneycodesetelementfee.codesetelement_elementid = ? AND active = true;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, element.getCodeSetElementID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                feeList.add(generateFee(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PaymentIntegrator.getFeeAssigned | Unable to retrieve fees associated with EnforcableCodeElement", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        } // close finally

        return feeList;
    }

    public void insertOccPeriodFee(MoneyOccPeriodFeeAssigned fee) throws IntegrationException {
        String query = "INSERT INTO public.moneyoccperiodfeeassigned(\n"
                + "    moneyoccperassignedfeeid, moneyfeeassigned_assignedid, occperiod_periodid,"
                + "    assignedby_userid, assignedbyts, waivedby_userid, lastmodifiedts, reduceby,"
                + "    reduceby_userid, notes, fee_feeid, occperiodtype_typeid)\n"
                + "    VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?::numeric::money, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, fee.getMoneyFeeAssigned());
            stmt.setInt(2, fee.getOccPeriodID());
            stmt.setInt(3, fee.getAssignedBy().getUserID());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(fee.getAssigned()));

            if (fee.getWaivedBy().getUserID() != 0) {
                stmt.setInt(5, fee.getWaivedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }

            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(fee.getLastModified()));
            stmt.setDouble(7, fee.getReducedBy());
            if (fee.getReducedByUser().getUserID() != 0) {
                stmt.setInt(8, fee.getReducedByUser().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }

            stmt.setString(9, fee.getNotes());
            stmt.setInt(10, fee.getFee().getOccupancyInspectionFeeID());
            stmt.setInt(11, fee.getOccPeriodTypeID());
            System.out.println("PaymentTypeIntegrator.insertOccPeriodFee | sql: " + stmt.toString());
            System.out.println("TRYING TO EXECUTE INSERT METHOD");
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert OccPeriodFee", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally

    }

    public void insertCECaseFee(MoneyCECaseFeeAssigned fee) throws IntegrationException {
        String query = "INSERT INTO public.moneycecasefeeassigned(\n"
                + "    cecaseassignedfeeid, moneyfeeassigned_assignedid, cecase_caseid,"
                + "    assignedby_userid, assignedbyts, waivedby_userid, lastmodifiedts, reduceby,"
                + "    reduceby_userid, notes, fee_feeid, codesetelement_elementid)\n"
                + "    VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?::numeric::money, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, fee.getMoneyFeeAssigned());
            stmt.setInt(2, fee.getCaseID());
            stmt.setInt(3, fee.getAssignedBy().getUserID());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(fee.getAssigned()));

            if (fee.getWaivedBy().getUserID() != 0) {
                stmt.setInt(5, fee.getWaivedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(fee.getLastModified()));
            stmt.setDouble(7, fee.getReducedBy());
            if (fee.getReducedByUser().getUserID() != 0) {
                stmt.setInt(8, fee.getReducedByUser().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            stmt.setString(9, fee.getNotes());
            stmt.setInt(10, fee.getFee().getOccupancyInspectionFeeID());
            stmt.setInt(11, fee.getCodeSetElement());
            System.out.println("PaymentTypeIntegrator.insertCECaseFee | sql: " + stmt.toString());
            System.out.println("TRYING TO EXECUTE INSERT METHOD");
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert CECaseFee", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally

    }

    public void updateOccPeriodFee(MoneyOccPeriodFeeAssigned fee) throws IntegrationException {
        String query = "UPDATE public.moneyoccperiodfeeassigned\n"
                + "SET moneyfeeassigned_assignedid=?, occperiod_periodid=?, assignedby_userid=?,\n"
                + "    assignedbyts=?, waivedby_userid=?, lastmodifiedts=?, reduceby=(?::numeric::money), reduceby_userid=?,\n"
                + "    notes=?, fee_feeid=?, occperiodtype_typeid=?\n"
                + "WHERE moneyoccperassignedfeeid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, fee.getMoneyFeeAssigned());
            stmt.setInt(2, fee.getOccPeriodID());
            stmt.setInt(3, fee.getAssignedBy().getUserID());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(fee.getAssigned()));
            if (fee.getWaivedBy() != null) {
                stmt.setInt(5, fee.getWaivedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(fee.getLastModified()));
            stmt.setDouble(7, fee.getReducedBy());
            if (fee.getReducedByUser() != null) {
                stmt.setInt(8, fee.getReducedByUser().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            stmt.setString(9, fee.getNotes());
            stmt.setInt(10, fee.getFee().getOccupancyInspectionFeeID());
            stmt.setInt(11, fee.getOccPeriodTypeID());
            stmt.setInt(12, fee.getOccPerAssignedFeeID());
            System.out.println("PaymentTypeIntegrator.updateOccPeriodFee | sql: " + stmt.toString());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot update OccPeriodFee", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally

    }

    public void updateCECaseFee(MoneyCECaseFeeAssigned fee) throws IntegrationException {
        String query = "UPDATE public.moneycecasefeeassigned\n"
                + "SET moneyfeeassigned_assignedid=?, cecase_caseid=?, assignedby_userid=?,\n"
                + "    assignedbyts=?, waivedby_userid=?, lastmodifiedts=?, reduceby=(?::numeric::money), reduceby_userid=?,\n"
                + "    notes=?, fee_feeid=?, codesetelement_elementid=?\n"
                + "WHERE cecaseassignedfeeid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, fee.getMoneyFeeAssigned());
            stmt.setInt(2, fee.getCaseID());
            stmt.setInt(3, fee.getAssignedBy().getUserID());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(fee.getAssigned()));
            if (fee.getWaivedBy() != null) {
                stmt.setInt(5, fee.getWaivedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(fee.getLastModified()));
            stmt.setDouble(7, fee.getReducedBy());
            if (fee.getReducedByUser() != null) {
                stmt.setInt(8, fee.getReducedByUser().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            stmt.setString(9, fee.getNotes());
            stmt.setInt(10, fee.getFee().getOccupancyInspectionFeeID());
            stmt.setInt(11, fee.getCodeSetElement());
            stmt.setInt(12, fee.getCeCaseAssignedFeeID());
            System.out.println("PaymentTypeIntegrator.updateOccPeriodFee | sql: " + stmt.toString());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot update OccPeriodFee", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally

    }

    public FeeAssigned generateFeeAssigned(ResultSet rs) throws IntegrationException {

        UserIntegrator ui = getUserIntegrator();

        FeeAssigned fee = new FeeAssigned();

        try {
            fee.setMoneyFeeAssigned(rs.getInt("moneyfeeassigned_assignedid"));
            fee.setAssignedBy(ui.getUser(rs.getInt("assignedby_userid")));
            fee.setAssigned(rs.getTimestamp("assignedbyts").toLocalDateTime());
            fee.setWaivedBy(ui.getUser(rs.getInt("waivedby_userid")));
            fee.setLastModified(rs.getTimestamp("lastmodifiedts").toLocalDateTime());
            fee.setReducedBy(rs.getDouble("reduceby"));
            fee.setReducedByUser(ui.getUser(rs.getInt("reduceby_userid")));
            fee.setNotes(rs.getString("notes"));
            fee.setFee(getFee(rs.getInt("fee_feeid")));
            fee.setMoneyFeeAssigned(rs.getInt("moneyfeeassigned_assignedid"));

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating OccPeriodFeeAssigned from ResultSet", ex);
        }

        return fee;

    }

    public MoneyOccPeriodFeeAssigned generateOccPeriodFeeAssigned(ResultSet rs) throws IntegrationException {

        UserIntegrator ui = getUserIntegrator();

        MoneyOccPeriodFeeAssigned fee = new MoneyOccPeriodFeeAssigned();

        try {

            fee = new MoneyOccPeriodFeeAssigned(generateFeeAssigned(rs));

            fee.setAssignedFeeID(rs.getInt("moneyoccperassignedfeeid"));
            fee.setOccPerAssignedFeeID(rs.getInt("moneyoccperassignedfeeid"));
            fee.setOccPeriodTypeID(rs.getInt("occperiodtype_typeid"));
            fee.setPaymentList(getPaymentList(fee));
            fee.setDomain(EventDomainEnum.OCCUPANCY);
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating OccPeriodFeeAssigned from ResultSet", ex);
        }

        return fee;

    }

    public MoneyCECaseFeeAssigned generateCECaseFeeAssigned(ResultSet rs) throws IntegrationException {

        UserIntegrator ui = getUserIntegrator();

        MoneyCECaseFeeAssigned fee = new MoneyCECaseFeeAssigned();

        try {

            fee = new MoneyCECaseFeeAssigned(generateFeeAssigned(rs));

            fee.setCeCaseAssignedFeeID(rs.getInt("cecaseassignedfeeid"));
            fee.setAssignedFeeID(rs.getInt("cecaseassignedfeeid"));
            fee.setCaseID(rs.getInt("cecase_caseid"));
            fee.setCodeSetElement(rs.getInt("codesetelement_elementid"));
            fee.setPaymentList(getPaymentList(fee));
            fee.setDomain(EventDomainEnum.CODE_ENFORCEMENT);

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating CECaseFeeAssigned from ResultSet", ex);
        }

        return fee;

    }

    public Fee generateFee(ResultSet rs) throws IntegrationException {

        MunicipalityIntegrator mi = getMunicipalityIntegrator();

        Fee fee = new Fee();

        try {
            fee.setOccupancyInspectionFeeID(rs.getInt("feeid"));
            fee.setMuni(mi.getMuni(rs.getInt("muni_municode")));
            fee.setName(rs.getString("feename"));
            fee.setAmount(rs.getDouble("feeamount"));
            fee.setEffectiveDate(rs.getTimestamp("effectivedate").toLocalDateTime());
            fee.setExpiryDate(rs.getTimestamp("expirydate").toLocalDateTime());
            fee.setNotes(rs.getString("notes"));
            if (hasColumn(rs, "autoassign")) {
                fee.setAutoAssigned(rs.getBoolean("autoassign"));
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating Fee from ResultSet", ex);
        }

        return fee;

    }

    public void updatePayment(Payment payment) throws IntegrationException {
        String query = "UPDATE public.moneypayment\n"
                + "   SET paymenttype_typeid=?, \n"
                + "       datereceived=?, datedeposited=?, amount=(?::numeric::money), payer_personid=?, referencenum=?, \n"
                + "       checkno=?, cleared=?, notes=?\n"
                + " WHERE paymentid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, payment.getPaymentType().getPaymentTypeId());

            if (payment.getDateReceived() != null) {
                stmt.setTimestamp(2, java.sql.Timestamp.valueOf(payment.getDateReceived()));

            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            //update date deposited
            if (payment.getDateDeposited() != null) {
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(payment.getDateDeposited()));

            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }

            stmt.setBigDecimal(4, BigDecimal.valueOf(payment.getAmount()));
            stmt.setInt(5, payment.getPayer().getPersonID());
            stmt.setString(6, payment.getReferenceNum());
            stmt.setInt(7, payment.getCheckNum());
            stmt.setBoolean(8, payment.isCleared());
            stmt.setString(9, payment.getNotes());
            stmt.setInt(10, payment.getPaymentID());
            System.out.println("PaymentIntegrator.updatePayment | sql: " + stmt.toString());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update payment", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        }

    }

    /**
     * Uses the ID num of a payment to get the full payment.
     *
     * @param paymentID
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public Payment getPayment(int paymentID) throws IntegrationException {

        String query = "SELECT paymentid, paymenttype_typeid, datereceived,\n"
                + "datedeposited, amount, payer_personid, referencenum, checkno, cleared, notes,\n"
                + "recordedby_userid, entrytimestamp\n"
                + "FROM public.moneypayment\n"
                + "WHERE paymentid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Payment skeleton = new Payment();
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, paymentID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                skeleton = generatePayment(rs, EventDomainEnum.UNIVERSAL); //The domain will need to be set by whoever calls this method.
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get Payment from ID", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        }
        return skeleton;
    }

    /**
     * Extracts the ID of the given OccPerioda and uses this to grab all
     * relevant payments from the db associated with this Occperiod
     *
     * @param period
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<Payment> getPaymentList(OccPeriod period) throws IntegrationException {

        String query = "SELECT paymentid, paymenttype_typeid, datereceived,\n"
                + "datedeposited, amount, payer_personid, referencenum, checkno, cleared, moneypayment.notes,\n"
                + "recordedby_userid, entrytimestamp\n"
                + "FROM moneyoccperiodfeeassigned, moneyoccperiodfeepayment, public.moneypayment\n"
                + "WHERE occperiod_periodid = ? AND moneyoccperassignedfeeid = occperiodassignedfee_id AND payment_paymentid = paymentid;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<Payment> paymentList = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, period.getPeriodID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                paymentList.add(generatePayment(rs, EventDomainEnum.OCCUPANCY));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get Payment List", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        }
        return paymentList;
    }

    public List<Payment> getPaymentList(MoneyOccPeriodFeeAssigned fee) throws IntegrationException {

        String query = "SELECT paymentid, paymenttype_typeid, datereceived,\n"
                + "datedeposited, amount, payer_personid, referencenum, checkno, cleared, moneypayment.notes,\n"
                + "recordedby_userid, entrytimestamp\n"
                + "FROM moneyoccperiodfeepayment, public.moneypayment\n"
                + "WHERE occperiodassignedfee_id = ? AND payment_paymentid = paymentid;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<Payment> paymentList = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, fee.getOccPerAssignedFeeID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                Payment p = generatePayment(rs, EventDomainEnum.OCCUPANCY);
                p.setAssignedFeeID(fee.getOccPerAssignedFeeID());
                p.setDomain(EventDomainEnum.OCCUPANCY);
                paymentList.add(p);

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get Payment List", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        }
        return paymentList;
    }

    /**
     * Extracts the ID of the given CECase and uses this to grab all relevant
     * payments from the db associated with this CECase
     *
     * @param cse
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<Payment> getPaymentList(CECase cse) throws IntegrationException {

        String query = "SELECT paymentid, paymenttype_typeid, datereceived, datedeposited, \n"
                + "amount, payer_personid, referencenum, checkno, cleared,\n"
                + "moneypayment.notes, recordedby_userid, entrytimestamp\n"
                + "FROM moneycecasefeeassigned, moneycecasefeepayment, public.moneypayment\n"
                + "WHERE cecase_caseid = ? AND cecaseassignedfeeid = cecaseassignedfee_id AND payment_paymentid = paymentid;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<Payment> paymentList = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cse.getCaseID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                paymentList.add(generatePayment(rs, EventDomainEnum.CODE_ENFORCEMENT));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get Payment List", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        }
        return paymentList;
    }

    public List<Payment> getPaymentList(MoneyCECaseFeeAssigned fee) throws IntegrationException {

        String query = "SELECT paymentid, paymenttype_typeid, datereceived,\n"
                + "datedeposited, amount, payer_personid, referencenum, checkno, cleared, moneypayment.notes,\n"
                + "recordedby_userid, entrytimestamp\n"
                + "FROM moneycecasefeepayment, public.moneypayment\n"
                + "WHERE cecaseassignedfee_id = ? AND payment_paymentid = paymentid;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<Payment> paymentList = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, fee.getCeCaseAssignedFeeID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                Payment p = generatePayment(rs, EventDomainEnum.CODE_ENFORCEMENT);
                p.setAssignedFeeID(fee.getCeCaseAssignedFeeID());

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get Payment List", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        }
        return paymentList;
    }

    public ArrayList<Payment> getPaymentList() throws IntegrationException {
        String query = "SELECT paymentid, paymenttype_typeid, datereceived, \n"
                + "       datedeposited, amount, payer_personid, referencenum, checkno, cleared, notes,\n"
                + "recordedby_userid, entrytimestamp\n"
                + "  FROM public.moneypayment;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<Payment> paymentList = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            System.out.println("PaymentIntegrator.getPaymentList | SQL: " + stmt.toString());
            while (rs.next()) {
                paymentList.add(generatePayment(rs, null));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get Payment List", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        }
        return paymentList;
    }

    public Payment getMostRecentPayment() throws IntegrationException {
        String query = "SELECT paymentid, paymenttype_typeid, datereceived,\n"
                + "datedeposited, amount, payer_personid, referencenum, checkno, cleared, notes,\n"
                + "recordedby_userid, entrytimestamp\n"
                + "FROM moneypayment\n"
                + "WHERE paymentid = (SELECT MAX(paymentid) from moneypayment);";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Payment skeleton = new Payment();

        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            System.out.println("PaymentIntegrator.getMostRecentPayment | SQL: " + stmt.toString());
            while (rs.next()) {
                skeleton = generatePayment(rs, null);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get most recent payment", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        }
        return skeleton;
    }

    public void insertPayment(Payment payment) throws IntegrationException {
        String query = "INSERT INTO public.moneypayment(\n"
                + " paymentid, paymenttype_typeid, datereceived, \n"
                + " datedeposited, amount, payer_personid, referencenum, checkno, cleared, notes, \n"
                + " recordedby_userid, entrytimestamp)\n"
                + "    VALUES (DEFAULT, ?, ?, \n"
                + "            ?, ?::numeric::money, ?, ?, ?, DEFAULT, ?, ?, NOW());";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, payment.getPaymentType().getPaymentTypeId());
            if (payment.getDateReceived() != null) {
                stmt.setTimestamp(2, java.sql.Timestamp.valueOf(payment.getDateReceived()));

            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            if (payment.getDateDeposited() != null) {
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(payment.getDateDeposited()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            stmt.setBigDecimal(4, BigDecimal.valueOf(payment.getAmount()));
            stmt.setInt(5, payment.getPayer().getPersonID());
            stmt.setString(6, payment.getReferenceNum());
            stmt.setInt(7, payment.getCheckNum());
            stmt.setString(8, payment.getNotes());
            stmt.setInt(9, payment.getRecordedBy().getUserID());
            System.out.println("PaymentIntegrator.insertPayment | sql: " + stmt.toString());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert payment", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally

    }

    public void insertFeePeriodTypeJoin(Fee fee, OccPeriodType type) throws IntegrationException {
        String query = "INSERT INTO public.moneyoccperiodtypefee(\n"
                + "    fee_feeid, occperiodtype_typeid, autoassign, active)\n"
                + "    VALUES (?, ?, ?, true);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, fee.getOccupancyInspectionFeeID());
            stmt.setInt(2, type.getTypeID());
            stmt.setBoolean(3, fee.isAutoAssigned());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PaymentIntegrator.insertFeePeriodTypeJoin | Error: ", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    public void deactivateFeePeriodTypeJoin(Fee fee, OccPeriodType type) throws IntegrationException {

        String query = "UPDATE public.moneyoccperiodtypefee\n"
                + "   SET active=false\n"
                + "   WHERE fee_feeid=? AND occperiodtype_typeid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, fee.getOccupancyInspectionFeeID());
            stmt.setInt(2, type.getTypeID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot Fee Occ Period Type join", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    public void reactivateFeePeriodTypeJoin(Fee fee, OccPeriodType type) throws IntegrationException {

        String query = "UPDATE public.moneyoccperiodtypefee\n"
                + "   SET active=true\n"
                + "   WHERE fee_feeid=? AND occperiodtype_typeid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, fee.getOccupancyInspectionFeeID());
            stmt.setInt(2, type.getTypeID());
            System.out.println("TRYING TO EXECUTE UPDATE METHOD");
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to reactivate fee and occperiod join", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        }

    }

    public void updateFeePeriodTypeJoin(Fee fee, OccPeriodType type) throws IntegrationException {

        String query = "UPDATE public.moneyoccperiodtypefee\n"
                + "   SET autoassign=?\n"
                + "   WHERE fee_feeid=? AND occperiodtype_typeid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setBoolean(1, fee.isAutoAssigned());
            stmt.setInt(2, fee.getOccupancyInspectionFeeID());
            stmt.setInt(3, type.getTypeID());
            System.out.println("TRYING TO EXECUTE UPDATE METHOD");
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update fee and occperiod join", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        }

    }

    public void insertFeeCodeElementJoin(Fee fee, EnforcableCodeElement element) throws IntegrationException {
        String query = "INSERT INTO public.moneycodesetelementfee(\n"
                + "    fee_feeid, codesetelement_elementid, autoassign, active)\n"
                + "    VALUES (?, ?, ?, true);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, fee.getOccupancyInspectionFeeID());
            stmt.setInt(2, element.getCodeSetElementID());
            stmt.setBoolean(3, fee.isAutoAssigned());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PaymentIntegrator.insertFeeCodeElementJoin | Error: ", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    public void deactivateFeeCodeElementJoin(Fee fee, EnforcableCodeElement element) throws IntegrationException {

        String query = "UPDATE public.moneycodesetelementfee\n"
                + "   SET active=false\n"
                + "   WHERE fee_feeid=? AND codesetelement_elementid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, fee.getOccupancyInspectionFeeID());
            stmt.setInt(2, element.getCodeSetElementID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot Fee Code Set Element join", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    public void reactivateFeeCodeElementJoin(Fee fee, EnforcableCodeElement element) throws IntegrationException {

        String query = "UPDATE public.moneycodesetelementfee\n"
                + "   SET active=true\n"
                + "   WHERE fee_feeid=? AND codesetelement_elementid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, fee.getOccupancyInspectionFeeID());
            stmt.setInt(2, element.getCodeSetElementID());
            System.out.println("TRYING TO EXECUTE UPDATE METHOD");
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to reactivate fee and Code Set Element join", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        }

    }

    public void updateFeeCodeElementJoin(Fee fee, EnforcableCodeElement element) throws IntegrationException {

        String query = "UPDATE public.moneycodesetelementfee\n"
                + "   SET autoassign=?\n"
                + "   WHERE fee_feeid=? AND codesetelement_elementid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setBoolean(1, fee.isAutoAssigned());
            stmt.setInt(2, fee.getOccupancyInspectionFeeID());
            stmt.setInt(3, element.getCodeSetElementID());
            System.out.println("TRYING TO EXECUTE UPDATE METHOD");
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update fee and code set element join", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        }

    }

    public void insertPaymentPeriodJoin(Payment payment, MoneyOccPeriodFeeAssigned fee) throws IntegrationException {
        String query = "INSERT INTO public.moneyoccperiodfeepayment(\n"
                + "    payment_paymentid, occperiodassignedfee_id)\n"
                + "    VALUES (?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, payment.getPaymentID());
            stmt.setInt(2, fee.getOccPerAssignedFeeID());
            System.out.println("PaymentIntegrator.insertPaymentPeriodJoin | sql: " + stmt.toString());
            System.out.println("TRYING TO EXECUTE INSERT METHOD");
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Payment-Occ Period join", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally

    }

    public void insertPaymentCaseJoin(Payment payment, MoneyCECaseFeeAssigned fee) throws IntegrationException {
        String query = "INSERT INTO public.moneycecasefeepayment(\n"
                + "    payment_paymentid, cecaseassignedfee_id)\n"
                + "    VALUES (?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, payment.getPaymentID());
            stmt.setInt(2, fee.getCeCaseAssignedFeeID());
            System.out.println("PaymentIntegrator.insertPaymentCaseJoin | sql: " + stmt.toString());
            System.out.println("TRYING TO EXECUTE INSERT METHOD");
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Payment-CE Case join", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally

    }

    //TODO: change this to deactivate payment records instead of deleting them.
    public void deletePayment(Payment payment) throws IntegrationException {
        String query = "DELETE FROM public.moneypayment\n"
                + " WHERE paymentid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, payment.getPaymentID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete payment record--probably because another"
                    + "part of the database has a reference item.", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    private Payment generatePayment(ResultSet rs, EventDomainEnum domain) throws IntegrationException {
        Payment newPayment = new Payment();

        PersonIntegrator pi = getPersonIntegrator();

        UserIntegrator ui = getUserIntegrator();

        try {
            newPayment.setPaymentID(rs.getInt("paymentid"));
            newPayment.setPaymentType(getPaymentTypeFromPaymentTypeID(rs.getInt("paymenttype_typeid")));
            java.sql.Timestamp dateReceived = rs.getTimestamp("datereceived");
            //for received date
            if (dateReceived != null) {
                newPayment.setDateReceived(dateReceived.toLocalDateTime());
            } else {
                newPayment.setDateReceived(null);
            }
            java.sql.Timestamp dateDeposited = rs.getTimestamp("datedeposited");
            //for deposited date
            if (dateDeposited != null) {
                newPayment.setDateDeposited(dateDeposited.toLocalDateTime());
            } else {
                newPayment.setDateDeposited(null);
            }
            java.sql.Timestamp entryTimestamp = rs.getTimestamp("entrytimestamp");
            //for deposited date
            if (entryTimestamp != null) {
                newPayment.setEntryTimestamp(entryTimestamp.toLocalDateTime());
            } else {
                newPayment.setEntryTimestamp(null);
            }
            newPayment.setAmount(rs.getDouble("amount"));
            newPayment.setPayer(pi.getPerson(rs.getInt("payer_personid")));
            newPayment.setReferenceNum(rs.getString("referencenum"));
            newPayment.setCheckNum(rs.getInt("checkno"));
            newPayment.setCleared(rs.getBoolean("cleared"));
            newPayment.setNotes(rs.getString("notes"));
            newPayment.setRecordedBy(ui.getUser(rs.getInt("recordedby_userid")));
            newPayment.setDomain(domain);

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating Payment from result set", ex);
        }
        return newPayment;
    }

    public PaymentType getPaymentTypeFromPaymentTypeID(int paymentTypeID) throws IntegrationException {
        PaymentType paymentType = new PaymentType();
        PreparedStatement stmt = null;
        Connection con = null;
        // note that paymentTypeID is not returned in this query since it is specified in the WHERE
        String query = "SELECT typeid, pmttypetitle \n"
                + "  FROM public.moneypaymenttype"
                + " WHERE typeid = ?;";
        ResultSet rs = null;

        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            stmt.setInt(1, paymentTypeID);
            //System.out.println("MunicipalityIntegrator.getMuniFromMuniCode | query: " + stmt.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                paymentType.setPaymentTypeId(rs.getInt("typeid"));
                paymentType.setPaymentTypeTitle(rs.getString("pmttypetitle"));

            }
        } catch (SQLException ex) {
            System.out.println("PaymentTypeIntegrator.getPaymentTypeFromPaymentTypeID | " + ex.toString());
            throw new IntegrationException("Exception in PaymentTypeIntegrator.getPaymentTypeFromPaymentTypeID", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {/* ignored */ }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally

        return paymentType;

    }

    public void updatePaymentType(PaymentType paymentType) throws IntegrationException {
        String query = "UPDATE public.moneypaymenttype\n"
                + "   SET pmttypetitle=?\n"
                + "   WHERE typeid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, paymentType.getPaymentTypeTitle());
            stmt.setInt(2, paymentType.getPaymentTypeId());
            System.out.println("TRYING TO EXECUTE UPDATE METHOD");
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update payment type", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        }

    }

    public ArrayList<PaymentType> getPaymentTypeList() throws IntegrationException {
        String query = "SELECT typeid, pmttypetitle\n"
                + "  FROM public.moneypaymenttype;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<PaymentType> paymentTypeList = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            System.out.println("");
            System.out.println("TRYING TO GET PAYMENT TYPE LIST");
            rs = stmt.executeQuery();
            System.out.println("PaymentTypeIntegrator.getPaymentTypeList | SQL: " + stmt.toString());
            while (rs.next()) {
                paymentTypeList.add(generatePaymentType(rs));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get payment type List", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        }
        return paymentTypeList;
    }

    public void insertPaymentType(PaymentType paymentType) throws IntegrationException {
        String query = "INSERT INTO public.moneypaymenttype(\n"
                + "    typeid, pmttypetitle)\n"
                + "    VALUES (DEFAULT, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, paymentType.getPaymentTypeTitle());
            System.out.println("PaymentTypeIntegrator.paymentTypeIntegrator | sql: " + stmt.toString());
            System.out.println("TRYING TO EXECUTE INSERT METHOD");
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Payment Type", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally

    }

    public void deletePaymentType(PaymentType pt) throws IntegrationException {
        String query = "DELETE FROM public.moneypaymenttype\n"
                + " WHERE typeid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, pt.getPaymentTypeId());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete payment type--probably because another"
                    + "part of the database has a reference item.", ex);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    private PaymentType generatePaymentType(ResultSet rs) throws IntegrationException {
        PaymentType newPtype = new PaymentType();
        try {
            newPtype.setPaymentTypeId(rs.getInt("typeid"));
            newPtype.setPaymentTypeTitle(rs.getString("pmttypetitle"));
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generating Payment Type from result set", ex);
        }
        return newPtype;
    }

    public ArrayList<PaymentType> getPaymentTypeTitleList() throws IntegrationException {
        ArrayList<PaymentType> payTypeList = new ArrayList<>();

        Connection con = getPostgresCon();
        String query = "SELECT typeid FROM moneypaymenttype;";
        ResultSet rs = null;
        Statement stmt = null;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                payTypeList.add(getPaymentTypeFromPaymentTypeID(rs.getInt("typeid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in PaymentTypeIntegrator.getPaymentTypeTitleList", ex);

        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {/* ignored */ }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally

        return payTypeList;
    }

    /* I think I built this all by accident while trying to de-bug my converter...
        public void generatePaymentTypeTitleIDMap() throws IntegrationException{
        HashMap<String, Integer> payMap = new HashMap<>();
        
        Connection con = getPostgresCon();
        String query = "SELECT typeid, pmttypetitle FROM paymenttype;";
        ResultSet rs = null;
        Statement stmt = null;
 
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()){
                payMap.put(rs.getString("pmttypetitle"), rs.getInt("typeid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Exception in PaymentTypeIntegrator.generatePaymentTypeTitleIDMap", ex);

        } finally{
           if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored * } }
           if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored * } }
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored *} }
        } // close finally
        
        paymentTypeMap = payMap;
    }

    /**
     * @return the paymentTypeMap
     *
    public HashMap getPaymentTypeMap() throws IntegrationException {
        generatePaymentTypeTitleIDMap();
        return paymentTypeMap;
    }

    /**
     * @param paymentTypeMap the paymentTypeMap to set
     *
    public void setPaymentTypeMap(HashMap paymentTypeMap) {
        this.paymentTypeMap = paymentTypeMap;
    }
     */
    public void updateOccupancyInspectionFee(Fee oif) throws IntegrationException {
        String query = "UPDATE public.moneyfee\n"
                + "   SET muni_municode=?, feename=?, feeamount=(?::numeric::money), effectivedate=?, \n"
                + "       expirydate=?, notes=? \n"
                + " WHERE feeid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, oif.getMuni().getMuniCode());
            stmt.setString(2, oif.getName());
            stmt.setDouble(3, oif.getAmount());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(oif.getEffectiveDate()));
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(oif.getExpiryDate()));
            stmt.setString(6, oif.getNotes());
            stmt.setInt(7, oif.getOccupancyInspectionFeeID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update occupancy inspection fee", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        }
    }

    public void deleteOccupancyInspectionFee(Fee oif) throws IntegrationException {
        String query = "DELETE FROM public.moneyfee\n" + " WHERE feeid= ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, oif.getOccupancyInspectionFeeID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete occupancy inspection fee--probably because another" + "part of the database has a reference item.", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    public ArrayList<Fee> getAllFeeTypes() throws IntegrationException {
        String query = "SELECT feeid, muni_municode, feename, feeamount, effectivedate, expirydate, \n"
                + "       notes\n"
                + "  FROM public.moneyfee";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<Fee> occupancyInspectionFeeList = new ArrayList();
        try {
            stmt = con.prepareStatement(query);
            System.out.println("");
            rs = stmt.executeQuery();
            System.out.println("PaymentIntegrator.getOccupancyInspectionFeeList | SQL: " + stmt.toString());
            while (rs.next()) {
                occupancyInspectionFeeList.add(generateOccupancyInspectionFee(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get Occupancy Inspection Fee List", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */
                }
            }
        }
        return occupancyInspectionFeeList;
    }

    public void insertOccupancyInspectionFee(Fee inspectionFee) throws IntegrationException {
        String query = "INSERT INTO public.moneyfee(\n"
                + "            feeid, muni_municode, feename, feeamount, effectivedate, expirydate, \n"
                + "            notes)\n"
                + "    VALUES (DEFAULT, ?, ?, ?::numeric::money, ?, ?, \n"
                + "            ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, inspectionFee.getMuni().getMuniCode());
            stmt.setString(2, inspectionFee.getName());
            stmt.setDouble(3, inspectionFee.getAmount());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(inspectionFee.getEffectiveDate()));
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(inspectionFee.getExpiryDate()));
            stmt.setString(6, inspectionFee.getNotes());
            System.out.println("PaymentIntegrator.occupancyInspectionFeeIntegrator | sql: " + stmt.toString());
            System.out.println("TRYING TO EXECUTE INSERT METHOD");
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Occupancy Inspection Fee", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    private Fee generateOccupancyInspectionFee(ResultSet rs) throws IntegrationException {
        Fee newOif = new Fee();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        try {
            newOif.setOccupancyInspectionFeeID(rs.getInt("feeid"));
            newOif.setMuni(mi.getMuni(rs.getInt("muni_municode")));
            newOif.setName(rs.getString("feename"));
            newOif.setAmount(rs.getDouble("feeamount"));
            newOif.setEffectiveDate(rs.getTimestamp("effectivedate").toLocalDateTime());
            newOif.setExpiryDate(rs.getTimestamp("expirydate").toLocalDateTime());
            newOif.setNotes(rs.getString("notes"));
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Error generation OccInspectionFee from result set", ex);
        }
        return newOif;
    }
}
