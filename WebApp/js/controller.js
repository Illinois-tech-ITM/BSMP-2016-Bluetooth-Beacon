var app = angular.module('bluemap', []);

app.controller('navbarCtrl', function($scope){

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