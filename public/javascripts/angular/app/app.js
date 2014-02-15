'use strict';

var scaxerciserApp = angular.module('scaxerciserApp', [
  'ui.router',
  'groupControllers',
  'groupServices'
]);

scaxerciserApp.partialsRoot = 'assets/javascripts/angular/app/partials/';

scaxerciserApp.config(['$stateProvider', '$urlRouterProvider',
  function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise("/groups");
    $stateProvider
      .state('groups-reload', {
        controller: function ($state) {
          $state.go('groups-list');
        }
      })
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
                  controller: 'GroupCreationCtrl'
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
          'additional': {
            templateUrl: scaxerciserApp.partialsRoot + 'groups-groupId-users.html',
            controller: function($stateParams) {
              console.log('Getting users for group ' + $stateParams.groupId)
            }
          }
        }
      })
  }
]);

scaxerciserApp.dataTables = {};
scaxerciserApp.dataTables.languageSettings = {
  sProcessing: "Proszę czekać...",
  sLengthMenu: "Pokaż _MENU_",
  sZeroRecords: "Brak danych.",
  sInfo: "Pozycje od _START_ do _END_ z _TOTAL_ łącznie",
  sInfoEmpty: "Pozycji 0 z 0 dostępnych",
  sInfoFiltered: "(filtrowanie spośród _MAX_ dostępnych pozycji)",
  sInfoPostFix: "",
  sSearch: "Szukaj:  ",
  sUrl: "",
  oPaginate: {
    sFirst: "Pierwsza",
    sPrevious: "Poprzednia",
    sNext: "Następna",
    sLast: "Ostatnia"
  }
};