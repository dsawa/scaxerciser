'use strict';

// ----- Group Controllers
var groupControllers = angular.module('groupControllers', []);

groupControllers.controller('GroupListCtrl', ['$scope', '$filter', 'ngTableParams', 'Group',
  function ($scope, $filter, ngTableParams, Group) {
    $scope.groupsTable = new ngTableParams({
      page: 1,
      count: 10,
      sorting: {
        email: 'asc'
      }
    }, {
      total: 0,
      getData: function ($defer, params) {
        Group.query({}, function (data) {
          var groups = data;

          if (params.sorting()) groups = $filter('orderBy')(groups, params.orderBy());
          if (params.filter()) groups = $filter('filter')(groups, params.filter());

          params.total(groups.length);
          $defer.resolve(groups.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        });
      }
    });

    $scope.deleteGroup = function (groupId) {
      groupsBloodhound.clearRemoteCache();
      Group.delete({id: groupId}, function () {
        $scope.groupsTable.reload();
      });
    };
  }
]);

groupControllers.controller('GroupDetailCtrl', ['$stateParams', '$scope', '$state', '$location', 'Group',
  function ($stateParams, $scope, $state, $location, Group) {
    $scope.updateGroup = function () {
      var params = $scope.group;
      params.id = $stateParams.groupId;
      Group.update(params, function () {
        groupsBloodhound.clearRemoteCache();
        $state.transitionTo('groups-reload');
        $location.path('groups');
      });
    };
    $scope.group = Group.show({
      id: $stateParams.groupId
    })
  }
]);

groupControllers.controller('GroupCreationCtrl', ['$scope', '$state', '$location', 'Group',
  function ($scope, $state, $location, Group) {
    $scope.createGroup = function () {
      Group.create($scope.group, function () {
        groupsBloodhound.clearRemoteCache();
        $state.transitionTo('groups-reload');
        $location.path('groups');
      });
    }
  }
]);

groupControllers.controller('GroupStatsCtrl', ['$scope', '$filter', 'ngTableParams', '$stateParams', '$state', 'GroupStats',
  function ($scope, $filter, ngTableParams, $stateParams, $state, GroupStats) {
    $scope.stats = GroupStats.stats({
      id: $stateParams.groupId
    });

    $scope.assignmentsStatsTable = new ngTableParams({
      page: 1,
      count: 10,
      sorting: {
        avgMark: 'desc'
      }
    }, {
      total: 0,
      getData: function ($defer, params) {
        GroupStats.assignmentsStats({
          id: $stateParams.groupId
        }, function (data) {
          var assignmentsStats = data;

          if (params.sorting()) assignmentsStats = $filter('orderBy')(assignmentsStats, params.orderBy());
          if (params.filter()) assignmentsStats = $filter('filter')(assignmentsStats, params.filter());

          params.total(assignmentsStats.length);
          $defer.resolve(assignmentsStats.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        });
      }
    });
  }
]);

// ----- Assignments Controllers

var assignmentsControllers = angular.module('assignmentsControllers', []);

assignmentsControllers.controller('AssignmentCreationCtrl', ['$stateParams', '$scope', '$state', 'Group', 'Assignment',
  function ($stateParams, $scope, $state, Group, Assignment) {
    var markDownEditorOpt = {
      autofocus: false,
      savable: false,
      iconlibrary: "fa",
      language: {
        titleBold: "Pogrubienie", titleItalic: 'Kursywa', titleHeading: 'Nagłówek', titleList: 'Lista',
        titleImage: 'Obrazek', titlePreview: 'Podgląd', btnTextPreview: 'Podgląd'
      }
    }, initializeMarkdownEditors;

    initializeMarkdownEditors = function () {
      setTimeout(function () {
        angular.element('#assignmentForm').find('textarea').markdown(markDownEditorOpt);
      }, 300);
    };

    $scope.$on('$viewContentLoaded', function () {
      angular.element('#assignmentForm').find('button[type=submit]').tooltip();
      initializeMarkdownEditors();
    });

    $scope.group = Group.show({
      id: $stateParams.groupId
    });

    $scope.assignment = {
      groupId: $stateParams.groupId,
      exercises: [
        { description: '', hint: ''}
      ]
    };

    $scope.createAssignment = function () {
      Assignment.create($scope.assignment, function (createdAssignment) {
        var params = { groupId: createdAssignment['groupId']['$oid'], id: createdAssignment['_id']['$oid'] };
        $state.go('group-assignments-new.project', params);
      });
    };

    $scope.newExercise = function ($event) {
      $event.preventDefault();
      $scope.assignment.exercises.push({ description: '', hint: ''});
      initializeMarkdownEditors();
    };
  }
]);

assignmentsControllers.controller('AssignmentCreationProjectCtrl', ['$stateParams', '$scope', '$state', 'Assignment',
  function ($stateParams, $scope, $state, Assignment) {
    angular.element('#assignmentForm').find('input').attr('disabled', true);
    angular.element('#assignmentForm').find('textarea').attr('disabled', true);
    angular.element('#assignmentForm').find('button').attr('disabled', true);

    $scope.formAction = '/api/groups/' + $stateParams.groupId + '/assignments/' + $stateParams.id + '/project';

    $scope.assignment = Assignment.show({
      groupId: $stateParams.groupId,
      id: $stateParams.id
    });

    $scope.uploadComplete = function (response) {
      if (typeof response['projectId'] !== 'undefined' && typeof response['projectTestsId'] !== 'undefined') {
        $state.transitionTo('group-assignments-show', {
          groupId: $stateParams.groupId,
          id: $stateParams.id
        })
      }
    };
  }
]);

assignmentsControllers.controller('GroupAssignmentsEditCtrl', ['$stateParams', '$scope', '$state', 'Group', 'Assignment',
  function ($stateParams, $scope, $state, Group, Assignment) {
    var markDownEditorOpt = {
      autofocus: false,
      savable: false,
      iconlibrary: "fa",
      language: {
        titleBold: "Pogrubienie", titleItalic: 'Kursywa', titleHeading: 'Nagłówek', titleList: 'Lista',
        titleImage: 'Obrazek', titlePreview: 'Podgląd', btnTextPreview: 'Podgląd'
      }
    }, initializeMarkdownEditors;

    initializeMarkdownEditors = function () {
      setTimeout(function () {
        angular.element('#assignmentForm').find('textarea').markdown(markDownEditorOpt);
      }, 300);
    };

    $scope.$on('$viewContentLoaded', initializeMarkdownEditors);

    $scope.formAction = '/api/groups/' + $stateParams.groupId + '/assignments/' + $stateParams.id + '/project';

    $scope.group = Group.show({
      id: $stateParams.groupId
    });

    $scope.assignment = Assignment.show({
      groupId: $stateParams.groupId,
      id: $stateParams.id
    }, function (assignment) {
      $scope.baseAssignmentTitle = assignment.title;
    });

    $scope.updateAssignment = function () {
      var params = $scope.assignment;
      params.groupId = $stateParams.groupId;
      params.id = $stateParams.id;
      Assignment.update(params, function (updatedAssignment) {
        $.notify('Aktualizacja przebiegła pomyślnie.', "success");
      }, function (errorText) {
        $.notify(errorText, "error")
      });
    };

    $scope.uploadComplete = function (response) {
      if (typeof response['projectId'] !== 'undefined' && typeof response['projectTestsId'] !== 'undefined') {
        $.notify('Przesyłanie nowych plików przebiegło pomyślnie.', 'success');
      } else {
        $.notify((response).replace(/<(\/)?pre>/g, ''), 'error');
      }
    };

    $scope.newExercise = function ($event) {
      $event.preventDefault();
      $scope.assignment.exercises.push({ description: '', hint: ''});
      initializeMarkdownEditors();
    };
  }
]);

assignmentsControllers.controller('GroupAssignmentsListCtrl', ['$stateParams', '$scope', '$state', 'Group', 'Assignment',
  function ($stateParams, $scope, $state, Group, Assignment) {
    $scope.group = Group.show({id: $stateParams.groupId});
    $scope.assignments = Assignment.query({groupId: $stateParams.groupId});

    $scope.expandLast = function ($last) {
      return $last ? "panel-collapse collapse in" : "panel-collapse collapse";
    };

    $scope.deleteAssignment = function (groupId, id) {
      Assignment.delete({ groupId: groupId, id: id}, function () {
        angular.element('div#assignment-panel-' + id).remove();
      }, function (errorResponse) {
        $.notify(errorResponse.data, "error")
      });
    }
  }
]);

assignmentsControllers.controller('GroupAssignmentsDetailCtrl', ['$stateParams', '$scope', '$state', '$location', 'Group',
  'Assignment', 'CurrentUserAssignmentSolution', 'Auth',
  function ($stateParams, $scope, $state, $location, Group, Assignment, CurrentUserAssignmentSolution, Auth) {
    var params = {
      groupId: $stateParams.groupId,
      id: $stateParams.id
    };

    $scope.projectLink = '/api/groups/' + $stateParams.groupId + '/assignments/' + $stateParams.id + '/project';

    $scope.group = Group.show({id: $stateParams.groupId}, function (group) {
      if (Auth.hasPermissionInGroup(group, ['NormalUser'])) {
        $scope.solution = CurrentUserAssignmentSolution.show({assignmentId: $stateParams.id}, function (solution) {
          var defineCssClassBasedOnMark = function (mark) {
            if (mark < 50) $scope.isDanger = true;
            else if (mark > 50 && mark < 80) $scope.isAverage = true;
            else $scope.isSuccess = true;
          };

          if (solution.result !== null && typeof solution.result !== 'undefined') {
            defineCssClassBasedOnMark(solution.result.mark)
          }
        }, function(response) {
          if(response.status === 404) $scope.solution = { result: null };
        });
      }
    });

    $scope.assignment = Assignment.show(params);

    $scope.activateAssignment = function () {
      $.extend(true, $scope.assignment, params);
      $scope.assignment.enabled = true;

      Assignment.update($scope.assignment, function () {
        $.notify('Aktywowano zadanie', "success");
      }, function (errorResponse) {
        $scope.assignment.enabled = false;
        $.notify(errorResponse.data, "error")
      })
    };

    $scope.deleteAssignment = function () {
      Assignment.delete(params, function () {
        $state.go('groups-list');
      }, function (errorResponse) {
        $.notify(errorResponse.data, "error")
      })
    }
  }
]);

// ----- User Controllers

var userControllers = angular.module('userControllers', []);

userControllers.controller('UserListCtrl', ['$scope', '$filter', '$q', 'ngTableParams', 'User',
  function ($scope, $filter, $q, ngTableParams, User) {
    $scope.usersTable = new ngTableParams({
      page: 1,
      count: 10,
      sorting: {
        email: 'asc'
      }
    }, {
      total: 0,
      getData: function ($defer, params) {
        User.query({}, function (data) {
          var users = data.map(function (user) {
            if (user.permission === 'Educator')
              return $.extend(user, {permission: 'Prowadzący'});
            else if (user.permission === 'NormalUser')
              return $.extend(user, {permission: 'Uczestnik'});
            else
              return user;
          });

          if (params.sorting()) users = $filter('orderBy')(users, params.orderBy());
          if (params.filter()) users = $filter('filter')(users, params.filter());

          params.total(users.length);
          $defer.resolve(users.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        });
      }
    });

    $scope.permissions = function (column) {
      var def = $q.defer();
      def.resolve([
        { id: 'Administrator', title: 'Administrator' },
        { id: 'Prowadzący', title: 'Prowadzący' },
        { id: 'Uczestnik', title: 'Uczestnik' }
      ]);
      return def;
    };

    $scope.deleteUser = function (userId) {
      User.delete({id: userId}, function () {
        $scope.usersTable.reload();
      });
    };
  }
]);

userControllers.controller('UserCreationCtrl', ['$scope', '$state', '$location', 'User',
  function ($scope, $state, $location, User) {
    $scope.createUser = function () {
      User.create($scope.user, function () {
        $scope.usersTable.reload();
        $scope.passRepeated = '';
        $scope.user = {};
        $.notify('Dodano użytkownika', 'success');
      });
    }
  }
]);

userControllers.controller('UserShortDetailCtrl', ['$stateParams', '$scope', '$state', '$location', 'User',
  function ($stateParams, $scope, $state, $location, User) {
    $scope.updateUser = function () {
      var params = $scope.user;
      params.id = $stateParams.id;
      if (params.password.length < 6) delete params.password;
      User.update(params, function () {
        $scope.usersTable.reload();
        $scope.user.password = '';
        $scope.passRepeated = '';
        $.notify('Zaktualizowano użytkownika', 'success');
      });
    };
    $scope.user = User.show({
      id: $stateParams.id
    }, function (user) {
      user.password = '';
    })
  }
]);

// ----- GroupMember Controllers

var groupMemberControllers = angular.module('groupMemberControllers', []);

groupMemberControllers.controller('GroupMembersListCtrl', ['$stateParams', '$scope', '$rootScope', '$filter', 'ngTableParams',
  'Group', 'GroupMember', function ($stateParams, $scope, $rootScope, $filter, ngTableParams, Group, GroupMember) {
    $scope.group = Group.show({id: $stateParams.groupId});
    $scope.membersTable = new ngTableParams({
      page: 1,
      count: 10,
      sorting: {
        email: 'asc'
      }
    }, {
      total: 0,
      getData: function ($defer, params) {
        GroupMember.getNormalUsers({
          groupId: $stateParams.groupId
        }, function (data) {
          var members = data;

          if (params.sorting()) members = $filter('orderBy')(members, params.orderBy());
          if (params.filter()) members = $filter('filter')(members, params.filter());

          params.total(members.length);
          $defer.resolve(members.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        });
      }
    });

    $scope.removeUserFromGroup = function (groupId, userId) {
      GroupMember.removeFromGroup({groupId: groupId, id: userId}, function () {
        $scope.membersTable.reload();
        $rootScope.usersTable.reload();
      });
    }
  }
]);

groupMemberControllers.controller('GroupMembersAddingCtrl', ['$stateParams', '$scope', '$rootScope', '$filter', 'Group', 'GroupMember',
  'User', 'ngTableParams', function ($stateParams, $scope, $rootScope, $filter, Group, GroupMember, User, ngTableParams) {
    $scope.group = Group.show({id: $stateParams.groupId});
    $scope.usersTable = new ngTableParams({
      page: 1,
      count: 10,
      sorting: {
        email: 'asc'
      }
    }, {
      total: 0,
      getData: function ($defer, params) {
        var filter = { groupIds: { '$ne': { '$oid': $stateParams.groupId } }, permission: { '$ne': 'Administrator' } };

        User.query({filter: (function () {
          return JSON.stringify(filter);
        })()}, function (data) {
          var users = data;

          if (params.sorting()) users = $filter('orderBy')(users, params.orderBy());
          if (params.filter()) users = $filter('filter')(users, params.filter());

          params.total(users.length);
          $defer.resolve(users.slice((params.page() - 1) * params.count(), params.page() * params.count()));

          $rootScope.usersTable = $scope.usersTable;
        });
      }
    });

    $scope.addUserToGroup = function (groupId, userId) {
      GroupMember.assignToGroup({groupId: groupId, id: userId, roleInGroup: "NormalUser"}, function () {
        $scope.membersTable.reload();
        $scope.usersTable.reload();
      });
    };
  }
]);

groupMemberControllers.controller('GroupMembersSolutionsListCtrl', ['$scope', '$stateParams', '$state', '$filter', 'ngTableParams', 'GroupMember',
  function ($scope, $stateParams, $state, $filter, ngTableParams, GroupMember) {
    $scope.solutionsTable = new ngTableParams({
      page: 1,
      count: 10,
      sorting: {}
    }, {
      total: 0,
      getData: function ($defer, params) {
        GroupMember.solutions({ groupId: $stateParams.groupId, id: $stateParams.id }, function (data) {
          var solutions = data;

          if (params.sorting()) solutions = $filter('orderBy')(solutions, params.orderBy());
          if (params.filter()) solutions = $filter('filter')(solutions, params.filter());

          params.total(solutions.length);
          $defer.resolve(solutions.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        });
      }
    });

    $scope.goToSolutionDetails = function (userId, solutionId) {
      $state.go('user-solutions-show', {userId: userId, id: solutionId});
    }
  }
]);

groupMemberControllers.controller('GroupEducatorsListCtrl', ['$stateParams', '$scope', '$rootScope', '$filter', 'ngTableParams',
  'Group', 'GroupMember', function ($stateParams, $scope, $rootScope, $filter, ngTableParams, Group, GroupMember) {
    $scope.group = Group.show({id: $stateParams.groupId}, function (group) {
      for (var i = 0; i < group.groupRoles.length; i += 1) {
        if (group.groupRoles[i].roleInGroup === 'Administrator') {
          group.ownerId = group.groupRoles[i].accountId;
          break;
        }
      }
    });
    $scope.educatorsTable = new ngTableParams({
      page: 1,
      count: 10,
      sorting: {
        email: 'asc'
      }
    }, {
      total: 0,
      getData: function ($defer, params) {
        GroupMember.getEducators({
          groupId: $stateParams.groupId
        }, function (data) {
          var educators = data;

          if (params.sorting()) educators = $filter('orderBy')(educators, params.orderBy());
          if (params.filter()) educators = $filter('filter')(educators, params.filter());

          params.total(educators.length);
          $defer.resolve(educators.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        });
      }
    });

    $scope.removeUserFromGroup = function (groupId, userId) {
      GroupMember.removeFromGroup({groupId: groupId, id: userId}, function () {
        $scope.educatorsTable.reload();
        $rootScope.usersTable.reload();
      });
    }
  }
]);

groupMemberControllers.controller('GroupEducatorsAddingCtrl', ['$stateParams', '$scope', '$rootScope', '$filter', 'Group', 'GroupMember',
  'User', 'ngTableParams', function ($stateParams, $scope, $rootScope, $filter, Group, GroupMember, User, ngTableParams) {
    $scope.group = Group.show({id: $stateParams.groupId});
    $scope.usersTable = new ngTableParams({
      page: 1,
      count: 10,
      sorting: {
        email: 'asc'
      }
    }, {
      total: 0,
      getData: function ($defer, params) {
        var filter = { groupIds: { '$ne': { '$oid': $stateParams.groupId } }, permission: { '$ne': 'Administrator' } };

        User.query({filter: (function () {
          return JSON.stringify(filter);
        })()}, function (data) {
          var users = data;

          if (params.sorting()) users = $filter('orderBy')(users, params.orderBy());
          if (params.filter()) users = $filter('filter')(users, params.filter());

          params.total(users.length);
          $defer.resolve(users.slice((params.page() - 1) * params.count(), params.page() * params.count()));

          $rootScope.usersTable = $scope.usersTable;
        });
      }
    });

    $scope.addUserToGroup = function (groupId, userId) {
      GroupMember.assignToGroup({groupId: groupId, id: userId, roleInGroup: "Educator"}, function () {
        $scope.educatorsTable.reload();
        $scope.usersTable.reload();
      });
    };
  }
]);

// ----- Solution Controllers
var solutionControllers = angular.module('solutionControllers', []);

solutionControllers.controller('UserSolutionsListCtrl', ['$scope', '$stateParams', '$state', '$filter', 'ngTableParams', 'UserSolution',
  function ($scope, $stateParams, $state, $filter, ngTableParams, UserSolution) {
    $scope.solutionsTable = new ngTableParams({
      page: 1,
      count: 10,
      sorting: {}
    }, {
      total: 0,
      getData: function ($defer, params) {
        UserSolution.query({ userId: $stateParams.userId }, function (data) {
          var solutions = data;

          if (params.sorting()) solutions = $filter('orderBy')(solutions, params.orderBy());
          if (params.filter()) solutions = $filter('filter')(solutions, params.filter());

          params.total(solutions.length);
          $defer.resolve(solutions.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        });
      }
    });

    $scope.goToSolutionDetails = function (userId, solutionId) {
      $state.go('user-solutions-show', {userId: userId, id: solutionId});
    }
  }
]);

solutionControllers.controller('UserSolutionsDetailCtrl', ['$stateParams', '$scope', '$state', '$window', 'UserSolution',
  'User', function ($stateParams, $scope, $state, $window, UserSolution, User) {
    $scope.solution = UserSolution.show({
      userId: $stateParams.userId,
      id: $stateParams.id
    }, function (solution) {

      $scope.user = User.show({id: solution.userId['$oid']});

      var defineCssClassBasedOnMark = function (mark) {
        if (mark < 50) $scope.isDanger = true;
        else if (mark > 50 && mark < 80) $scope.isAverage = true;
        else $scope.isSuccess = true;
      };

      if (solution.result !== null || typeof solution.result !== 'undefined') {
        defineCssClassBasedOnMark(solution.result.mark)
      }
    });

    $scope.back = function () {
      $window.history.back();
    }
  }
]);