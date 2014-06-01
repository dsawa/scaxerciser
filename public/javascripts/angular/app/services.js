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
        var appendSolutionToPreviewList, startWS;

        appendSolutionToPreviewList = function (solution) {
          var listElement, link, linkContent, solutionInfo, progressBar, divider, actualLi,
            defineCssClass = function (mark) {
              if (mark < 50) {
                return "danger";
              } else if (mark > 50 && mark < 70) {
                return "warning";
              } else if (mark > 70 && mark < 90) {
                return "info";
              } else {
                return "success";
              }
            };

          actualLi = angular.element('li#' + solution.id);

          if (actualLi.length === 1) {
            actualLi.next().remove();
            actualLi.remove();
          }

          divider = angular.element('<li class="divider"></li>');
          listElement = angular.element('<li></li>');

          link = angular.element('<a ui-sref="group-assignments-show({groupId: "' + solution.groupId + '", ' +
            'id: "' + solution.assignmentId + '"})" href="#/groups/' + solution.groupId + '/assignments/' +
            solution.assignmentId + '"></a>');

          linkContent = angular.element('<div></div>');

          solutionInfo = angular.element('<p><strong>' + solution.assignmentTitle + '</strong>' +
            '<span class="pull-right text-muted">' + solution.result.mark + '%</span></p>');

          progressBar = angular.element('<div class="progress progress-striped active">' +
            '<div class="progress-bar progress-bar-' + defineCssClass(solution.result.mark) + '" ' +
            'role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: ' + solution.result.mark + '%">' +
            '</div></div>');

          linkContent.append(solutionInfo);
          linkContent.append(progressBar);
          link.append(linkContent);
          listElement.append(link);
          angular.element('ul#solution-results-preview').prepend(divider).prepend(listElement);
        };

        startWS = function () {
          $http({method: 'GET', url: '/wsUrl'}).success(function (data) {
            $rootScope.socket = new WebSocket(data['wsUrl']);
            $rootScope.socket.onmessage = function (msg) {
              $rootScope.$apply(function () {
                var solutionData = JSON.parse(msg.data);
                appendSolutionToPreviewList(solutionData);
                $.notify("Oceniono Twoje rozwiązanie zadania " + solutionData.assignmentTitle, "success");
              });
            }
          });
        };

        startWS();
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