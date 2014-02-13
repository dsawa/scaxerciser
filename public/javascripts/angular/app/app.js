'use strict';

var scaxerciserApp = angular.module('scaxerciserApp', [
  //  'ngRoute',
  'ui.router',
  'groupControllers',
  'groupServices'
]);

scaxerciserApp.partialsRoot = 'assets/javascripts/angular/app/partials/'

//scaxerciserApp.config(['$routeProvider',
//  function($routeProvider) {
//    $routeProvider.
//      when('/groups', {
//        templateUrl: scaxerciserApp.partialsRoot + 'group-list.html',
//        controller: 'GroupListCtrl'
//      }).
//      when('/groups/:groupId', {
//        templateUrl: scaxerciserApp.partialsRoot + 'group-detail.html',
//        controller: 'GroupDetailCtrl'
//      }).
//      otherwise({
//        redirectTo: '/'
//      });
//  }]);

scaxerciserApp.config(['$stateProvider', '$urlRouterProvider',
  function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise("/groups");
    $stateProvider
      .state('groups', {
        url: "/groups",
        views: {
          'main': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-list.html',
            controller: 'GroupListCtrl'
          }
        }
      })
  }
]);