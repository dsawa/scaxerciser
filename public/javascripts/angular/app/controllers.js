'use strict';

var groupControllers = angular.module('groupControllers', []);

groupControllers.controller('GroupListCtrl', ['$scope', 'Group',
  function ($scope, Group) {
    $scope.$on('$viewContentLoaded', groupTable.load)
    $scope.groups = Group.query();

    $scope.deleteGroup = function(groupId) {
      var row = $('#' + groupTable.tableId).find('tr#' + groupId)[0];
      Group.delete({groupId: groupId});
      $('#' + groupTable.tableId).dataTable().fnDeleteRow(row);
    };
  }
]);

groupControllers.controller('GroupDetailCtrl', ['$stateParams', '$scope', '$state', '$location', 'Group',
  function ($stateParams, $scope, $state, $location, Group) {
    $scope.updateGroup = function () {
      var params = $scope.group;
      params.groupId = $stateParams.groupId;
      Group.update(params);
      $state.transitionTo('groups-reload');
      $location.path('groups');
    };
    $scope.group = Group.show({
      groupId: $stateParams.groupId
    })
  }
]);

groupControllers.controller('GroupCreationCtrl', ['$scope', '$state', '$location', 'Group',
  function ($scope, $state, $location, Groups) {
    $scope.createGroup = function () {
      Groups.create($scope.group);
      $state.transitionTo('groups-reload');
      $location.path('groups');
    }
  }
]);

var groupTable = {
  tableId: 'groups-table',
  tableSettings: {
    sPaging: 'groups_table_pagination',
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