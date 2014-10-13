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

// check authentication
callGet({ method: 'authenticated',
          success: function() { mainView.loadPage({url: 'pages/map.html', animatePages: false}); },
          error: function() { mainView.loadPage({url: 'pages/login.html', animatePages: false}); }
        });

// set up logout action
$$('#logout').on('click', function() {
    callGet({ method: 'logout',
        success: function() { myApp.closePanel(); mainView.loadPage('pages/login.html');},
        error: function() { myApp.alert("Unexpected error"); mainView.loadPage('pages/login.html'); }})
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
                   success: function() { mainView.loadPage('pages/map.html'); },
                   error: function() { myApp.alert("User name or password is invalid"); },
                   showIndicator: true });
    });
});

myApp.onPageInit('map-screen', function(page) {
    myApp.params.swipePanel = 'left';

    loadDevices();
});

function loadDevices() {
    var devicesList = $$('#devicesList');
    devicesList.html('');

    callGet({ method: 'getDevices',
        success: function(data) {
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