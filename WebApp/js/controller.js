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

app.controller('loginCtrl', function($scope, $location, $http, sessionInfo){
	$scope.login = function(username, password){
		var config = {
			
			headers:  {
				"Accept": "application/json",
				"Content-Type": "application/json",
        		"name": username,
        		"password" : password
    		}
		};
		
		var successCallback = function(res){
			console.log(res.headers());
			var session = {
				"username": username,
				"sessionId": res.headers().sessionid
			}
			sessionInfo.setSession(session);
			$location.path("form");
		};
		
		var errorCallback = function(err){
			alert(err);
		};
		
		$http.get('https://floating-journey-50760.herokuapp.com/authenticate', config).then(successCallback, errorCallback);
		
	};
	
	$scope.newAcc = function(name, pass){
		var config = {
			headers:  {
				"Accept": "application/json",
				"Content-Type": "application/json"
    		}
		};
		
		var data = {
			"name": name,
			"password": pass
		}
		
		var successCallback = function(res){
			$scope.login(name, pass);
			console.log(res);
		};
		
		var errorCallback = function(err){
			alert(err);
		};
		
		$http.post('https://floating-journey-50760.herokuapp.com/addUser', data, config).then(successCallback, errorCallback);
	}
});

app.controller('navbarCtrl', function($scope, $location, sessionInfo){
	$scope.session = sessionInfo.getSession();

	$scope.logout = function(){
		$location.path("index");
	};
});

app.controller('mainCtrl', function($scope, $http, sessionInfo){
	$scope.addingTranslation = false;
	$scope.editingTranslation = false;
	$scope.editingDevice = false;
	$scope.selectedDevice = null;
	$scope.newDevice = {};
	$scope.newDevice.translations = [];
	$scope.devices = [];
	$scope.session = sessionInfo.getSession();
	
	var config = {
		headers:  {
    		"sessionId": $scope.session.sessionId
		}
	};
	
	console.log(config);
	
	var successCallback = function(res){
		console.log(res);
		for (var i=0;i<res.data.length;i++)
			$scope.devices.push(res.data[i]);
	};
	
	var errorCallback = function(err){
		alert(err);
	};

	$http.get('https://floating-journey-50760.herokuapp.com/getArtworksByUser', config).then(successCallback, errorCallback);

	$scope.onDeviceClick = function(device){
		console.log(device);
		$scope.selectedDevice = device;
		if (!$scope.selectedDevice.translations) {
			$scope.selectedDevice.translations = [];
		}
		$scope.addingTranslation = false;
		$scope.editingDevice = false;
	}
	$scope.addNewDevice = function(){
		$scope.selectedDevice = null;
		$scope.addingTranslation = false;
		$scope.editingDevice = false;
	}
	$scope.saveNewDevice = function(newDevice){
		if (!newDevice || !newDevice.dvcKey){
			return;
		}
		
		var config = {
			headers:  {
	    		"sessionId": $scope.session.sessionId
			}
		};
		
		var successCallback = function(res){
			console.log(res);
			$scope.devices.push(newDevice);
		};
		
		var errorCallback = function(err){
			alert(err);
		};
	
		$http.post('https://floating-journey-50760.herokuapp.com/addOrUpdateArtwork', newDevice, config).then(successCallback, errorCallback);
		$scope.newDevice = {};
		$scope.newDevice.translations = [];
	}
	
	$scope.addNewTranslation = function(device){
		$scope.addingTranslation = true;

	}
	
	$scope.saveNewTranslation = function(translation){
		var config = {
			headers:  {
	    		"sessionId": $scope.session.sessionId
			}
		};
		
		if (!translation) {
			$scope.addingTranslation = false;
			return;
		}
		if (!$scope.selectedDevice){
			$scope.newDevice.translations.push(translation);
		} else if (!$scope.editingTranslation) {
			$scope.selectedDevice.translations.push(translation);
			$http.post('https://floating-journey-50760.herokuapp.com/addOrUpdateArtwork', $scope.selectedDevice, config).then(function(res){
				console.log(res);
			}, function(err){
				alert(err);
			});
		} else {
			var index = $scope.selectedDevice.translations.indexOf($scope.oldTranslation);
			$scope.selectedDevice.translations[index] = translation;
			$http.post('https://floating-journey-50760.herokuapp.com/addOrUpdateArtwork', $scope.selectedDevice, config).then(function(res){
				console.log(res);
			}, function(err){
				alert(err);
			});
		}
		$scope.translation =  null;
		$scope.addingTranslation = false;
	}
	
	$scope.onTranslationClick = function(translation){
		$scope.translation = translation;
		$scope.oldTranslation = translation;
		$scope.addingTranslation = true;
		$scope.editingTranslation = true;
	}
	
	$scope.saveEditedDevice = function(selectedDevice){
		$http.post('https://floating-journey-50760.herokuapp.com/addOrUpdateArtwork', selectedDevice, config).then(function(res){
			console.log(res);
			for (var i=0;i<$scope.devices.length;i++){
				if ($scope.devices[i].dvcKey === selectedDevice.dvcKey){
					$scope.devices[i] = selectedDevice;
				}
			}
			$scope.editingDevice = false;
		}, function(err){
			alert(err);
		});
	}
	
});