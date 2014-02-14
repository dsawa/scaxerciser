'use strict';

var scaxerciserApp = angular.module('scaxerciserApp', [
  'ui.router',
  'groupControllers',
  'groupServices'
]);

scaxerciserApp.partialsRoot = 'assets/javascripts/angular/app/partials/'

scaxerciserApp.config(['$stateProvider', '$urlRouterProvider',
  function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise("/groups");
    $stateProvider
      .state('groups-list', {
        url: "/groups",
        views: {
          'main': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-list.html',
            controller: 'GroupListCtrl'
          }
        }
      })
      .state('groups-list.new', {
              url: '/new',
              views: {
                '': {
                  templateUrl: scaxerciserApp.partialsRoot + 'group-list.new.html',
                  controller: function () {
                  }
                }
              }
            })
      .state('groups-list.edit', {
        url: '/:groupId/edit',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-list.edit.html',
            controller: 'GroupDetailCtrl'
          }
        }
      })
      .state('groups-groupId-users', {
        url: '/groups/:groupId/users',
        views: {
          'main': { template: '=' },
          'right': {
            templateUrl: scaxerciserApp.partialsRoot + 'groups-groupId-users.html',
            controller: function($stateParams) {
              console.log('Getting users for group ' + $stateParams.groupId)
            }
          }
        }
      })
  }
]);