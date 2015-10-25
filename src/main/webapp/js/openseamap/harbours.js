/******************************************************************************
 Copyright 2008 - 2011 Xavier Le Bourdon, Christoph Böhme, Mitja Kleider

 This file originates from the Openstreetbugs project and was modified
 by Matthias Hoffmann and Olaf Hannemann for the OpenSeaMap project.

 This file is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This file is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this file.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/******************************************************************************
 This file implements the client-side of the harbour display.
 Version 0.1.5  03.10.2011
 ******************************************************************************/

// List of downloaded harbours:
var harbours = new Array();

var selectControlHarbour;

// Current state of the user interface. This is used
// to keep track which popups are displayed.
var harbour_state = 0;
var harbour_current_feature = null;
var popuptime=0;


// AJAX functions--------------------------------------------------------------------------------------------

// Request harbours from the server.
function make_harbour_request(params) {
    var url = "";
    for (var name in params) {
        url += (url.indexOf("?") > -1) ? "&" : "?";
        url += encodeURIComponent(name) + "=" + encodeURIComponent(params[name]);
    }
    // Example: http://dev.openseamap.org/website/map/api/getHarbours.php?b=43.16098&t=43.46375&l=16.23863&r=17.39219&ucid=0&maxSize=5&zoom=11
    var skgUrl="http://dev.openseamap.org/website/map/api/getHarbours.php"+url;

    var script = document.createElement("script");
    script.src = skgUrl;
    script.type = "text/javascript";
    document.body.appendChild(script);
}

// This function is called from the scripts that are returned on make_harbour_request calls.
function putHarbourMarker(id, lon, lat, names, link, type) {

    if (!harbour_exist(id,type)) {
        var name = names.split("-");
        if(type==-1) {
            type = determineHarbourType(name[0]);
        }
        var popupText = "<b>" + name[0] +"</b>&nbsp;&nbsp;&nbsp;&nbsp;<br/>";
        if (typeof name[1] != "undefined") {
            popupText += name[1];
        }
        if (typeof name[2] != "undefined") {
            popupText += "<br/><i>" + name[2] + "</i>";
        }
        if (link != '') {
            popupText += "<br/><br/><a href='" + link + "' target='blank'>" + linkTextSkipperGuide + "</a>";
        }
        popupText += "<br/><a href='http://openportguide.org/cgi-bin/weather/weather.pl/weather.png?var=meteogram&nx=614&ny=750&lat=" + lat + "&lon=" + lon + "&lang=" + language + "&unit=metric&label=" + convert2Locode(name[0]) + "' target='blank'>" + linkTextWeatherHarbour + "</a>";
        create_harbour_marker(lon2x(lon), lat2y(lat), popupText, type);

        var harbour = {id: id, name: names, lat: lat, lon: lon, type: type, feature: null};
        harbours.push(harbour);
    }
}


// Harbour management----------------------------------------------------------------------------------------

// Downloads new harbours from the server.
function refreshHarbours() {

    if (refreshHarbours.call_count == undefined) {
        refreshHarbours.call_count = 0;
    } else {
        ++refreshHarbours.call_count;
    }
    bounds = map.getExtent().toArray();
    var b = y2lat(bounds[1]).toFixed(5);
    var t = y2lat(bounds[3]).toFixed(5);
    var l = x2lon(bounds[0]).toFixed(5);
    var r = x2lon(bounds[2]).toFixed(5);

    var params = { "b": b, "t": t, "l": l, "r": r, "ucid": refreshHarbours.call_count, "maxSize":getHarbourVisibility(zoom), "zoom":zoom};
    make_harbour_request(params);
}

// Check if a harbour has been downloaded already.
function harbour_exist(id,type) {
    for (var i in harbours) {
        if (harbours[i].id == id && (harbours[i].type == type
            || type==-1 && (harbours[i].type==5
                ||harbours[i].type==6 ))) {
            return true;
        }
    }

    return false;
}

function isWPI(type) {
    /**********************************
    1 = L
    2 = M
    3 = S
    4 = V (Very small)
    5 = Marina (representative skipperguide)
    6 = Anchorage (representative skipperguide)
    7 = other descr. skipperguide
    ***********************************/
    if(type<=4) {
        return true;
    }
    return false;
}

function determineHarbourType(myName){
    for (var i in harbours) {
        var otherName = harbours[i].name.split("-");
        if(myName==otherName[0]) {
            return 6;
        }
    }
    return 5;
}

function getHarbourVisibility(zoom){
    var maxType=1;
    if(zoom>=7) {
        maxType=2;
    }
    if(zoom>=8) {
        maxType=3;
    }
    if(zoom>=9) {
        maxType=4;
    }
    if(zoom>=10) {
        maxType=6;
    }

 return maxType;
}

// Return a harbour description from the list of downloaded harbours.
function get_harbour(id,type) {
    for (var i in harbours) {
        if (harbours[i].id == id && harbours[i].type == type) {
            return harbours[i];
        }
    }

    return '';
}

function create_harbour_marker(x, y, popupText, type) {
    var layer_poi_icon_style = OpenLayers.Util.extend({});
    var harbour_marker = new OpenLayers.Geometry.Point(x, y);
    var maxType = getHarbourVisibility(zoom);

    if(type <= maxType){
        if (isWPI(type)) {
            layer_poi_icon_style.externalGraphic = './resources/places/harbour_32.png';
        } else {
            if (type == 6) {
                layer_poi_icon_style.externalGraphic = './resources/places/anchorage_32.png';
            } else {
                layer_poi_icon_style.externalGraphic = './resources/places/marina_32.png';
            }
        }
        layer_poi_icon_style.graphicWidth = 24;
        layer_poi_icon_style.graphicHeight = 24;

        if(zoom>=5 ||refreshHarbours.call_count>0) {
            var pointFeature = new OpenLayers.Feature.Vector(harbour_marker, null, layer_poi_icon_style);
            pointFeature.popupClass = OpenLayers.Class(OpenLayers.Popup.FramedCloud);
            pointFeature.data.popupContentHTML = popupText;
            layer_pois.addFeatures([pointFeature]);
        }
    }
}
