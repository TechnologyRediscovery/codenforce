/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.Manageable;
import com.tcvcog.tcvce.entities.ManagedSchemaEnum;
import com.tcvcog.tcvce.entities.PropertyUseType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;

/**
 *
 * @author Mike-Faux
 */
public class ManageBB extends BackingBeanUtils implements Serializable {

    /**
     * ------------------------------------------------ To add a new Manageable.
     *
     * - Create a sendName Method housing the Insert/Update/Deactivate Methods
     * for the new Manageable. 
     * - Create a entry in ManagedSchemaEnum for the new
     * Manageable. 
     * - Add the Manageable to the switch statements in
     * doMainSwitch, createNew, and loadMList. 
     * --- You will need a getList
     * method that takes in a boolean for showing deactivated manageables.
     * - Add fields for the new manageable on manage-home.xhtml
     * 
     * --- layout-manage.xhtml should not need to be touched
     */
    private List<Manageable> mList;
    private List<Icon> iconList;
    private ManagedSchemaEnum currentSchema;
    private Manageable current;
    private boolean showDeactivated = false;
    private boolean editMode = false;
    private boolean newManageable = false;
    private final List<ManagedSchemaEnum> enumList = Arrays.asList(ManagedSchemaEnum.values());

    private final String UPDATE = "Update";
    private final String INSERT = "Insert";
    private final String DEACTIVATE = "Deactivate";

    /**
     *
     */
    public ManageBB() {
    }

    @PostConstruct
    public void initBean() {
        currentSchema = ManagedSchemaEnum.Icon;
        current = new Icon();
        loadMList();
        System.out.println(Arrays.toString(ManagedSchemaEnum.values()));
    }

    /**
     * Properly routes Update, Insert, and Deactivate requests to the proper
     * methods
     *
     * @param m Manageable to Update/Insert/Deactivate
     * @param action String of action to perform. Must be one of the Final
     * Variables UPDATE/INSERT/DEACTIVATE
     * @throws IntegrationException
     */
    private void doMainSwitch(Manageable m, String action) throws IntegrationException {

        switch (currentSchema) {
            case Icon:
                if (current instanceof Icon) {
                    sendIcon((Icon) m, action);
                }
                return;
            case PropertyUseType:
                if (current instanceof PropertyUseType) {
                    sendPropertyUseType((PropertyUseType) m, action);
                }
                return;
            case BlobType:
                if (current instanceof BlobType) {
                    sendBlobType((BlobType) m, action);
                }
                return;
            /*case BobSource:

            case CEActionRequestIssueType:

            case CEActionRequestStatus:

            case CitationStatus:

            case CitationFilingType:

            case ContactPhoneType:

            case ImprovementType:

            case LinkedObjectRole:

            case LogCategory:

            case MoneyPaymentType:

            case OCCPeriodType:

            case OCCInspectionCause:

            case OCCInspectionDetermination:

            case TaxStatus:

            case TextBlockCategory:

            case IntensityClass:

            /*case CourtEntity:
                        break;*/
            default:
                throw new IntegrationException("Action " + action + " " + m.getMANAGEABLE_SCHEMA().getTARGET_OBJECT_FRIENDLY_NAME() + " not implemented");

        }
    }

    /**
     * Creates a new Manageable based off currentSchema
     */
    public void createNew() {
        newManageable = true;
        editMode = true;
        switch (currentSchema) {
            case Icon:
                current = new Icon();
                break;
            case PropertyUseType:
                current = new PropertyUseType();
                break;
            case BlobType:
                current = new BlobType();
                break;
            default:
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Entity " + currentSchema.getTARGET_OBJECT_FRIENDLY_NAME(), " could not be created!"));
                return;
        }
        current.setID(-1);
    }

    /**
     * Loads Manageable List mList with Manageables based off currentSchema and
     * showDeactivated
     */
    private void loadMList() {
        SystemCoordinator sc = getSystemCoordinator();
        mList = new ArrayList<>();
        try {
            switch (currentSchema) {
                case Icon:
                    mList.addAll(sc.getIconList(showDeactivated));
                    break;
                case PropertyUseType:
                    mList.addAll(sc.getPutList(showDeactivated));
                    break;
            }

            iconList = sc.getIconList();

        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not get " + currentSchema.getTARGET_OBJECT_FRIENDLY_NAME(), " List"));
        }
    }

    /**
     * Refreshes the Manageable List mList
     */
    public void refreshMList() {
        loadMList();
    }

    /**
     * Assigns Manageable m to be edited
     *
     * @param m Manageable to edit
     */
    public void edit(Manageable m) {
        newManageable = false;
        editMode = false;
        current = m;
    }

    /**
     * Updates current Manageable in the database
     *
     * @param ev
     */
    public void commitUpdates(ActionEvent ev) {
        if (current.getMANAGEABLE_SCHEMA() != currentSchema) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Schema mismatch! Could not update " + currentSchema.getTARGET_OBJECT_FRIENDLY_NAME() + "!", ""));
        } else {

            try {
                doMainSwitch(current, UPDATE);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Successfully updated " + currentSchema.getTARGET_OBJECT_FRIENDLY_NAME(), ""));
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Could not update " + currentSchema.getTARGET_OBJECT_FRIENDLY_NAME(), ""));
            }
        }

    }

    /**
     * Inserts current Manageable into the database
     *
     * @param ev
     */
    public void commitInsert(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();
        if (current.getMANAGEABLE_SCHEMA() != currentSchema) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Schema mismatch! Could not insert " + currentSchema.getTARGET_OBJECT_FRIENDLY_NAME() + "!", ""));
        } else {

            try {
                doMainSwitch(current, INSERT);

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Successfully inserted " + currentSchema.getTARGET_OBJECT_FRIENDLY_NAME(), ""));
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Could not insert " + currentSchema.getTARGET_OBJECT_FRIENDLY_NAME(), ""));
            }
        }
    }

    /**
     * Removes current Manageable from the database
     *
     * @param ev
     */
    public void commitRemove(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();
        if (current.getMANAGEABLE_SCHEMA() != currentSchema) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Schema mismatch! Could not remove " + currentSchema.getTARGET_OBJECT_FRIENDLY_NAME() + "!", ""));
        } else {
            if (current.getID() > 0) {
                try {
                    int uses = sc.checkForUse(current);

                    if (uses == 0) {
                        doMainSwitch(current, DEACTIVATE);
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Success! " + currentSchema.getTARGET_OBJECT_FRIENDLY_NAME() + " removed", ""));
                    } else {
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        currentSchema.getTARGET_OBJECT_FRIENDLY_NAME() + " is in use " + uses + " times. Could not remove", ""));
                    }
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Could not remove " + currentSchema.getTARGET_OBJECT_FRIENDLY_NAME() + ", sorry", ""));
                }
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Invalid ID: " + current.getID(), ""));
            }
        }
    }

    /**
     * Listener for editMode Toggle
     */
    public void onEditModeToggle() {
        if (editMode) {
            if (newManageable) {
                commitInsert(null);
            } else {
                commitUpdates(null);
            }
        }
        refreshMList();
        editMode = !editMode;
    }

    /**
     * Listener for Remove/Restore Button
     */
    public void onRemoveRestore() {
        if (current.getDeactivatedts() == null) {
            commitRemove(null);
        } else {
            current.setDeactivatedts(null);
            commitUpdates(null);
        }
        refreshMList();
    }

    /**
     * Listener for showDeactivated toggle
     */
    public void onShowDeactivatedToggle() {
        refreshMList();
    }

    /**
     * @return the showDeactivated
     */
    public boolean isShowDeactivated() {
        return showDeactivated;
    }

    /**
     * @param showDeactivated the showDeactivated to set
     */
    public void setShowDeactivated(boolean showDeactivated) {
        this.showDeactivated = showDeactivated;
    }

    /**
     * @return the currentSchema
     */
    public ManagedSchemaEnum getCurrentSchema() {
        return currentSchema;
    }

    /**
     * @param currentSchema the currentSchema to set
     */
    public void setCurrentSchema(ManagedSchemaEnum currentSchema) {
        this.currentSchema = currentSchema;
        loadMList();
    }

    /**
     * @return the current
     */
    public Manageable getCurrent() {
        return current;
    }

    /**
     * @param current the current to set
     */
    public void setCurrent(Manageable current) {
        this.current = current;
    }

    /**
     * @return the mList
     */
    public List<Manageable> getmList() {
        return mList;
    }

    /**
     * @param mList the mList to set
     */
    public void setmList(List<Manageable> mList) {
        this.mList = mList;
    }

    /**
     * @return the enumList
     */
    public List<ManagedSchemaEnum> getEnumList() {
        return enumList;
    }

    /**
     * @return the editMode
     */
    public boolean isEditMode() {
        return editMode;
    }

    /**
     * @return the newManageable
     */
    public boolean isNewManageable() {
        return newManageable;
    }

    /**
     * @return the iconList
     */
    public List<Icon> getIconList() {
        return iconList;
    }

    /**
     * @param iconList the iconList to set
     */
    public void setIconList(List<Icon> iconList) {
        this.iconList = iconList;
    }

    /**
     * Sends Icon i to the database with instructions action
     *
     * @param i the Icon sent to the Database
     * @param action the action to affect Icon i
     * @throws IntegrationException
     */
    private void sendIcon(Icon i, String action) throws IntegrationException {
        SystemCoordinator sc = getSystemCoordinator();
        switch (action) {
            case UPDATE:
                sc.updateIcon(i);
                break;
            case INSERT:
                sc.insertIcon(i);
                break;
            case DEACTIVATE:
                sc.deactivateIcon(i);
                break;
        }
    }

    /**
     * Sends PropertyUseType p to the database with instructions action
     *
     * @param p the PropertyUseType sent to the Database
     * @param action the action to affect PropertyUseType p
     * @throws IntegrationException
     */
    private void sendPropertyUseType(PropertyUseType p, String action) throws IntegrationException {
        SystemCoordinator sc = getSystemCoordinator();
        switch (action) {
            case UPDATE:
                sc.updatePut(p);
                break;
            case INSERT:
                sc.insertPut(p);
                break;
            case DEACTIVATE:
                sc.deactivatePut(p);
                break;
        }
    }

    /**
     * Sends BlobType b to the database with instructions action
     *
     * @param b the BlobType sent to the Database
     * @param action the action to affect BlobType b
     * @throws IntegrationException
     */
    private void sendBlobType(BlobType b, String action) throws IntegrationException {
        BlobCoordinator bc = getBlobCoordinator();
        switch (action) {
            case UPDATE:
                bc.updateBlobType(b);
                break;
            case INSERT:
                bc.insertBlobType(b);
                break;
            case DEACTIVATE:
                bc.deactivateBlobType(b);
                break;
        }
    }
}
