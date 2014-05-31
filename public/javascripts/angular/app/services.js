'use strict';

// ----- Group Services
var groupServices = angular.module('groupServices', ['ngResource']);

groupServices.factory('Group', ['$resource',
  function ($resource) {
    return $resource('api/groups/:id/:customAction', {}, {
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
]).factory('GroupStats', ['$resource',
  function ($resource) {
    return $resource('api/groups/:id/stats/:custom', {}, {
      stats: {
        method: 'GET',
        params: {
          id: '@id'
        }
      },
      assignmentsStats: {
        method: 'GET',
        params: {
          id: '@id',
          custom: 'assignments'
        },
        isArray: true
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


// ----- Solution Services
var solutionServices = angular.module('solutionServices', ['ngResource']);

solutionServices.factory('AssignmentSolution', ['$resource',
  function ($resource) {
    return $resource('api/assignments/:assignmentId/solutions/:id', {}, {
      query: {
        method: 'GET',
        params: {
          assignmentId: '@assignmentId'
        },
        isArray: true
      },
      show: {
        method: 'GET',
        params: {
          assignmentId: '@assignmentId',
          id: '@id'
        }
      }
    });
  }
]).factory('CurrentUserAssignmentSolution', ['$resource',
  function ($resource) {
    return $resource('api/users/current/assignments/:assignmentId/solutions', {}, {
      query: {
        method: 'GET',
        params: {
          assignmentId: '@assignmentId'
        },
        isArray: true
      },
      show: {
        method: 'GET',
        params: {
          assignmentId: '@assignmentId'
        }
      }
    });
  }
]).factory('UserSolution', ['$resource',
  function ($resource) {
    return $resource('api/users/:userId/solutions/:id', {}, {
      query: {
        method: 'GET',
        params: {
          userId: '@userId',
          id: ''
        },
        isArray: true
      },
      show: {
        method: 'GET',
        params: {
          userId: '@userId',
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
    getCurrentPermission: function () {
      return permission;
    },
    setPermission: function (perm) {
      permission = perm;
    },
    hasPermission: function (perm) {
      if (Array.isArray(perm))
        return perm.indexOf(permission.name) !== -1;
      else
        return perm.trim() === permission.name;
    },
    hasPermissionInGroup: function (group, permissionsThatAllow) {
      for (var i = 0; i < group['groupRoles'].length; i += 1) {
        if (group['groupRoles'][i]['accountId']['$oid'] === permission['accountId']
          && permissionsThatAllow.indexOf(group['groupRoles'][i]['roleInGroup']) !== -1) {
          return true;
        }
      }
      return false;
    }
  };
});

// ------ WebSocket Service
var wsServices = angular.module('wsServices', ['ng']);

wsServices.factory('WS', ['$rootScope', '$http',
  function ($rootScope, $http) {
    return {
      initialize: function () {
        $http({method: 'GET', url: '/wsUrl'}).success(function (data) {
          $rootScope.socket = new WebSocket(data['wsUrl']);
          $rootScope.socket.onmessage = function (msg) {
            $rootScope.$apply(function() {
              console.log("Odebrano: ");
              console.log(msg)
            });
          }
        });
      }
    }
  }
]);

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
      solutions: {
        method: 'GET',
        params: {
          customAction: 'solutions',
          groupId: '@groupId',
          id: '@id'
        },
        isArray: true
      },
      getEducators: {
        method: 'GET',
        params: {
          id: 'educators'
        },
        isArray: true
      },
      getNormalUsers: {
        method: 'GET',
        params: {
          id: 'normalusers'
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