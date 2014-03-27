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

// ----- Assignments Controllers

var assignmentsControllers = angular.module('assignmentsControllers', []);

assignmentsControllers.controller('AssignmentCreationCtrl', ['$stateParams', '$scope', '$state', 'Group', 'Assignment',
  function ($stateParams, $scope, $state, Group, Assignment) {
    angular.element('#assignmentForm').find('button[type=submit]').tooltip();

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
    };

    $scope.addPreTagToExercise = function ($event, $index) {
      $event.preventDefault();
      var description = $scope.assignment.exercises[$index].description || '';
      description += '<pre></pre>';
      $scope.assignment.exercises[$index].description = description;
    };

    $scope.addCodeTagToExercise = function ($event, $index) {
      $event.preventDefault();
      var description = $scope.assignment.exercises[$index].description || '';
      description += '<code></code>';
      $scope.assignment.exercises[$index].description = description;
    };
  }
]);

assignmentsControllers.controller('AssignmentCreationProjectCtrl', ['$stateParams', '$scope', '$state', 'Assignment',
  function ($stateParams, $scope, $state, Assignment) {
    angular.element('#assignmentForm').find('button').attr('disabled', true);

    $scope.formAction = '/api/groups/' + $stateParams.groupId + '/assignments/' + $stateParams.id + '/project';

    $scope.assignment = Assignment.show({
      groupId: $stateParams.groupId,
      id: $stateParams.id
    });

    $scope.uploadComplete = function (response) {
      // TODO: redirect do widoku edycji zadanka
      console.log(response);
    };
  }
]);

assignmentsControllers.controller('GroupAssignmentsListCtrl', ['$stateParams', '$scope', 'Group', 'Assignment',
  function ($stateParams, $scope, Group, Assignment) {
    $scope.group = Group.show({id: $stateParams.groupId});
    $scope.assignments = Assignment.query({groupId: $stateParams.groupId});

    $scope.expandLast = function ($last) {
      return $last ? "panel-collapse collapse in" : "panel-collapse collapse";
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
          var users = data;

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
        { id: 'NormalUser', title: 'NormalUser' },
        { id: 'Administrator', title: 'Administrator' }
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
        $state.transitionTo('users-reload');
        $location.path('users');
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
        $state.transitionTo('users-reload');
        $location.path('users');
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
        GroupMember.query({groupId: $stateParams.groupId}, function (data) {
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
        var filter = { groupIds: { '$ne': { '$oid': $stateParams.groupId } }, permission: 'NormalUser' };

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
      GroupMember.assignToGroup({groupId: groupId, id: userId}, function () {
        $scope.membersTable.reload();
        $scope.usersTable.reload();
      });
    };
  }
]);