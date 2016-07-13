var app = angular.module('bluemap', ['ngRoute']);

app.config(['$locationProvider', '$routeProvider', function($locationProvider, $routeProvider){
	$routeProvider.
		when('/', {
			templateUrl: 'views/login.html',
			controller: 'loginCtrl'
		}).
		when('/form', {
			templateUrl: 'views/form.html'
		}).
		otherwise({
			redirectTo: '/'
		});
		$locationProvider.html5Mode(true);
}]);

app.service('sessionInfo', function(){
    var session = null;
    return {
        getSession: function() {
            return session;
        },
        setSession: function(s){
            session = s;
        }
    }
});

app.controller('loginCtrl', function($scope, $location, sessionInfo){
	$scope.login = function(username){
		console.log("bla");
		sessionInfo.setSession(username);
		$location.path("form");
	}
});

app.controller('navbarCtrl', function($scope, $location, sessionInfo){
	$scope.session = {};
	$scope.session.name = sessionInfo.getSession();

	$scope.logout = function(){
		$location.path("index");
	};
});

app.controller('mainCtrl', function($scope, $http){
	$scope.addingTranslation = false;
	$scope.selectedDevice = null;
	$scope.newDevice = {};
	$scope.newDevice.translations = [];

	$scope.devices = [{
		key: "DVC0000",
		name: "Mona Lisa"
	}, {
		key: "DVC0001",
		name: "The Scream"
	}];

	$scope.onDeviceClick = function(device){
		$scope.selectedDevice = device;
		if (!$scope.selectedDevice.translations) {
			$scope.selectedDevice.translations = [];
		}
		$scope.addingTranslation = false;
	}
	$scope.addNewDevice = function(){
		$scope.selectedDevice = null;
	}
	$scope.saveNewDevice = function(newDevice){
		if (!newDevice || !newDevice.key){
			return;
		}
		$scope.devices.push(newDevice);
		$scope.newDevice = {};
		$scope.newDevice.translations = [];
	}
	$scope.addNewTranslation = function(device){
		$scope.addingTranslation = true;

	}
	$scope.saveNewTranslation = function(translation){
		if (!translation) {
			$scope.addingTranslation = false;
			return;
		}
		if (!$scope.selectedDevice){
			$scope.newDevice.translations.push(translation);
		} else {
			$scope.selectedDevice.translations.push(translation);
		}
		$scope.translation =  null;
		$scope.addingTranslation = false;
	}
});