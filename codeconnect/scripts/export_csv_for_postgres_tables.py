#!python2
import re

import pyodbc

import csv_utils

DB_PATH = "/home/cedba/systems_project/accessdb/PropertyMgmt-DataBase.accdb"
CSV_FILE_ENCODING = 'utf-8'

CSVS_TO_EXPORT = (
    # Postgres tables to export CSV's for
    'property',
    'municipality',
    'codeViolaton',
    'propertyOwner',
    'propertyAgent',
    'codeOfficer',
    'codeEnfEvent',
    'codeEnfCase',
    'inspecChecklist',
    'occInspec',
    'occPermit',
    'payment',
)

cnxn = pyodbc.connect(
    r'DRIVER={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=%s;' % DB_PATH)

# List of properties that we've skipped
skipped_properties = []

def main():
    print "Starting the export process of %d files..." % len(CSVS_TO_EXPORT)
    for i, csv_file in enumerate(CSVS_TO_EXPORT, start=1):
        print '[%d/%d] Exporting %s' % (i, len(CSVS_TO_EXPORT), csv_file)
        export_csv(csv_file)


def export_csv(csv_file):
    CSV_FILE_FUNCTION_MAPPING = {
        'property': export_property,
        'municipality': export_municipality,
        'codeViolaton': export_code_violation,
        'propertyOwner': export_propertyOwner,
        'propertyAgent': export_agent,
        'occPermit': export_occPermit,
        'occInspec': export_occInspec,
        'payment': export_payment,
        'inspecChecklist': export_inspecChecklist,
        'codeOfficer': export_codeOfficer,
        'codeEnfCase': export_codeEnfCase,
        'codeEnfEvent': export_codeEnfEvent,
        'codeEnfCaseViolation': export_codeEnfCaseViolation
    }
    if csv_file not in CSV_FILE_FUNCTION_MAPPING:
        print "Don't know how to generate csv file %s." % csv_file
        return
    # Generate the file using the right function
    CSV_FILE_FUNCTION_MAPPING[csv_file]()

def export_agent():
    # TODO: Add property agentID, propertyUseID, ownerID
    sql_query = """
        SELECT
          AgentID,
          CompanyName,
          ContactName,
          MailingAddress,
          City,
          State,
          ZipCode
        FROM
          tblAgent
    """
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    OUT_FILE_NAME = 'propertyAgent.csv'
    # note that we are using postgres fields called agentFirstName for the agent's company
    # use agentLastName for the full name of the agent
    header = (
        'agentID',
        'agentFirstName',
        'agentLastName',
        'mailingAddress',
        'mailingCity',
        'mailingState',
        'mailingZipcode'
    )

    write_to_csv(OUT_FILE_NAME, header, cursor.fetchall())

def export_codeEnfCase():
    # no place to put description field
    # cannot join with codeEnfEvent detail due to lack of correlating key
    sql_query = """
        SELECT
            EventID,
            EventDate,
            PropertyID,
            OrdinanceOfficer,
            ObservationInfo
          
        FROM
            tblCodeEnforcement
    """
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    OUT_FILE_NAME = 'codeEnfCase.csv'
    header = (
        'caseID',
        'caseOpenDate',
        'property_propertyID',
        'codeOfficer_officerID',
        'CaseStatus_ceEventStatusID',
        'comments'
    )

    def get_rows():
        for r in cursor.fetchall():
            if r.EventID is None:
                print 'Warning: Skipping row with missing eventID: %s' % r
                continue
            if r.PropertyID in skipped_properties:
                print 'Warning: Skipping row with invalid propertyID: %s' % r
                continue
            # Assign case status 6 (undetermined) to all cases from Access
            ceEventStatus = 6
            # If officerID is missing, assign it to officer 0
            officerID = 0 if r.OrdinanceOfficer is None else r.OrdinanceOfficer
            clean_row = (
                r.EventID,
                r.EventDate,
                r.PropertyID,
                officerID,
                ceEventStatus,
                r.ObservationInfo
            )
            yield (clean_row)

    write_to_csv(OUT_FILE_NAME, header, get_rows())


# all of these events are code enforcement letters
def export_codeEnfEvent():
    # EventID is our case id
    sql_query = """
        SELECT
            EventID,
            EventDate,
            ObservationInfo,
            LetterText,
            OrdinanceOfficer
        FROM
          tblCodeEnforcement
        WHERE (((tblCodeEnforcement.LetterText) Is Not Null));
    """
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    OUT_FILE_NAME = 'codeEnfEvent.csv'
    header = (
        'codeEnfCase_caseID',
        'eventDate',
        'eventDescription',
        'letterText',
        'codeOfficer_officerID',
    )

    def get_rows():
        for r in cursor.fetchall():
            if r.EventID is None:
                print 'Warning: Skipping row with missing eventID: %s' % r
                continue
            if r.EventDate is None:
                print 'Warning: Skipping row with missing date: %s' % r
                continue
            # If officerID is missing, assign it to officer 0
            officerID = 0 if r.OrdinanceOfficer is None else r.OrdinanceOfficer
            clean_row = (
                r.EventID,
                r.EventDate,
                r.ObservationInfo,
                r.LetterText,
                officerID
            )
            yield (clean_row)

    write_to_csv(OUT_FILE_NAME, header, get_rows())

# populate briding table between code enforcement case and its violations
def export_codeEnfCaseViolation():
    sql_query = """
        SELECT
            EventID,
            ViolationID
        FROM
          [tblCodeEnforcement-Detail]
    """
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    OUT_FILE_NAME = 'codeEnfCaseViolation.csv'
    # note that we are using postgres fields called agentFirstName for the agent's company
    # use agentLastName for the full name of the agent
    header = (
        'codeViolation_violationID',
        'codeEnfCase_caseID'
    )

    write_to_csv(OUT_FILE_NAME, header, cursor.fetchall())


def export_occPermit():
    # extract fee paid and amount for the fee paid table set in postgres
    # what to do with field name in access called inspection#, passinspection?
    sql_query = """
        SELECT
          RentalID, 
          RefNum,
          PassDate,
          TempToClose
        FROM
          tblRentalPermit
    """
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    # the weird refNum field in Access is dumped into the permitComments field in postgres
    # since there is no better way to store this data since some of them are really long
    OUT_FILE_NAME = 'occPermit.csv'
    header = (
        'inspectionID', 
        'permitComments', 
        'dateIssued',
        'temporaryPermit',
        )

    write_to_csv(OUT_FILE_NAME, header, cursor.fetchall())

def export_occInspec():
    sql_query = """
        SELECT
            PropertyID,
            RentalID,
            InspectionDate,
            PassDate,
            Reinspect,
            Resolved,
            GeneralComments
        FROM
          tblRentalPermit
    """
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    # the weird refNum field in Access is dumped into the permitComments field in postgres
    OUT_FILE_NAME = 'occInpsec.csv'

    # use the reinspect and resolved fields from access to determine the 
    # proper inspection status code in import. HENCE the GENERATE prefix
    # if resolved is true, then use inspection status code for resolved
    header = (
        'property_propertyID',
        'inspectionID',
        'firstInspectionDate',
        'inspectionPassDate',
        'GENERATEreinspect',
        'GENERATEresolved',
        'inspectionComments'
        )

    write_to_csv(OUT_FILE_NAME, header, cursor.fetchall())

def export_payment():
    # ship out inspection date as a proxy if payment date is NULL
    sql_query = """
        SELECT
           RentalID,
           InspectionDate,
           PaidDate,
           FeePaid
        FROM
            tblRentalPermit
        WHERE
            ((Not (tblRentalPermit.FeePaid)=0));
    """
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    # the weird refNum field in Access is dumped into the permitComments field in postgres
    OUT_FILE_NAME = 'payment.csv'

    # use the reinspect and resolved fields from access to determine the 
    # proper inspection status code in import. HENCE the GENERATE prefix
    header = (
        'occInspec_inspection',
        'dateReceived',
        'GENERATEinspectionDate',
        'amount'
        )

    write_to_csv(OUT_FILE_NAME, header, cursor.fetchall())

def export_inspecChecklist():
    # TODO: Add property agentID, propertyUseID, ownerID
    sql_query = """
        SELECT
            RentalID,
            Roof,
            Roof_VID,
            RainGutters,
            RainGutters_VID,
            DownSpouts,
            DownSpouts_VID,
            ExtWindows,
            ExtWindows_VID,
            WindowScreens,
            WindowScreens_VID,
            Doors,
            Doors_VID,
            DoorScreens,
            DoorScreens_VID,
            ExtRailings,
            ExtRailings_VID,
            Porch,
            Porch_VID,
            Sidewalks,
            Sidewalks_VID,
            Walls,
            Walls_VID,
            Ceiling,
            Ceiling_VID,
            Floors,
            Floors_VID,
            IntWindows,
            IntWindows_VID,
            Stairs,
            Stairs_VID,
            IntRailings,
            IntRailings_VID,
            SmokeDetectorsBed1,
            SmokeDetectorsBed2,
            SmokeDetectorsBed3,
            SmokeDetectorsOther,
            SmokeDetectors_VID,
            CODetectors,
            CODetectors_VID,
            GeneralSanitation,
            GeneralSanitation_VID,
            [Mold/Mildew],
            [Mold/Mildew_VID],
            Vermin,
            Vermin_VID,
            [Trash/Garbage],
            [Trash/Garbage_VID],
            KitchenFaucets,
            KitchenFaucets_VID,
            BathLavatory,
            BathLavatory_VID,
            BathTub,
            BathTub_VID,
            BathShower,
            BathShower_VID,
            LaundryTub,
            LaundryTub_VID,
            Toilet,
            Toilet_VID,
            ElectricalService,
            ElectricalService_VID,
            IdentifyAllBreakers,
            IdentifyAllBreakers_VID,
            [Switches/CoverPlates],
            [Switches/CoverPlates_VID],
            [Receptacles/CoverPlates],
            [Receptacles/CoverPlates_VID],
            GFCIs,
            GFCIs_VID,
            KitchenElectric,
            KitchenElectric_VID,
            BathRoomElectric,
            BathRoomElectric_VID,
            CeilingLights,
            CeilingLights_VID,
            ExteriorLights,
            ExteriorLights_VID,
            Laundry,
            Laundry_VID,
            StairwayLights,
            StairwayLights_VID,
            GeneralFurnace,
            GeneralFurnace_VID,
            [FurnaceVent/Exhaust],
            [FurnaceVent/Exhaust_VID],
            Thermostat,
            Thermostat_VID,
            GeneralHotWaterTank,
            GeneralHotWaterTank_VID,
            [HotWaterVent/Exhaust],
            [HotWaterVent/Exhaust_VID],
            TPSafetyValve,
            TPSafetyValve_VID,
            [3/4BlowOff6inFromFloor],
            [3/4BlowOff6inFromFloor_VID],
            Controller,
            Controller_VID,
            RoofComment,
            RainGuttersComment,
            DownSpoutsComment,
            ExtWindowsComment,
            WindowScreensComment,
            DoorsComment,
            DoorScreensComment,
            ExtRailingsComment,
            PorchComment,
            SidewalksComment,
            WallsComment,
            CeilingComment,
            FloorsComment,
            IntWindowsComment,
            StairsComment,
            IntRailingsComment,
            SmokeDetectorsComment,
            CODetectorsComment,
            GeneralSanitationComment,
            [Mold/MildewComment],
            VerminComment,
            [Trash/GarbageComment],
            RecycleBinsComment,
            KitchenFaucetsComment,
            BathLavatoryComment,
            BathTubComment,
            BathShowerComment,
            LaundryTubComment,
            ToiletComment,
            ElectricalServiceComment,
            IdentifyAllBreakersComment,
            [Switches/CoverPlatesComment],
            [Receptacles/CoverPlatesComment],
            GFCIsComment,
            KitchenElectricComment,
            BathroomElectricComment,
            CeilingLightsComment,
            ExteriorLightsComment,
            LaundryComment,
            StairwayLightsComment,
            GeneralFurnaceComment,
            [FurnaceVent/ExhaustComment],
            ThermostatComment,
            GeneralHotWaterTankComment,
            [HotWaterVent/ExhaustComment],
            TPSafetyValveComment,
            [3/4BlowOff6inFromFloorComment],
            ControllerComment
        FROM
          tblRentalPermit
    """
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    OUT_FILE_NAME = 'inspecChecklist.csv'
    header = (
        'checklistID',
        'Roof',
        'Roof_VID',
        'RainGutters',
        'RainGutters_VID',
        'DownSpouts',
        'DownSpouts_VID',
        'ExtWindows',
        'ExtWindows_VID',
        'WindowScreens',
        'WindowScreens_VID',
        'Doors',
        'Doors_VID',
        'DoorScreens',
        'DoorScreens_VID',
        'ExtRailings',
        'ExtRailings_VID',
        'Porch',
        'Porch_VID',
        'Sidewalks',
        'Sidewalks_VID',
        'Walls',
        'Walls_VID',
        'Ceiling',
        'Ceiling_VID',
        'Floors',
        'Floors_VID',
        'IntWindows',
        'IntWindows_VID',
        'Stairs',
        'Stairs_VID',
        'IntRailings',
        'IntRailings_VID',
        'SmokeDetectorsBed1',
        'SmokeDetectorsBed2',
        'SmokeDetectorsBed3',
        'SmokeDetectorsOther',
        'SmokeDetectors_VID',
        'CODetectors',
        'CODetectors_VID',
        'GeneralSanitation',
        'GeneralSanitation_VID',
        'Mold_Mildew',
        'Mold_Mildew_VID',
        'Vermin',
        'Vermin_VID',
        'Trash_Garbage',
        'Trash_Garbage_VID',
        'KitchenFaucets',
        'KitchenFaucets_VID',
        'BathLavatory',
        'BathLavatory_VID',
        'BathTub',
        'BathTub_VID',
        'BathShower',
        'BathShower_VID',
        'LaundryTub',
        'LaundryTub_VID',
        'Toilet',
        'Toilet_VID',
        'ElectricalService',
        'ElectricalService_VID',
        'IdentifyAllBreakers',
        'IdentifyAllBreakers_VID',
        'Switches_CoverPlates',
        'Switches_CoverPlates_VID',
        'Receptacles_CoverPlates',
        'Receptacles_CoverPlates_VID',
        'GFCIs',
        'GFCIs_VID',
        'KitchenElectric',
        'KitchenElectric_VID',
        'BathRoomElectric',
        'BathRoomElectric_VID',
        'CeilingLights',
        'CeilingLights_VID',
        'ExteriorLights',
        'ExteriorLights_VID',
        'Laundry',
        'Laundry_VID',
        'StairwayLights',
        'StairwayLights_VID',
        'GeneralFurnace',
        'GeneralFurnace_VID',
        'FurnaceVent_Exhaust',
        'FurnaceVent_Exhaust_VID',
        'Thermostat',
        'Thermostat_VID',
        'GeneralHotWaterTank',
        'GeneralHotWaterTank_VID',
        'HotWaterVent_Exhaust',
        'HotWaterVent_Exhaust_VID',
        'TPSafetyValve',
        'TPSafetyValve_VID',
        'BlowOff6inFromFloor',
        'BlowOff6inFromFloor_VID',
        'Controller',
        'Controller_VID',
        'RoofComment',
        'RainGuttersComment',
        'DownSpoutsComment',
        'ExtWindowsComment',
        'WindowScreensComment',
        'DoorsComment',
        'DoorScreensComment',
        'ExtRailingsComment',
        'PorchComment',
        'SidewalksComment',
        'WallsComment',
        'CeilingComment',
        'FloorsComment',
        'IntWindowsComment',
        'StairsComment',
        'IntRailingsComment',
        'SmokeDetectorsComment',
        'CODetectorsComment',
        'GeneralSanitationComment',
        'Mold_MildewComment',
        'VerminComment',
        'Trash_GarbageComment',
        'RecycleBinsComment',
        'KitchenFaucetsComment',
        'BathLavatoryComment',
        'BathTubComment',
        'BathShowerComment',
        'LaundryTubComment',
        'ToiletComment',
        'ElectricalServiceComment',
        'IdentifyAllBreakersComment',
        'Switches_CoverPlatesComment',
        'Receptacles_CoverPlatesComment',
        'GFCIsComment',
        'KitchenElectricComment',
        'BathroomElectricComment',
        'CeilingLightsComment',
        'ExteriorLightsComment',
        'LaundryComment',
        'StairwayLightsComment',
        'GeneralFurnaceComment',
        'FurnaceVent_ExhaustComment',
        'ThermostatComment',
        'GeneralHotWaterTankComment',
        'HotWaterVent_ExhaustComment',
        'TPSafetyValveComment',
        'BlowOff6inFromFloorComment',
        'ControllerComment'
    )

    write_to_csv(OUT_FILE_NAME, header, cursor.fetchall())



def export_property():
    # TODO: Add property agentID, propertyUseID, ownerID
    sql_query = """
        SELECT
          PropertyID,
          parID,
          lotAndBlock,
          Address,
          ApartmentNo,
          municode_1 as municipality_municipalityID
        FROM
          tblProperty
    """
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    OUT_FILE_NAME = 'property.csv'
    header = (
        'PropertyID',
        'parID',
        'lotAndBlock',
        'Address',
        'ApartmentNo',
        'municipality_municipalityID'
    )

    def get_rows():
        for r in cursor.fetchall():
            if r.parID is None or r.PropertyID is None:
                print(
                  "Warning: Skipping row with missing"
                  " parcel or property id: %s" % r)
                skipped_properties.append(r.PropertyID)
                continue
            municipalityID = '%d' % r.municipality_municipalityID
            apartmentNo = (
                None if r.ApartmentNo is None or not r.ApartmentNo.strip() else
                r.ApartmentNo)
            parID = r.parID.replace('-', '')
            clean_row = (
                r.PropertyID,
                parID,
                r.lotAndBlock,
                r.Address,
                apartmentNo,
                municipalityID
            )
            yield (clean_row)

    write_to_csv(OUT_FILE_NAME, header, get_rows())


def export_municipality():
    sql_query = """
        SELECT
          municode_1,
          MunDesc
        FROM
          tblMunicipal
    """
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    OUT_FILE_NAME = 'municipality.csv'
    header = (
        'municipalityID',
        'muniName',
    )

    write_to_csv(OUT_FILE_NAME, header, cursor.fetchall())

def export_codeOfficer():
    sql_query = """
        SELECT
          OrdinanceOfficer,
          OrdinanceOfficerName
        FROM
          tblOrdinanceOfficerCode
    """
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    OUT_FILE_NAME = 'codeOfficer.csv'
    # insert the first and last name into lastName 
    header = (
        'officerID',
        'GENERATEOrdinanceOfficerName',
    )

    write_to_csv(OUT_FILE_NAME, header, cursor.fetchall())

def export_code_violation():
    #TODO: Assign typeId as unknown for unmapped violations
    sql_query = """
        SELECT
          ViolationID,
          municode_1,
          Violation,
          ViolationLetter,
          Active
        FROM
          tblIssueCode
    """
  
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    OUT_FILE_NAME = 'code_violation.csv'
    header = (
        'violationID',
        'municipalCodeNum',
        'violationName',
        'violationLetter',
        'active', 
    )

    write_to_csv(OUT_FILE_NAME, header, cursor.fetchall())

def export_propertyOwner():
    #TODO: Assign typeId as unknown for unmapped violations
    sql_query = """
        SELECT
          OwnerID,
          PropertyOwner,
          MailingAddress,
          City,
          State,
          ZipCode
        FROM
          tblOwner
    """
  
    cursor = cnxn.cursor()
    cursor.execute(sql_query)

    # Write to CSV
    OUT_FILE_NAME = 'propertyOwner.csv'
    header = (
        'ownerID',
        'ownerFullname',
        'ownerAddress',
        'ownerCity',
        'ownerState', 
        'ownerZIPCode'
    )

    write_to_csv(OUT_FILE_NAME, header, cursor.fetchall())


def write_to_csv(output_file, header, rows):
    with open(output_file, 'w') as outfile:
        writer = csv_utils.UnicodeWriter(outfile, delimiter="|", encoding=CSV_FILE_ENCODING)
        writer.writerow(header)
        for row in rows:
            writer.writerow((clean_value(r) for r in row))


def clean_value(value):
    # Common cleaning operations on fields
    # Convert to string
    as_string = unicode(value).strip()
    # Remove trailing zeroes
    as_string = re.sub('[.]0+$', '', as_string)
    # Replace multiple whitespace characters with a single space
    as_string = re.sub('\s+', ' ', as_string)
    return as_string


if __name__ == '__main__':
    main()
