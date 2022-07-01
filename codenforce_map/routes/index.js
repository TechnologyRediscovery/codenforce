var express = require('express'); //require Express
var router = express.Router();  // setup usage of the Express router engine

// PostgreSQL and PostGIS module and connection setup
const { Client, Query } =require('pg');

// Setup Connection
var username = "sylvia";
var password = "c0d3";
var host = "localhost:3000";
var database = "cogdb";
var connString = "postgres://" + username + ":" + password + "@" + host + "/" + database;

// Set up your database query to display GeoJSON
// Changed to propertymapdata2 on 5.16.21 to reflect ownership data
var json_query = "SELECT row_to_json(fc) FROM ( SELECT 'FeatureCollection' As type, array_to_json(array_agg(f)) AS features FROM (SELECT 'Feature' As type, ST_AsGeoJSON(lg.geom)::json As geometry, row_to_json((propertyho, propertyad, usegroup, propertyci, propertyzi, ownercode, landbankheld, active, casecount, casename, propertyid, address, address_citystatezip)) AS properties FROM propertymapdata2 as lg) As f) As fc";
//Variable for holding property json data
var sendData = "";

//Variable for holding municipality data for dropdown select menu
var municipalities = [];
var htmlFormString = "";  //HTML String variable to pass for dropdown menu

//Establishing Arrays for Image Search
var residential = ["TOWNHOUSE", "APART: 5-19 UNITS", "COMM APRTM CONDOS 5-19 UNITS", "ROWHOUSE", "APART:20-39 UNITS", "THREE FAMILY", "Residential", "CONDOMINIUM COMMON PROPERTY", "MOBILE HOME", "TWO FAMILY", "COMM APRTM CONDOS 40+ UNITS", "FOUR FAMILY", "RESIDENTIAL", "MOBILE HOMES/TRAILER PKS", "SINGLE FAMILY", "CONDOMINIUM", "CONDOMINIUM UNIT", "MOBILE HOME (IN PARK)", "APART:40+ UNITS", "RIGHT OF WAY - RESIDENTIAL", "GROUP HOME", "INDEPENDENT LIVING (SENIORS)", "CHARITABLE EXEMPTION/HOS/HOMES"];

var commercial = ["RESTAURANT, CAFET AND/OR BAR", "NEIGH SHOP CENTER", "RETL/STOR OVER", "CONVENIENCE STORE/GAS", "COMMUNITY SHOPPING CENTER", "OTHER RETAIL STRUCTURES", "BIG BOX RETAIL", "BOWLING ALLEYS/REC FACILITY", "DEPARTMENT STORE", "OFFICE-WALKUP -3 + STORIES", "AUTO SERV STATION", "RETL/OFF OVER", "DWG USED AS OFFICE", "Commercial", "MEDICAL CLINICS/OFFICES", "COMM AUX BUILDING", "RIGHT OF WAY - COMMERCIAL", "CAR WASH", "COMMERCIAL", "OFFICE/RETAIL OVER", "OFFICE/WAREHOUSE", "FAST FOOD/DRIVE THRU WINDOW", "OFFICE - 1-2 STORIES", "BANK", "WAREHOUSE", "OTHER COMMERCIAL HOUSING", "DISCOUNT STORE", "VACANT COMMERCIAL LAND", "CONDOMINIUM OFFICE BUILDING", "BED & BREAKFAST", "FUNERAL HOMES", "SMALL SHOP", "PHARMACY (CHAIN)", "HOTELS", "AUTO SALES & SERVICE", "DRIVE IN REST OR FOOD SERVICE", "COMMERCIAL GARAGE", "OFFICE-ELEVATOR -3 + STORIES"];

var mixeduse = ["OFFICE/APARTMENTS OVER", "RES AUX BUILDING (NO HOUSE)", "RETL/APT'S OVER", "DWG USED AS RETAIL", "COMMUNITY URBAN RENEWAL", "OWNED BY METRO HOUSING AU", "SMALL DETACHED RET(UNDER 10000)", "LODGE HALL/AMUSEMENT PARK"];

var parking = ["PARKING GARAGE/LOTS"];

var industrial = ["LIGHT MANUFACTURING", "INDUSTRIAL", "Industrial", "COAL RIGHTS, WORKING INTERESTS", "MINI WAREHOUSE"];

var government = ["MUNICIPAL GOVERNMENT", "COUNTY GOVERNMENT", "GOVERNMENT", "Government", "OWNED BY BOARD OF EDUCATION", "FEDERAL GOVERNMENT", "MUNICIPAL URBAN RENEWAL"];

var utilities = ["UTILITIES", "Utilities", "COMMERCIAL/UTILITY"];

var railroad = ["R.R. - USED IN OPERATION", "R.R. - NOT USED IN OPERATION"];

var agricultural = ["AGRICULTURAL", "GENERAL FARM", "Agricultural", "GREENHOUSES, VEG & FLORACULTURE"];

var worship = ["CHURCHES, PUBLIC WORSHIP"];

var school = ["DAYCARE/PRIVATE SCHOOL", "OWNED BY COLLEGE/UNIV/ACADEMY"];

var lot = [">10 ACRES VACANT", "PUBLIC PARK", "CEMETERY/MONUMENTS", "BUILDERS LOT", "VACANT LAND"];

var other = ["None", "OTHER", "Other", "NULL"]

//Retrieves image data for later use
const fs = require('fs');
const path = require('path');

imageFiles = fs.readdirSync('./public/images/');

var imageBank = [];

imageFiles.forEach(file => {
  if (path.extname(file) == ".png")
    imageBank.push(file.slice(0, -4))
  });

/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'CodeNForce' });
});

module.exports = router;

// GET the map page
router.get('/map', async function(req, res) {         //(ASYNCs AND AWAITs ARE NEW, REMOVE IF BROKEN)
  var client = new Client(connString);               //setup Postgres Client
  client.connect();                                 //connect to the client
  var query = client.query(new Query(json_query)); //Run Query

  
  // Run original query
  query.on("row", function(row, result) {
    result.addRow(row);
  });
  //Pass the result to the map page
  query.on("end", async function(result) {
    var data = result.rows[0].row_to_json; // Save JSON as variable data
    sendData = data; //Save JSON variable outside of function
    //Populates municipality data to send to map
    for (var i=0; i < data.features.length -1; i++) {
      var city = data.features[i].properties.f4;

      if (city) {
        var cityCheck = municipalities.includes(city);

        if (cityCheck == false) {
          municipalities.push(city);
          }
      }
    };
    //Creates dropdown menu HTML for passing
    var formBuild = "";
    for (var i=0; i <= municipalities.length -1; i++){
      var city = municipalities[i];
      formBuild += '<option value="' + city + '">' + city + '</option>';
    };

    var formTag = await '<select name="propertyci"><option disabled="", selected="", value="">Select One...</option>' + formBuild + '</select><input type="submit", value="Submit">';

    htmlFormString = formTag;

    var zipView = [40.3725,-79.8701, 12]; // starting point center for map
    res.render('map', {
      title: "CodeNforce Map", //Give title to the page
      jsonData: data, //Pass the data to the view
      zipView: zipView, //Pass the zipView to the map view
      htmlFormString: htmlFormString //Pass the form html string to the map view
    });
  });
});


// !{jsonData} is the name of the variable passed from index.js to the web page document


// GET the filtered page
router.get('/filter*', function (req, res) {
    var propertyci = req.query.propertyci;
    if (propertyci.indexOf("--") > -1 || propertyci.indexOf("'") > -1 || propertyci.indexOf(";") > -1 || propertyci.indexOf("/*") > -1 || propertyci.indexOf("xp_") > -1){
        console.log("Bad request detected");
        res.redirect('/map');
        return;
    } else {
        console.log("Request passed");
    var case_json_query = "SELECT row_to_json(fc) FROM ( SELECT 'FeatureCollection' As type, array_to_json(array_agg(f)) AS features FROM (SELECT 'Feature' As type, row_to_json((propertyid, caseid, violationid, casename, description, originationdate, closingdate, entrytimestamp, stipulatedcompliancedate, actualcompliancedate)) AS properties FROM casedata as lg) As f) As fc";

    var client = new Client(connString);
        client.connect();
        var query = client.query(new Query(case_json_query));


        var zipRepo = {
      'EAST MC KEESPORT': [40.3843,-79.8161, 16],
      'ELIZABETH': [40.2986,-79.8561, 14],
      'GLASSPORT': [40.3183,-79.8935, 14],
      'CHALFANT': [40.4107,-79.8393, 16],
      'HOMESTEAD': [40.3917,-79.9032, 15],
      'MUNHALL': [40.3917,-79.9032, 14],
      'WEST MIFFLIN': [40.3875,-79.8961, 15],
      'MCKEESPORT': [40.3329,-79.8213, 13],
      'WHITE OAK': [40.3204,-79.7905, 16],
      'NORTH VERSAILLES': [40.3804,-79.8127, 13],
      'PITCAIRN': [40.4085,-79.7788, 16],
      'TURTLE CREEK': [40.4221,-79.8288, 15],
      'MONROEVILLE': [40.4201,-79.7875, 13],
      'WILMERDING': [40.3893,-79.8096, 16],
      'WALL': [40.3893,-79.8096, 15],
      'PITTSBURGH': [40.4376,-79.8326, 14],
      'IRWIN': [40.3533,-79.7782, 15],
      'BRADDOCK': [40.4021,-79.8779,15]
      };
    var zipView = zipRepo[propertyci];
        query.on("row", function (row, result) {
            result.addRow(row);
        });
        query.on("end", function (result) { 
          var filteredPropertyData = {
            "type": "FeatureCollection",
            "features": []
          };


          for (var i = 0; i < sendData.features.length - 1; i++) {        //Filters the map data for township chosen in filter
            var currentParcel = sendData.features[i];
            var sendDataPropertyci = currentParcel.properties.f4;

            if (sendDataPropertyci == propertyci) {
              filteredPropertyData.features.push(currentParcel)
          
            }
          }
            var data = result.rows[0].row_to_json;

            var joinDataJSON = {
        "type": "FeatureCollection",
        "features": []
        };                                  //final necessary Object to be sent to map
      var newPropertyData = filteredPropertyData.features;          //JSON containing property data
      var casesJSONObject = data.features;                    //JSON containing case data

      for (var i = 0; i < newPropertyData.length - 1; i++) {          //Runs through every property parcel
        var propertyPropID = newPropertyData[i].properties.f11;
        var propertyLoadIn = newPropertyData[i];
        var casesPerProperty = [];                      //case package
        var casesPresent = 0;                       //tracks case loading for map color coding
        for (var j=0; j < casesJSONObject.length - 1; j++) {        //Runs through every case
          var casesPropID = casesJSONObject[j].properties.f1;
          var caseID = casesJSONObject[j].properties.f2;
          if (propertyPropID == casesPropID) {              //If the propertyIDs match, add all case data to the package to be loaded in
            casesPerProperty.push(casesJSONObject[j].properties);   //creates individual case data based on case ID to load into case package
            casesPresent += 1;                      //increases casesPresent
        }
      }

        for (var k = 0; k < imageBank.length - 1; k++) {          //Searches image bank for corresponding property image to load
          var image = imageBank[k];

          if (propertyPropID == image) {
            propertyLoadIn.properties["image"] = ("/images/" + image + ".png");
          }

        }

        if (!propertyLoadIn.properties["image"]) {
          var usegroup = propertyLoadIn.properties.f3;
          var image = "";

          if (residential.includes(usegroup)) {
            image = "residential";
          } else if (commercial.includes(usegroup)) {
            image = "commercial";
          } else if (government.includes(usegroup)) {
            image = "government";
          } else if (agricultural.includes(usegroup)) {
            image = "agricultural";
          } else if (mixeduse.includes(usegroup)) {
            image = "mixeduse";
          } else if (school.includes(usegroup)) {
            image = "school";
          } else if (railroad.includes(usegroup)) {
            image = "railroad";
          } else if (parking.includes(usegroup)) {
            image = "parking";
          } else if (worship.includes(usegroup)) {
            image = "worship";
          } else if (utilities.includes(usegroup)) {
            image = "utilities";
          } else if (industrial.includes(usegroup)) {
            image = "industrial";
          } else if (lot.includes(usegroup)) {
            image = "lot";
          } else if (other.includes(usegroup) || usegroup == null) {
            image = "other"
          }

          propertyLoadIn.properties["image"] = ("/images/" + image + ".png");
        }

        propertyLoadIn.properties["cases"] = casesPerProperty;        //Loads the case package into the appropriate property
        propertyLoadIn.properties["countTest"] = i;             //TEST to number/label for finding!
        propertyLoadIn.properties.f9 = casesPresent;            //TEST ALTERS CASECOUNT, DELETE WHEN FINISHED!!!!!!
        joinDataJSON.features.push(propertyLoadIn);             //pushes the new full property JSON into the "features" array
    };


            res.render('map', {
                title: "CodeNforce Map",
                jsonData: joinDataJSON,
                zipView: zipView,
                htmlFormString: htmlFormString //Pass the form html string to the map view
            });
        });
    };
});