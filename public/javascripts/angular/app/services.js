'use strict';

// ----- Group Services
var groupServices = angular.module('groupServices', ['ngResource']);

groupServices.factory('Group', ['$resource',
  function ($resource) {
    return $resource('api/groups/:id', {}, {
      query: {
        method: 'GET',
        params: {},
        isArray: true
      },
      create: {
        method: 'POST',
        params: {}
      },
      show: {
        method: 'GET',
        params: {
          id: '@id'
        }
      },
      update: {
        method: 'PUT',
        params: {
          id: '@id'
        }
      },
      delete: {
        method: 'DELETE',
        params: {
          id: '@id'
        }
      }
    });
  }
]);

// ----- User Services
var userServices = angular.module('userServices', ['ngResource']);

userServices.factory('User', ['$resource',
  function ($resource) {
    return $resource('api/users/:id', {}, {
      query: {
        method: 'GET',
        params: {},
        isArray: true
      },
      create: {
        method: 'POST',
        params: {}
      },
      show: {
        method: 'GET',
        params: {
          id: '@id'
        }
      },
      update: {
        method: 'PUT',
        params: {
          id: '@id'
        }
      },
      delete: {
        method: 'DELETE',
        params: {
          id: '@id'
        }
      }
    });
  }
]);

// ------ Authorization Service
var authServices = angular.module('authServices', []);

authServices.factory('Auth', function ($rootScope) {
  var permission;
  return {
    setPermission: function (perm) {
      permission = perm;
    },
    hasPermission: function (perm) {
      return perm.trim() === permission.name;
    }
  };
});

// ----- GroupMember services
var groupMemberServices = angular.module('groupMemberServices', ['ngResource']);

groupMemberServices.factory('GroupMember', ['$resource',
  function ($resource) {
    return $resource('api/groups/:groupId/members/:id', {}, {
      query: {
        method: 'GET',
        params: {
          groupId: '@groupId'
        },
        isArray: true
      }
    });
  }
]);