'use strict';

var scaxerciserApp = angular.module('scaxerciserApp', [
    'ui.router',
    'ngTable',
    'ngUpload',
    'hc.marked',
    'customDirectives',
    'authServices',
    'wsServices',
    'groupControllers',
    'groupServices',
    'groupMemberControllers',
    'groupMemberServices',
    'userControllers',
    'userServices',
    'assignmentsControllers',
    'assignmentServices',
    'solutionServices',
    'solutionControllers'
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
    $urlRouterProvider.otherwise(function($injector, $location){
      permission.name === 'Administrator' ? $location.path('users') : $location.path('groups')
    });
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
        permission: ['Educator', 'NormalUser'],
        views: {
          'main': {
            templateUrl: scaxerciserApp.partialsRoot + 'groups-list.html',
            controller: 'GroupListCtrl'
          }
        }
      })
      .state('groups-list.new', {
        permission: ['Educator'],
        url: '/new',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'groups-list.new.html',
            controller: 'GroupCreationCtrl'
          }
        }
      })
      .state('groups-list.edit', {
        permission: ['Educator'],
        url: '/:groupId/edit',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'groups-list.edit.html',
            controller: 'GroupDetailCtrl'
          }
        }
      })
      .state('groups-list.stats', {
        permission: ['Educator', 'NormalUser'],
        url: '/:groupId/stats',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'groups-list.stats.html',
            controller: 'GroupStatsCtrl'
          }
        }
      })
      .state('group-members-list', {
        permission: ['Educator', 'NormalUser'],
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
        permission: ['Educator', 'NormalUser'],
        url: '/add',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-members-list.add.html',
            controller: 'GroupMembersAddingCtrl'
          }
        }
      })
      .state('group-members-list.solutions', {
        permission: ['Educator', 'NormalUser'],
        url: '/:id/solutions',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'user-solutions-list.html',
            controller: 'GroupMembersSolutionsListCtrl'
          }
        }
      })
      .state('group-educators-list', {
        permission: ['Educator', 'Administrator', 'NormalUser'],
        url: '/groups/:groupId/educators',
        views: {
          'main': {
            template: '='
          },
          'additional': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-educators-list.html',
            controller: 'GroupEducatorsListCtrl'
          }
        }
      })
      .state('group-educators-list.add', {
        permission: ['Educator', 'Administrator', 'NormalUser'],
        url: '/add',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-educators-list.add.html',
            controller: 'GroupEducatorsAddingCtrl'
          }
        }
      })
      .state('group-assignments-new', {
        permission: ['Educator', 'NormalUser'],
        url: '/groups/:groupId/assignments/new',
        views: {
          'main': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-assignments-new.html',
            controller: 'AssignmentCreationCtrl'
          }
        }
      })
      .state('group-assignments-new.project', {
        permission: ['Educator', 'NormalUser'],
        url: '/:id/project',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-assignments-new.project.html',
            controller: 'AssignmentCreationProjectCtrl'
          }
        }
      })
      .state('group-assignments-edit', {
        permission: ['Educator', 'NormalUser'],
        url: '/groups/:groupId/assignments/:id/edit',
        views: {
          'main': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-assignments-edit.html',
            controller: 'GroupAssignmentsEditCtrl'
          }
        }
      })
      .state('group-assignments-show', {
        url: '/groups/:groupId/assignments/:id',
        views: {
          'main': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-assignments-show.html',
            controller: 'GroupAssignmentsDetailCtrl'
          }
        }
      })
      .state('group-assignments-list', {
        url: '/groups/:groupId/assignments',
        views: {
          'main': {
            template: '='
          },
          'additional': {
            templateUrl: scaxerciserApp.partialsRoot + 'group-assignments-list.html',
            controller: 'GroupAssignmentsListCtrl'
          }
        }
      })
      .state('users-list', {
        permission: ['Administrator', 'Educator'],
        url: "/users",
        views: {
          'main': {
            templateUrl: scaxerciserApp.partialsRoot + 'users-list.html',
            controller: 'UserListCtrl'
          }
        }
      })
      .state('users-list.new', {
        permission: ['Administrator'],
        url: '/new',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'users-list.new.html',
            controller: 'UserCreationCtrl'
          }
        }
      })
      .state('users-list.edit', {
        permission: ['Administrator', 'Educator'],
        url: '/:id/edit',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'users-list.edit.html',
            controller: 'UserShortDetailCtrl'
          }
        }
      })
      .state('users-list.solutions', {
        permission: ['Educator', 'NormalUser'],
        url: '/:userId/solutions',
        views: {
          '': {
            templateUrl: scaxerciserApp.partialsRoot + 'user-solutions-list.html',
            controller: 'UserSolutionsListCtrl'
          }
        }
      })
      .state('user-solutions-list', {
        permission: ['NormalUser', 'Educator'],
        url: "/user/:userId/solutions",
        views: {
          'main': {
            templateUrl: scaxerciserApp.partialsRoot + 'user-solutions-list.html',
            controller: 'UserSolutionsListCtrl'
          }
        }
      })
      .state('user-solutions-show', {
        permission: ['Educator', 'NormalUser'],
        url: "/users/:userId/solutions/:id",
        views: {
          'main': {
            templateUrl: scaxerciserApp.partialsRoot + 'user-solutions-show.html',
            controller: 'UserSolutionsDetailCtrl'
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

scaxerciserApp.run(['$rootScope', '$state', 'Auth', 'WS',
  function ($rootScope, $state, Auth, WS) {
    Auth.setPermission(permission);
    WS.initialize();
    $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
      if (typeof toState.permission !== "undefined" && !Auth.hasPermission(toState.permission)) {
        event.preventDefault();
        $state.transitionTo('unauthorized');
      }
    });
  }]);