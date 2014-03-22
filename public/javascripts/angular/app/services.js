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

// ----- Assignment Services
var assignmentServices = angular.module('assignmentServices', ['ngResource']);

assignmentServices.factory('Assignment', ['$resource',
  function ($resource) {
    return $resource('api/groups/:groupId/assignments/:id', {}, {
      query: {
        method: 'GET',
        params: {
          groupId: '@groupId'
        },
        isArray: true
      },
      create: {
        method: 'POST',
        params: {
          groupId: '@groupId'
        }
      },
      show: {
        method: 'GET',
        params: {
          groupId: '@groupId',
          id: '@id'
        }
      },
      update: {
        method: 'PUT',
        params: {
          groupId: '@groupId',
          id: '@id'
        }
      },
      delete: {
        method: 'DELETE',
        params: {
          groupId: '@groupId',
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
    return $resource('api/groups/:groupId/members/:id/:customAction', {}, {
      query: {
        method: 'GET',
        params: {
          groupId: '@groupId'
        },
        isArray: true
      },
      assignToGroup: {
        method: 'PUT',
        params: {
          customAction: 'add',
          groupId: '@groupId',
          id: '@id'
        }
      },
      removeFromGroup: {
        method: 'DELETE',
        params: {
          customAction: 'remove',
          groupId: '@groupId',
          id: '@id'
        }
      }
    });
  }
]);