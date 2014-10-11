// Initialize app
var myApp = new Framework7({
    // Hide and show indicator during ajax requests
    modalTitle: ''
});

// If we need to use custom DOM library, let's save it to $$ variable:
var $$ = Dom7;

// Add view
var mainView = myApp.addView('.view-main');

if (true) {
    mainView.loadPage({url: 'pages/login.html', animatePages: false});
}

// In page callbacks:
myApp.onPageInit('login-screen', function (page) {
    // "page" variable contains all required information about loaded and initialized page
    var pageContainer = $$(page.container);

    pageContainer.find('#sign-in').on('click', function() {
        var username = pageContainer.find('input[name="username"]').val();
        var password = pageContainer.find('input[name="password"]').val();

        if (username.trim().length == 0 || password.trim().length == 0) {
            myApp.alert("User name and password must not be empty");
            return;
        }

        $$.ajax({
            url: '/login',
            method: 'POST',
            dataType: 'json',
            data: { username: username, password: password },
            success: function(xhr) {
                myApp.hideIndicator();
//                console.log('success!');
            },
            complete: function(xhr) {
                myApp.hideIndicator();
                if (xhr.status != 200) {
                    myApp.alert("User name or password is invalid")
                }
                console.log(xhr.status);
            },
            error: function(xhr) {
                myApp.hideIndicator();
            }
        });
    });
});