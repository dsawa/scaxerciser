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
    $scope.group = Group.show({id: $stateParams.groupId});
    $scope.members = GroupMember.query({groupId: $stateParams.groupId}, membersTable.load);

    $scope.removeUserFromGroup = function (groupId, userId) {
      GroupMember.removeFromGroup({groupId: groupId, id: userId}, function (user) {
        var row = $('#' + membersTable.tableId).find('tr#' + userId)[0];
        membersAddingTable.showLoader();
        membersTable.getDataTable().fnDeleteRow(row);
        membersAddingTable.getDataTable().fnDestroy();
//        $scope.users.push(user); TODO , undefined
        membersAddingTable.load();
        membersAddingTable.hideLoader();
      });
    }
  }
]);

groupMemberControllers.controller('GroupMembersAddingCtrl', ['$stateParams', '$scope', '$state', 'Group', 'GroupMember', 'User',
  function ($stateParams, $scope, $state, Group, GroupMember, User) {
    $scope.group = Group.show({id: $stateParams.groupId});
    $scope.users = User.query({}, membersAddingTable.load);

    $scope.addUserToGroup = function (groupId, userId) {
      membersTable.showLoader();
      GroupMember.assignToGroup({groupId: groupId, id: userId}, function (user) {
        var row = $('#' + membersAddingTable.tableId).find('tr#' + userId)[0];
        membersAddingTable.getDataTable().fnDeleteRow(row);
        membersTable.getDataTable().fnDestroy();
        $scope.members.push(user);
        membersTable.load();
        membersTable.hideLoader();
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
  getDataTable: function () {
    return $('#' + membersTable.tableId).dataTable();
  },
  showLoader: function () {
    $('#' + membersTable.tableId).parents('.table-responsive').find('.loader').fadeIn('fast');
  },
  hideLoader: function () {
    $('#' + membersTable.tableId).parents('.table-responsive').find('.loader').fadeOut('fast');
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
  getDataTable: function () {
    return $('#' + membersAddingTable.tableId).dataTable();
  },
  showLoader: function () {
    $('#' + membersAddingTable.tableId).parents('.table-responsive').find('.loader').fadeIn('fast');
  },
  hideLoader: function () {
    $('#' + membersAddingTable.tableId).parents('.table-responsive').find('.loader').fadeOut('fast');
  }
};