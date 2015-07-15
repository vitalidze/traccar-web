// set up locale
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
    results = regex.exec(location.search);
    return results === null ? null : decodeURIComponent(results[1].replace(/\+/g, " "));
}
//
function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
}
//
var locale = getParameterByName("locale");
if (locale === null) {
    locale = readCookie('GWT_LOCALE');
}
if (locale === null || locale == 'default') {
    locale = 'en';
}
i18n = i18n[locale];

// Initialize app
var myApp = new Framework7({
    modalTitle: '',
    swipeBackPage: false,
    template7Pages: true, // enable Template7 rendering for Ajax and Dynamic pages
    //Specify templates/pages data
    template7Data: {
        'page:login-screen' : { i18n : i18n }
    }
    });

// If we need to use custom DOM library, let's save it to $$ variable:
var $$ = Dom7;

// register template7 helpers

function formatDate(timestamp) {
    var date = new Date(timestamp);
    var month = date.getMonth() + 1;

    return date.getFullYear() + '-' +
        (month < 10 ? '0' : '') + month + '-' +
        (date.getDate() < 10 ? '0' : '') + date.getDate() + ' ' +
        (date.getHours() < 10 ? '0' : '') + date.getHours() + ':' +
        (date.getMinutes() < 10 ? '0' : '') + date.getMinutes() + ':' +
        (date.getSeconds() < 10 ? '0' : '') + date.getSeconds();
}

Template7.registerHelper('formatDate', formatDate);

function formatDouble(double, n) {
    return isNaN(double) ? "-" : double.toFixed(n)
}

Template7.registerHelper('formatDouble', function(double, options) {
    return formatDouble(double, options.hash.n);
});

function formatSpeed(speed) {
    if (isNaN(speed)) return "-";

    var factor = 1;
    var suffix = 'kn';

    if (appState.userSettings.speedUnit == 'kilometersPerHour') {
        factor = 1.852;
        suffix = 'km/h';
    } else if (appState.userSettings.speedUnit == 'milesPerHour') {
        factor = 1.150779;
        suffix = 'mph';
    }
    return (speed * factor).toFixed(2) + ' ' + suffix;
}

Template7.registerHelper('formatSpeed', formatSpeed);

// Add view
var mainView = myApp.addView('.view-main');

// Application state, user settings, devices, etc.
var appState = {};

// load application settings
callGet({ method: 'getApplicationSettings', success: function(appSettings) { appState.settings = appSettings; }});

// check authentication
callGet({ method: 'authenticated',
          success: function(data) {
              // save user and his settings to the application state
              appState.user = data;
              appState.userSettings = data.userSettings;
              mainView.loadPage({url: 'pages/map.html', animatePages: false});
          },
          error: function() { mainView.loadPage({url: 'pages/login.html', animatePages: false}); }
        });

myApp.onPageInit('login-screen', function (page) {
    myApp.params.swipePanel = false;

    // "page" variable contains all required information about loaded and initialized page
    var pageContainer = $$(page.container);

    pageContainer.find('#sign-in').on('click', function() {
        pageContainer.find('#form-login').trigger('submit');
    });

    var language = pageContainer.find('#language');
    var sel = language[0];
    var opts = sel.options;
    for(var j = 0; j < opts.length; j++) {
        if (opts[j].value == locale) {
            sel.selectedIndex = j;
            break;
        }
    }

    // set up redirect when language changes
    language.on('change', function() {
        var sel = pageContainer.find('#language')[0];
        var newLocale = sel.options[sel.selectedIndex].value;
        window.location = newLocale == locale ? '?' : ('?locale=' + newLocale);
    });

    // set up open desktop version action
    pageContainer.find('.open-desktop-version').on('click', function() {
        window.location = '/?' + (locale == null ? '' : 'locale=' + locale + '&') + 'nomobileredirect=1';
    });

    pageContainer.find('#form-login').on('submit', function(e) {
        e.preventDefault();

        // removes the iOS keyboard
        document.activeElement.blur();

        var username = pageContainer.find('input[name="username"]').val();
        var password = pageContainer.find('input[name="password"]').val();

        if (username.trim().length == 0 || password.trim().length == 0) {
            myApp.alert(i18n.user_name_and_password_must_not_be_empty);
            return false;
        }

        callPost({ method: 'login',
                   data: [username, password],
                   success: function(data) {
                       // save user and his settings to the application state
                       appState.user = data;
                       appState.userSettings = data.userSettings;

                       mainView.loadPage('pages/map.html');
                   },
                   error: function() { myApp.alert(i18n.user_name_or_password_is_invalid); },
                   showIndicator: true });

        return false;
    });
});

// button that opens sidebar menu
var OpenSideMenuControl = function(opt_options) {
    var options = opt_options || {};

    var anchor = document.createElement('a');
    anchor.href = '#open-side-menu';
    anchor.innerHTML = '&gt;';

    var handleOpenSideMenu = function(e) {
        e.preventDefault();
        myApp.openPanel('left');
    };

    anchor.addEventListener('click', handleOpenSideMenu, false);
    anchor.addEventListener('touchstart', handleOpenSideMenu, false);

    var element = document.createElement('div');
    element.className = 'button-open-side-menu ol-unselectable ol-has-tooltip';
    element.appendChild(anchor);

    ol.control.Control.call(this, {
        element: element,
        target: options.target
    });

};
ol.inherits(OpenSideMenuControl, ol.control.Control);

// initialize map when page ready
var map;
var vectorLayer;

myApp.onPageInit('map-screen', function(page) {
    myApp.params.swipePanel = 'left';

    loadDevices();

    var divOlMap = document.getElementById('map');

    var vectorSource = new ol.source.Vector();
    vectorLayer = new ol.layer.Vector({ source: vectorSource });

    var view = new ol.View();
    var controls = ol.control.defaults({ attribution: false });

    // set up layers
    var layers = [];
    if (appState.userSettings.mapType == "OSM") {
        var attribution = new ol.control.Attribution({
            collapsible: false
        });

        layers.push(new ol.layer.Tile({
            source: new ol.source.OSM()
        }));

        controls.extend([attribution])
    } else if (appState.userSettings.mapType.indexOf("BING_") == 0) {
        var style = 'Road';
        if (appState.userSettings.mapType == "BING_HYBRID") {
            style = 'AerialWithLabels';
        } else if (appState.userSettings.mapType == "BING_AERIAL") {
            style = 'Aerial';
        }

        layers.push(new ol.layer.Tile({
            source: new ol.source.BingMaps({
                key: 'AseEs0DLJhLlTNoxbNXu7DGsnnH4UoWuGue7-irwKkE3fffaClwc9q_Mr6AyHY8F',
                imagerySet: style
            })
        }));

        controls = ol.control.defaults({ attribution: true });
    } else if (appState.userSettings.mapType.indexOf("GOOGLE_") == 0) {
        var divGmap = document.createElement('div');
        divGmap.id = 'gmap';

        var divOlMap = document.createElement('div');
        divOlMap.id = 'olmap';

        $$('#map').append(divGmap);
        $$('#map').append(divOlMap);

        $$('#gmap').css({height: '100%', width: '100%'});
        $$('#olmap').css({height: '100%', width: '100%'});

        // make sure the view doesn't go beyond the 22 zoom levels of Google Maps
        view = new ol.View({ maxZoom: 21 });

        var mapTypeId = google.maps.MapTypeId.ROADMAP;
        if (appState.userSettings.mapType == "GOOGLE_HYBRID") {
            mapTypeId = google.maps.MapTypeId.HYBRID;
        } else if (appState.userSettings.mapType == "GOOGLE_SATELLITE") {
            mapTypeId = google.maps.MapTypeId.SATELLITE;
        } else if (appState.userSettings.mapType == "GOOGLE_TERRAIN") {
            mapTypeId = google.maps.MapTypeId.TERRAIN;
        }

        var gmap = new google.maps.Map(divGmap, {
            disableDefaultUI: true,
            keyboardShortcuts: false,
            draggable: false,
            disableDoubleClickZoom: true,
            scrollwheel: false,
            streetViewControl: false,
            mapTypeId: mapTypeId
        });

        view.on('change:center', function() {
            var center = ol.proj.transform(view.getCenter(), 'EPSG:3857', 'EPSG:4326');
            gmap.setCenter(new google.maps.LatLng(center[1], center[0]));
        });
        view.on('change:resolution', function() {
            gmap.setZoom(view.getZoom());
        });
    } else if (appState.userSettings.mapType.indexOf("MAPQUEST_") == 0) {
        var tiles = 'osm';
        if (appState.userSettings.mapType == "MAPQUEST_AERIAL") {
            tiles = 'sat';
        }
        layers.push(new ol.layer.Tile({
            source: new ol.source.MapQuest({layer: tiles})
        }));
    } else if (appState.userSettings.mapType == 'STAMEN_TONER') {
        layers.push(new ol.layer.Tile({
            source: new ol.source.Stamen({
                layer: 'toner'
            })
        }));
    }
    layers.push(vectorLayer);

    map = new ol.Map({
        target: divOlMap,
        layers: layers,
        view: view,
        controls: controls.extend([new OpenSideMenuControl()])
    });

    // set up map center and zoom
    map.getView().setCenter(createPoint(appState.userSettings.centerLongitude, appState.userSettings.centerLatitude));
    map.getView().setZoom(appState.userSettings.zoomLevel);

    if (appState.userSettings.mapType.indexOf("GOOGLE_") == 0) {
        divOlMap.parentNode.removeChild(divOlMap);
        gmap.controls[google.maps.ControlPosition.TOP_LEFT].push(divOlMap);
    }

    // start positions loading cycle
    loadPositions();
});

function loadPositions() {
    callGet({ method: 'getLatestPositions',
        success: function(positions) {
            // initialize latest positions in application state
            if (appState.latestPositions == undefined) {
                appState.latestPositions = [];
            }

            // remove old markers
            vectorLayer.getSource().clear();

            currentTime = new Date().getTime();

            for (var i = 0; i < positions.length; i++) {
                var position = positions[i];

                // save 'selected' state from previous position
                prevPosition = appState.latestPositions[position.device.id];
                if (prevPosition != undefined) {
                    position.selected = prevPosition.selected;
                    if (prevPosition.follow != undefined) {
                        position.follow = prevPosition.follow;
                    }
                }
                if (position.selected == undefined) {
                    position.selected = false;
                }

                // calculate whether we can consider device offline
                appState.latestPositions[position.device.id] = position;
                position.time = Date.parse(position.time);
                position.offline = currentTime - position.time > position.device.timeout * 1000;

                // draw marker on map
                drawMarker(position);

                // center if necessary
                if (position.follow != undefined && position.follow &&
                    (prevPosition == null || prevPosition.id != position.id)) {
                    catchPosition(position);
                }

                // update position details in side panel
                if (prevPosition != undefined && position.id != prevPosition.id) {
                    drawDeviceDetails(position.device.id, position);
                }
            }

            setTimeout(loadPositions, appState.settings.updateInterval);
        }
    });
}

function createPoint(lon, lat) {
    return ol.proj.transform([lon, lat], 'EPSG:4326', map.getView().getProjection());
}

function catchPosition(position) {
    var mapExtent = map.getView().calculateExtent(map.getSize());
    var point = createPoint(position.longitude, position.latitude);
    if (!ol.extent.containsCoordinate(mapExtent, point)) {
        map.getView().setCenter(point);
    }
}

function loadDevices() {
    var devicesList = $$('#devicesList');
    devicesList.html('');

    callGet({ method: 'getDevices',
        success: function(devices) {
            // save devices into application state
            appState.devices = devices;

            var source   = $$('#devices-list-template').html();
            var template = Template7.compile(source);

            devicesList.html(template({ devices: devices, i18n : i18n }));

            $$('.device-details-link').on('click', function(e) {
                var deviceId = e.currentTarget.id.substring(7);
                deviceId = deviceId.substring(0, deviceId.indexOf('-'));
                drawDeviceDetails(deviceId, appState.latestPositions[deviceId]);
                myApp.accordionToggle($$('#device-' + deviceId + '-list-item'));
            });

            // set up logout action
            $$('#logout').on('click', function() {
                callGet({ method: 'logout',
                    success: function() {
                        myApp.closePanel();
                        mainView.loadPage('pages/login.html');
                        appState = {};
                        $$('#map').html('');
                    },
                    error: function() {
                        myApp.alert(i18n.unexpected_error);
                        mainView.loadPage('pages/login.html');
                    }
                });
            });

            // set up open desktop version action
            $$('.open-desktop-version').on('click', function() {
                window.location = '/?' + (locale == null ? '' : 'locale=' + locale + '&') + 'nomobileredirect=1';
            });
        },
        error: function() {
            devicesList.append("Unable to load devices list");
        }
    });
}

function parseOther(position) {
    // parse 'other' field
    var xmlDoc;
    if (window.DOMParser)  {
        parser = new DOMParser();
        xmlDoc = parser.parseFromString(position.other, "text/xml");
        // Internet Explorer
    } else {
        xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
        xmlDoc.async = false;
        xmlDoc.loadXML(position.other);
    }

    var result;
    if (xmlDoc.documentElement == null) {
        result = null;
    } else {
        var device;
        for (var i = 0; i < appState.devices.length; i++) {
            if (appState.devices[i].id == position.device.id) {
                device = appState.devices[i];
                break;
            }
        }

        result = {};
        var nodes = xmlDoc.documentElement.childNodes;
        for (var i = 0; i < nodes.length; i++) {
            var name = nodes[i].nodeName;
            var valueText = nodes[i].textContent;
            if (valueText == null) {
                valueText = nodes[i].nodeValue;
            }
            var visible = true;
            for (var s = 0; s < device.sensors.length; s++) {
                var sensor = device.sensors[s];
                if (sensor.parameterName == name) {
                    visible = sensor.visible;
                    name = sensor.name;
                    if (sensor.intervals != null && !isNaN(valueText)) {
                        var intervals = JSON.parse(sensor.intervals);
                        if (intervals.length > 0) {
                            var value = valueText;
                            var valueText = null;
                            for (var j = 0; j < intervals.length; j++) {
                                if (valueText == null) {
                                    valueText = intervals[j].text;
                                }
                                if (value < intervals[j].value) {
                                    break;
                                }
                                valueText = intervals[j].text;
                            }
                        }
                    }
                    break;
                }
            }
            if (!visible) {
                continue;
            }
            result[name] = valueText;
        }
    }

    return result;
}

function drawDeviceDetails(deviceId, position) {
    var deviceDetails = $$('#device-' + deviceId + '-details');
    if (deviceDetails != undefined) {
        if (position == undefined) {
            deviceDetails.html('<div class="content-block">' + i18n.no_data_available + '</div>');
        } else {
            var otherXML = position.other;
            position.other = parseOther(position);

            var source   = $$('#device-details-template').html();
            var template = Template7.compile(source);
            position.i18n = i18n;

            deviceDetails.html(template(position));
            // restore back XML
            position.other = otherXML;

            // register 'select on map' function
            $$('#device-' + deviceId +'-select-on-map').on('click', function() {
                position = appState.latestPositions[deviceId];
                position.selected = true;
                drawMarker(position);
                // deselect previously selected position
                appState.latestPositions.forEach(function(pos) {
                    if (pos.id != position.id && pos.selected != undefined && pos.selected) {
                        pos.selected = false;
                        drawMarker(pos);
                    }
                });
                map.getView().setCenter(createPoint(position.longitude, position.latitude));
                myApp.closePanel();
            });

            // register 'follow' function
            $$('#device-' + deviceId + '-follow').on('click', function() {
                appState.latestPositions.forEach(function(pos) {
                    pos.follow = false;
                });
                position = appState.latestPositions[deviceId];
                position.follow = true;
                drawDeviceDetails(deviceId, position);
                catchPosition(position);
            });

            // register 'unfollow' function
            $$('#device-' + deviceId + '-unfollow').on('click', function() {
                position = appState.latestPositions[deviceId];
                position.follow = false;
                drawDeviceDetails(deviceId, position);
            });

            var drawSubject = function() {
                for (var i = 0; i < appState.devices.length; i++) {
                    if (appState.devices[i].id == deviceId) {
                        return appState.devices[i].name;
                    }
                }
                return "";
            };
            var drawCoordinatesText = function() {
                var p = appState.latestPositions[deviceId];
                var text =
                    i18n.time + ': ' + formatDate(p.time) + '\n' +
                    i18n.latitude + ': ' + formatDouble(p.latitude, 4) + '\n' +
                    i18n.longitude + ': ' + formatDouble(p.longitude, 4) + '\n' +
                    i18n.speed + ': ' + formatSpeed(p.speed) + '\n' +
                    i18n.course + ': ' + formatDouble(p.course, 2) + '\n';
                if (p.address != undefined && p.address != null) {
                    text += i18n.address + ': ' + encodeURIComponent(p.address) + '\n';
                }
                var other = parseOther(p);
                if (other != undefined && other != null) {
                    for (var k in other) {
                        text += k + ': ' + other[k] + '\n';
                    }
                }
                if (p.geoFences != undefined && p.geoFences != null) {
                    for (var i = 0; i < p.geoFences.length; i++) {
                        text += i18n.geo_fence + ': ' + p.geoFences[i].name + '\n';
                    }
                }
                return text;
            };
            var drawURL = function(sms) {
                var p = appState.latestPositions[deviceId];
                var amp = '&';
                if (sms || myApp.device.android && myApp.device.osVersion == '4.4.2') {
                    amp = '%26';
                }
                return 'http://www.openstreetmap.org/?mlat=' + p.latitude + amp + 'mlon=' + p.longitude;
            };

            // register 'send by email' function
            $$('#device-' + deviceId + '-send-email').on('click', function () {
                var target = this;
                var buttons = [
                    {
                        text: i18n.send_location_by_email,
                        onClick: function () {
                            window.location = 'mailto:?subject=' + encodeURIComponent(drawSubject()) + '&body=' + encodeURIComponent(drawCoordinatesText());
                        }
                    },
                    {
                        text: i18n.send_location_url_by_email,
                        onClick: function () {
                            window.location = 'mailto:?subject=' + encodeURIComponent(drawSubject()) + '&body=' + encodeURIComponent(drawURL(false));
                        }
                    }
                ];
                myApp.actions(target, buttons);
            });

            // register 'send by sms' function
            $$('#device-' + deviceId + '-send-sms').on('click', function() {
                var target = this;
                var buttons = [
                    {
                        text: i18n.send_location_by_sms,
                        onClick: function () {
                            window.location = 'sms:?body=' + encodeURIComponent(drawCoordinatesText());
                        }
                    },
                    {
                        text: i18n.send_location_url_by_sms,
                        onClick: function () {
                            window.location = 'sms:?body=' + encodeURIComponent(drawURL(true));
                        }
                    }
                ];
                myApp.actions(target, buttons);
            });

            // hide 'send by sms' on ios devices since 'body' is not supported in sms URLs there
            if (myApp.device.ios) {
                $$('#device-' + deviceId + '-send-sms').hide();
            }
        }
    }
}

function drawMarker(position) {
    var markerFeature = new ol.Feature({
        geometry: new ol.geom.Point(createPoint(position.longitude, position.latitude))
    });

    var markerStyle = new ol.style.Style({
        image: new ol.style.Icon({
            anchor: [0.5, 1.0],
            anchorXUnits: 'fraction',
            anchorYUnits: 'fraction',
            opacity: 0.9,
            src: getIconURL(position)
        }),
        text: new ol.style.Text({
            text: position.device.name,
            textAlign: 'center',
            offsetX: 0,
            offsetY: 8,
            font: '12px Arial',
            fill: new ol.style.Fill({
                color: '#0000FF'
            }),
            stroke: new ol.style.Stroke({
                color: '#fff',
                width: 3
            })
        })
    });

    markerFeature.setStyle(markerStyle);

    if (position.marker != undefined) {
        vectorLayer.getSource().removeFeature(position.marker);
    }
    position.marker = markerFeature;
    vectorLayer.getSource().addFeature(markerFeature);
}

function getIconURL(position) {
    if (position.device.icon == null) {
        return position.offline ? position.device.iconType.OFFLINE.urls[position.selected ? 1 : 0] :
                                  position.device.iconType.LATEST.urls[position.selected ? 1 : 0];
    } else {
        var pictureId;
        if (position.selected) {
            pictureId = position.device.icon.selectedIcon.id;
        } else {
            pictureId = position.offline ? position.device.icon.offlineIcon.id : position.device.icon.defaultIcon.id;
        }
        return '/traccar/p/' + pictureId;
    }
}

function callGet(options) {
    options.httpMethod = 'GET'
    invoke(options);
}

function callPost(options) {
    options.httpMethod = 'POST'
    invoke(options);
}

function invoke(options) {
    $$.ajax({
        url: '/traccar/rest/' + options.method,
        method: options.httpMethod,
        dataType: 'json',
        data: JSON.stringify(options.data),
        processData: false,
        start: function(xhr) {
            if (options.showIndicator != undefined && options.showIndicator) {
                myApp.showIndicator();
            }
        },
        success: options.success,
        complete: function(xhr) {
            if (options.showIndicator != undefined && options.showIndicator) {
                myApp.hideIndicator();
            }

            if (xhr.status != 200 && options.error != undefined) {
                options.error(xhr);
            }
        },
        error: options.error
    })
}

// Fix scrolling issue in accordion in side panel
if (myApp.device.ios) {
    $$(window).resize(function () {
        $$('.panel-left').css({height: $$(window).height() + 'px'});
    }).trigger('resize');
}