// Initialize app
var myApp = new Framework7({
    modalTitle: '',
    swipeBackPage: false,
    swipePanel: 'left',
    swipePanelActiveArea: 10
});

// If we need to use custom DOM library, let's save it to $$ variable:
var $$ = Dom7;

// register handlebars helpers
Handlebars.registerHelper('formatDate', function(timestamp) {
    var date = new Date(timestamp);

    return date.getFullYear() + '-' +
        (date.getMonth() < 10 ? '0' : '') + date.getMonth() + '-' +
        (date.getDay() < 10 ? '0' : '') + date.getDay() + ' ' +
        (date.getHours() < 10 ? '0' : '') + date.getHours() + ':' +
        (date.getMinutes() < 10 ? '0' : '') + date.getMinutes() + ':' +
        (date.getSeconds() < 10 ? '0' : '') + date.getSeconds();
});

Handlebars.registerHelper('formatDouble', function(double, n) {
    return double.toFixed(n);
});

Handlebars.registerHelper('formatSpeed', function(speed) {
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
});

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
            myApp.alert("Unexpected error");
            mainView.loadPage('pages/login.html');
        }
    });
});

myApp.onPageInit('login-screen', function (page) {
    myApp.params.swipePanel = false;

    // "page" variable contains all required information about loaded and initialized page
    var pageContainer = $$(page.container);

    pageContainer.find('#sign-in').on('click', function() {
        var username = pageContainer.find('input[name="username"]').val();
        var password = pageContainer.find('input[name="password"]').val();

        if (username.trim().length == 0 || password.trim().length == 0) {
            myApp.alert("User name and password must not be empty");
            return;
        }

        callPost({ method: 'login',
                   data: [username, password],
                   success: function(data) {
                       // save user and his settings to the application state
                       appState.user = data;
                       appState.userSettings = data.userSettings;

                       mainView.loadPage('pages/map.html');
                   },
                   error: function() { myApp.alert("User name or password is invalid"); },
                   showIndicator: true });
    });
});

// initialize map when page ready
var map;
var vectorLayer;

myApp.onPageInit('map-screen', function(page) {
    myApp.params.swipePanel = 'left';

    loadDevices();

    var attribution = new ol.control.Attribution({
        collapsible: false
    });

    var vectorSource = new ol.source.Vector();
    vectorLayer = new ol.layer.Vector({ source: vectorSource });

    map = new ol.Map({
        target: 'map',
        layers: [
            new ol.layer.Tile({
                source: new ol.source.OSM()
            }),
            vectorLayer
        ],
        controls: ol.control.defaults({ attribution: false }).extend([attribution])
    });

    // set up map center and zoom
    map.setView(new ol.View({
        center: createPoint(appState.userSettings.centerLongitude, appState.userSettings.centerLatitude),
        zoom: appState.userSettings.zoomLevel
    }));

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

            for (i = 0; i < positions.length; i++) {
                var position = positions[i];

                // save 'selected' state from previous position
                prevPosition = appState.latestPositions[position.device.id];
                if (prevPosition != undefined) {
                    position.selected = prevPosition.selected;
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

                // update position details in side panel
                if (prevPosition != undefined && position.id != prevPosition.id) {
                    drawDeviceDetails(position);
                }
            }

            setTimeout(loadPositions, appState.settings.updateInterval);
        }
    });
}

function createPoint(lon, lat) {
    return ol.proj.transform([lon, lat], 'EPSG:4326', map.getView().getProjection());
}

function loadDevices() {
    var devicesList = $$('#devicesList');
    devicesList.html('');

    callGet({ method: 'getDevices',
        success: function(devices) {
            // save devices into application state
            appState.devices = devices;

            var source   = $$('#devices-list-template').html();
            var template = Handlebars.compile(source);

            devicesList.html(template({devices: devices}));

            $$('.device-details-link').on('click', function(e) {
                var deviceId = e.currentTarget.id.substring(7);
                deviceId = deviceId.substring(0, deviceId.indexOf('-'));
                drawDeviceDetails(appState.latestPositions[deviceId]);
                myApp.accordionToggle($$('#device-' + deviceId + '-list-item'));
            });
        },
        error: function() {
            devicesList.append("Unable to load devices list");
        }
    });
}

function drawDeviceDetails(position) {
    var deviceId = position.device.id;
    var deviceDetails = $$('#device-' + deviceId + '-details');
    if (deviceDetails != undefined) {
        if (position == undefined) {
            deviceDetails.html('<p>No data available</p>');
        } else {
            var source   = $$('#device-details-template').html();
            var template = Handlebars.compile(source);

            deviceDetails.html(template(position));

            $$('#device-' + deviceId +'-select-on-map').on('click', function() {
                position = appState.latestPositions[deviceId];
                position.selected = true;
                drawMarker(position);
                map.getView().setCenter(createPoint(position.longitude, position.latitude));
                myApp.closePanel();
            });
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
            opacity: 0.75,
            src:
                position.selected ? 'http://cdnjs.cloudflare.com/ajax/libs/openlayers/2.13.1/img/marker-green.png' :
                position.offline ? '/img/marker-white.png' : 'http://cdnjs.cloudflare.com/ajax/libs/openlayers/2.13.1/img/marker.png'
        }),
        text: new ol.style.Text({
            text: position.device.name,
            textAlign: 'center',
            offsetX: 0,
            offsetY: 8,
            font: '12px',
            fill: new ol.style.Fill({
                color: '#0000FF'
            }),
            stroke: new ol.style.Stroke({
                color: '#fff',
                width: 2
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