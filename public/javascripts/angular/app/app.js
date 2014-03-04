'use strict';

var scaxerciserApp = angular.module('scaxerciserApp', [
  'ui.router',
  'customDirectives',
  'authServices',
  'groupControllers',
  'groupServices',
  'groupMemberControllers',
  'groupMemberServices',
  'userControllers',
  'userServices'
]),
  permission;

angular.element(document).ready(function () {
  $.get('/api/users/detectPermission', function (data) {
    permission = data;
    angular.bootstrap(document, ['scaxerciserApp']);
  });
});

scaxerciserApp.partialsRoot = 'assets/javascripts/angular/app/partials/';

scaxerciserApp.config(['$stateProvider', '$urlRouterProvider', '$httpProvider',
  function ($stateProvider, $urlRouterProvider, $httpProvider) {
    $httpProvider.responseInterceptors.push('securityInterceptor');
    $urlRouterProvider.otherwise("/groups");
    $stateProvider
      .state('unauthorized', {
        url: '/unauthorized',
        views: {
          'main': {
            templateUrl: scaxerciserApp.partialsRoot + 'unauthorized.html'
          },
          'additional': {
            template: ''
          }
        }
      })
      .state('groups-reload', {
        controller: function ($state) {
          $state.go('groups-list');
        }
      })
      .state('groups-list', {
        url: "/groups",
        views: {
          'main': {
            templateUrl: scaxerciserApp.partialsRoot + 'groups-list.html',
            controller: 'GroupListCtrl'
          }
        }
      })
      .state('groups-list.new', {
        permission: 'Administrator',
        url: '/new',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'groups-list.new.html',
            controller: 'GroupCreationCtrl'
          }
        }
      })
      .state('groups-list.edit', {
        permission: 'Administrator',
        url: '/:groupId/edit',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'groups-list.edit.html',
            controller: 'GroupDetailCtrl'
          }
        }
      })
      .state('group-members-list', {
        permission: 'Administrator',
        url: '/groups/:groupId/members',
        views: {
          'main': {
            template: '='
          },
          'additional': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-members-list.html',
            controller: 'GroupMembersListCtrl'
          }
        }
      })
      .state('group-members-list.add', {
        permission: 'Administrator',
        url: '/add',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-members-list.add.html',
            controller: 'GroupMembersAddingCtrl'
          }
        }
      })
      .state('users-reload', {
        controller: function ($state) {
          $state.go('users-list');
        }
      })
      .state('users-list', {
        permission: 'Administrator',
        url: "/users",
        views: {
          'main': {
            templateUrl: scaxerciserApp.partialsRoot + 'users-list.html',
            controller: 'UserListCtrl'
          }
        }
      })
      .state('users-list.new', {
        permission: 'Administrator',
        url: '/new',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'users-list.new.html',
            controller: 'UserCreationCtrl'
          }
        }
      })
      .state('users-list.edit', {
        permission: 'Administrator',
        url: '/:id/edit',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'users-list.edit.html',
            controller: 'UserShortDetailCtrl'
          }
        }
      })
  }
]);

scaxerciserApp.provider('securityInterceptor', function () {
  this.$get = function ($location, $q) {
    return function (promise) {
      return promise.then(null, function (response) {
        if (response.status === 403 || response.status === 401) {
          $location.path('/unauthorized');
        }
        return $q.reject(response);
      });
    };
  };
});

scaxerciserApp.run(['$rootScope', '$state', 'Auth',
  function ($rootScope, $state, Auth) {
    Auth.setPermission(permission)

    $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
      if (typeof toState.permission !== "undefined" && !Auth.hasPermission(toState.permission)) {
        event.preventDefault();
        $state.transitionTo('unauthorized');
      }
    });
}]);

scaxerciserApp.dataTables = {
  languageSettings: {
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
  }
};