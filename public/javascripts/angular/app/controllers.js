'use strict';

var groupControllers = angular.module('groupControllers', []);

groupControllers.controller('GroupListCtrl', ['$scope', 'Group',
  function($scope, Group) {
    $scope.groups = Group.query();
  }]);

groupControllers.controller('GroupDetailCtrl', ['$scope', '$routeParams', 'Group',
  function($scope, $routeParams, Group) {
    $scope.group = Group.get({groupId: $routeParams.groupId}, function(group) {
        console.log(group);
    });
  }]);
