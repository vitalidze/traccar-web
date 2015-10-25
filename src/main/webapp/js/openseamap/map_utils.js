/******************************************************************************
 Javascript OpenLayers map_utils
 author Olaf Hannemann
 license GPL V3
 version 0.1.3
 date 11.09.2011

 This file is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This file is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License (http://www.gnu.org/licenses/) for more details.
 ******************************************************************************/

// Constants-------------------------------------------------------------------
var earthRadius = 6371.221; //Km

// Projections-----------------------------------------------------------------
var projMerc = new OpenLayers.Projection("EPSG:900913");
var proj4326 = new OpenLayers.Projection("EPSG:4326");

// Zoom------------------------------------------------------------------------
var zoomUnits= [
    30*3600,    // zoom=0
    30*3600,
    15*3600,
    10*3600,
    5*3600,
    5*3600,
    2*3600,
    1*3600,
    30*60,
    20*60,
    10*60,      // zoom=10
    5*60,
    2*60,
    1*60,
    30,
    30,
    12,
    6,
    6,
    3           // zoom=19
];

// Transformations-------------------------------------------------------------
function Lon2Merc(value) {
    return 20037508.34 * value / 180;
}

function Lat2Merc(value) {
    var PI = 3.14159265358979323846;
    lat = Math.log(Math.tan( (90 + value) * PI / 360)) / (PI / 180);
    return 20037508.34 * value / 180;
}

function plusfacteur(a) {
    return a * (20037508.34 / 180);
}

function moinsfacteur(a) {
    return a / (20037508.34 / 180);
}

function y2lat(a) {
    return 180/Math.PI * (2 * Math.atan(Math.exp(moinsfacteur(a)*Math.PI/180)) - Math.PI/2);
}

function lat2y(a) {
    return plusfacteur(180/Math.PI * Math.log(Math.tan(Math.PI/4+a*(Math.PI/180)/2)));
}

function x2lon(a) {
    return moinsfacteur(a);
}

function lon2x(a) {
    return plusfacteur(a);
}

function km2nm(a) {
    return a * 0.540;
}

function lat2DegreeMinute(buffLat) {
    var ns = buffLat >= 0 ? 'N' : 'S';
    var lat_m = Math.abs(buffLat*60).toFixed(3);
    var lat_d = Math.floor (lat_m/60);
    lat_m -= lat_d*60;

    return ns + lat_d + "°" + format2FixedLenght(lat_m, 6, 3) + "'";
}

function lon2DegreeMinute(buffLon) {
    var we = buffLon >= 0 ? 'E' : 'W';
    var lon_m = Math.abs(buffLon*60).toFixed(3);
    var lon_d = Math.floor (lon_m/60);
    lon_m -= lon_d*60;

    return we + lon_d + "°" + format2FixedLenght(lon_m, 6, 3) + "'";
}

function lonLatToMercator(ll) {
    return new OpenLayers.LonLat(lon2x(ll.lon), lat2y(ll.lat));
}

// shorten coordinate to 5 digits in decimal fraction
function shorter_coord(coord) {
    return Math.round(coord*100000)/100000;
}


// Common utilities------------------------------------------------------------
function jumpTo(lon, lat, zoom) {
    var lonlat = new OpenLayers.LonLat(lon, lat);
    lonlat.transform(proj4326, projMerc);
    map.setCenter(lonlat, zoom);
}

function getTileURL(bounds) {
    var res = this.map.getResolution();
    var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
    var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
    var z = this.map.getZoom();
    var limit = Math.pow(2, z);
    if (y < 0 || y >= limit) {
        return null;
    } else {
        x = ((x % limit) + limit) % limit;
        url = this.url;
        path= z + "/" + x + "/" + y + "." + this.type;
        if (url instanceof Array) {
            url = this.selectUrl(path, url);
        }
        return url+path;
    }
}

function getTileURLAsParams(bounds) {
    var res = this.map.getResolution();
    var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
    var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
    var z = this.map.getZoom();

    var limit = Math.pow(2, z);

    if (y < 0 || y >= limit) {
        return OpenLayers.Util.getImagesLocation() + "404.png";
    } else {
        x = ((x % limit) + limit) % limit;
        return this.url + "x=" + x + "&y=" + y + "&z=" + z;
    }
}

function addMarker(layer, buffLon, buffLat, popupContentHTML) {
    var pos = new OpenLayers.LonLat(buffLon, buffLat);
    pos.transform(proj4326, projMerc);
    var mFeature = new OpenLayers.Feature(layer, pos);
    mFeature.closeBox = true;
    mFeature.popupClass = OpenLayers.Class(OpenLayers.Popup.FramedCloud, {minSize: new OpenLayers.Size(260, 100) } );
    mFeature.data.popupContentHTML = popupContentHTML;

    var size = new OpenLayers.Size(32,32);
    var offset = new OpenLayers.Pixel(-(size.w/2), -size.h);
    var icon = new OpenLayers.Icon('resources/icons/Needle_Red_32.png', size, offset);

    var marker = new OpenLayers.Marker(pos, icon);
    marker.feature = mFeature;

    var markerClick = function(evt) {
        if (this.popup == null) {
            this.popup = this.createPopup(this.closeBox);
            map.addPopup(this.popup);
            this.popup.show();
        } else {
            this.popup.toggle();
        }
        OpenLayers.Event.stop(evt);
    };


    layer.addMarker(marker);
    if (popupContentHTML != -1) {
        marker.events.register("mousedown", mFeature, markerClick);
        map.addPopup(mFeature.createPopup(mFeature.closeBox));
    }
}

// Vector layer utilities------------------------------------------------------
function getLineSegments(line) {
    var numSegments = line.components.length - 1;
    var segments = new Array(numSegments), point1, point2;
    for(var i = 0; i < numSegments; ++i) {
        point1 = line.components[i];
        point2 = line.components[i + 1];
        segments[i] = {
            x1: point1.x,
            y1: point1.y,
            x2: point2.x,
            y2: point2.y
        };
    }

    return segments;
}

function getLineSegmentLength(segment) {
    return Math.sqrt( Math.pow((segment.x2 -segment.x1),2) + Math.pow((segment.y2 -segment.y1),2) );
}

function getDistance(latA, latB, lonA, lonB) {
    var dLat = OpenLayers.Util.rad(latB - latA);
    var dLon = OpenLayers.Util.rad(lonB - lonA);
    var lat1 = OpenLayers.Util.rad(latA);
    var lat2 = OpenLayers.Util.rad(latB);

    var a = Math.PI/2-lat2;
    var b = Math.PI/2-lat1;
    var c = Math.acos(Math.cos(a)*Math.cos(b)+Math.sin(a)*Math.sin(b)*Math.cos(dLon));
    var d = km2nm(earthRadius * c);

    return d;
}

function getBearing(latA, latB, lonA, lonB) {
    var dLat = OpenLayers.Util.rad(latB-latA);
    var dLon = OpenLayers.Util.rad(lonB-lonA);
    var lat1 = OpenLayers.Util.rad(latA);
    var lat2 = OpenLayers.Util.rad(latB);

    var y = Math.sin(dLon) * Math.cos(lat2);
    var x = Math.cos(lat1)*Math.sin(lat2) -
        Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
    var brng = OpenLayers.Util.deg(Math.atan2(y, x));

    return (brng + 360) % 360;
}
