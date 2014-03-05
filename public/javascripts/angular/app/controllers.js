'use strict';

// ----- Group Controllers
var groupControllers = angular.module('groupControllers', []);

groupControllers.controller('GroupListCtrl', ['$scope', 'Group',
  function ($scope, Group) {
    $scope.groups = Group.query({}, groupTable.load);

    $scope.deleteGroup = function (groupId) {
      var row = $('#' + groupTable.tableId).find('tr#' + groupId)[0];
      Group.delete({id: groupId}, function () {
        $('#' + groupTable.tableId).dataTable().fnDeleteRow(row);
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
        $state.transitionTo('groups-reload');
        $location.path('groups');
      });
    }
  }
]);

var groupTable = {
  tableId: 'groups-table',
  tableSettings: {
    oLanguage: scaxerciserApp.dataTables.languageSettings,
    aoColumnDefs: [
      {
        "bSortable": false,
        "aTargets": [1]
      }
    ]
  },
  loadDelay: 300,
  load: function () {
    setTimeout(function () {
      var $table = $('#' + groupTable.tableId);
      if (!$.fn.DataTable.fnIsDataTable($table)) {
        $table.dataTable(groupTable.tableSettings);
      }
    }, groupTable.loadDelay);
  }
};

// ----- User Controllers

var userControllers = angular.module('userControllers', []);

userControllers.controller('UserListCtrl', ['$scope', 'User',
  function ($scope, User) {
    $scope.users = User.query({}, userTable.load);

    $scope.deleteUser = function (userId) {
      var row = $('#' + userTable.tableId).find('tr#' + userId)[0];
      User.delete({id: userId}, function () {
        $('#' + userTable.tableId).dataTable().fnDeleteRow(row);
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
      if (params.password.length < 6) delete params.password
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

var userTable = {
  tableId: 'users-table',
  tableSettings: {
    oLanguage: scaxerciserApp.dataTables.languageSettings,
    aoColumnDefs: [
      {
        "bSortable": false,
        "aTargets": [2]
      }
    ]
  },
  loadDelay: 300,
  load: function () {
    setTimeout(function () {
      var $table = $('#' + userTable.tableId);
      if (!$.fn.DataTable.fnIsDataTable($table)) {
        $table.dataTable(userTable.tableSettings);
      }
    }, userTable.loadDelay);
  }
};

// ----- GroupMember Controllers

var groupMemberControllers = angular.module('groupMemberControllers', []);

groupMemberControllers.controller('GroupMembersListCtrl', ['$stateParams', '$scope', '$state', 'Group', 'GroupMember',
  function ($stateParams, $scope, $state, Group, GroupMember) {
    var $membersTable = $('#' + membersTable.tableId);

    $scope.group = Group.show({id: $stateParams.groupId});
    $scope.members = GroupMember.query({groupId: $stateParams.groupId}, membersTable.load);

    $scope.removeUserFromGroup = function (groupId, userId) {
      GroupMember.removeFromGroup({groupId: groupId, id: userId}, function (user) {
        var row = $membersTable.find('tr#' + userId)[0];
        $membersTable.dataTable().fnDeleteRow(row);
      });
    }
  }
]);

groupMemberControllers.controller('GroupMembersAddingCtrl', ['$stateParams', '$scope', '$state', 'Group', 'GroupMember', 'User',
  function ($stateParams, $scope, $state, Group, GroupMember, User) {
    var $usersTable = $('#' + membersAddingTable.tableId), $membersTable = $('#' + membersTable.tableId);

    $scope.group = Group.show({id: $stateParams.groupId});
    $scope.users = User.query({}, membersAddingTable.load);

    $scope.addUserToGroup = function (groupId, userId) {
      GroupMember.assignToGroup({groupId: groupId, id: userId}, function (user) {
        var row = $usersTable.find('tr#' + userId)[0];
        $usersTable.dataTable().fnDeleteRow(row);
        $membersTable.dataTable().fnAddTr(membersTable.produceTr(user));
      });
    };
  }
]);

var membersTable = {
  tableId: 'members-table',
  tableSettings: {
    oLanguage: scaxerciserApp.dataTables.languageSettings,
    aoColumnDefs: [
      {
        "bSortable": false,
        "sWidth": "45px",
        "aTargets": [0]
      }
    ]
  },
  loadDelay: 300,
  load: function () {
    setTimeout(function () {
      var $table = $('#' + membersTable.tableId);
      if (!$.fn.DataTable.fnIsDataTable($table)) {
        $table.dataTable(membersTable.tableSettings);
      }
    }, membersTable.loadDelay);
  },
  produceTr: function (object) {
    var tr = document.createElement('tr');
    tr.setAttribute('id', object._id.$oid);
    tr.innerHTML = '<td><a ng-click="removeUserFromGroup(group._id.$oid, member._id.$oid)" class="btn btn-danger btn-xs">Usuń</a>' +
      '</td><td>' + object.email + '</td>';
    return tr;
  }
};

var membersAddingTable = {
  tableId: 'members-adding-table',
  tableSettings: {
    oLanguage: scaxerciserApp.dataTables.languageSettings,
    aoColumnDefs: [
      {
        "bSortable": false,
        "sWidth": "45px",
        "aTargets": [0]
      }
    ]
  },
  loadDelay: 300,
  load: function () {
    setTimeout(function () {
      var $table = $('#' + membersAddingTable.tableId);
      if (!$.fn.DataTable.fnIsDataTable($table)) {
        $table.dataTable(membersAddingTable.tableSettings);
      }
    }, membersAddingTable.loadDelay);
  },
  produceTr: function (object) {
    var tr = document.createElement('tr');
    tr.setAttribute('id', object._id.$oid);
    tr.innerHTML = '<td><a ng-click="addUserToGroup(group._id.$oid, user._id.$oid)" class="btn btn-success btn-xs">Dodaj</a>' +
      '</td><td>' + object.email + '</td>';
    return tr;
  }
};