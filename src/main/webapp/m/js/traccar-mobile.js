// Initialize app
var myApp = new Framework7({
    modalTitle: '',
    swipeBackPage: false,
    swipePanel: 'left'
});

// If we need to use custom DOM library, let's save it to $$ variable:
var $$ = Dom7;

// Add view
var mainView = myApp.addView('.view-main');

// Application state, user settings, devices, etc.
var appState = {};

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

myApp.onPageInit('map-screen', function(page) {
    myApp.params.swipePanel = 'left';

    loadDevices();

    map = new ol.Map({
        target: 'map',
        layers: [
            new ol.layer.Tile({
                source: new ol.source.OSM()
            })
        ]
    });

    // set up map center and zoom
    map.setView(new ol.View({
        center: createPoint(appState.userSettings.centerLongitude, appState.userSettings.centerLatitude),
        zoom: appState.userSettings.zoomLevel
    }));
});

function createPoint(lon, lat) {
    return ol.proj.transform([lon, lat], 'EPSG:4326', map.getView().getProjection());
}

function loadDevices() {
    var devicesList = $$('#devicesList');
    devicesList.html('');

    callGet({ method: 'getDevices',
        success: function(data) {
            // save devices into application state
            appState.devices = data;

            var listHTML = '<div class="list-block">';
            listHTML += '<div class="list-block-label">Devices</div>';
            listHTML += '<ul>';
            for (var i = 0; i < data.length; i++) {
                listHTML += '<li class="item-content">';
                listHTML += '<div class="item-inner"><div class="item-title">' + data[i].name + '</div></div>';
                listHTML += '</li>';
            }
            listHTML += '</ul>';
            listHTML += '</div>';

            devicesList.append(listHTML);
        },
        error: function() {
            devicesList.append("Unable to load devices list");
        }
    });
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

            if (xhr.status != 200) {
                options.error(xhr);
            }
        },
        error: options.error
    })
}