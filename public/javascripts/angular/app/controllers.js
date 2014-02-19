'use strict';

// ----- Group Controllers
var groupControllers = angular.module('groupControllers', []);

groupControllers.controller('GroupListCtrl', ['$scope', 'Group',
  function ($scope, Group) {
    $scope.$on('$viewContentLoaded', groupTable.load)
    $scope.groups = Group.query();

    $scope.deleteGroup = function(groupId) {
      var row = $('#' + groupTable.tableId).find('tr#' + groupId)[0];
      Group.delete({groupId: groupId}, function () {
              $('#' + groupTable.tableId).dataTable().fnDeleteRow(row);
      });
    };
  }
]);

groupControllers.controller('GroupDetailCtrl', ['$stateParams', '$scope', '$state', '$location', 'Group',
  function ($stateParams, $scope, $state, $location, Group) {
    $scope.updateGroup = function () {
      var params = $scope.group;
      params.groupId = $stateParams.groupId;
      Group.update(params, function () {
        $state.transitionTo('groups-reload');
              $location.path('groups');
      });

    };
    $scope.group = Group.show({
      groupId: $stateParams.groupId
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
    aoColumnDefs: [{
      "bSortable": false,
      "aTargets": [1]
    }],
  },
  loadDelay: 500,
  load: function () {
    setTimeout(function () {
      if (!$.fn.DataTable.fnIsDataTable($('#' + groupTable.tableId))) {
        $('#' + groupTable.tableId).dataTable(groupTable.tableSettings);
        //      $('#' + groupTable.tableId + '_filter').find('input').attr("placeholder", "Filtruj..");
      }
    }, groupTable.loadDelay);
  }
};

// ----- User Controllers

var userControllers = angular.module('userControllers', []);

userControllers.controller('UserListCtrl', ['$scope', 'User',
  function ($scope, User) {
    $scope.$on('$viewContentLoaded', userTable.load)
    $scope.users = User.query();

    $scope.deleteUser = function(userId) {
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
    aoColumnDefs: [{
      "bSortable": false,
      "aTargets": [2]
    }],
  },
  loadDelay: 500,
  load: function () {
    setTimeout(function () {
      if (!$.fn.DataTable.fnIsDataTable($('#' + userTable.tableId))) {
        $('#' + userTable.tableId).dataTable(userTable.tableSettings);
      }
    }, userTable.loadDelay);
  }
};