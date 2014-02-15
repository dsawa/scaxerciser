'use strict';

var groupControllers = angular.module('groupControllers', []);

groupControllers.controller('GroupListCtrl', ['$scope', 'Group',
  function ($scope, Group) {
    $scope.$on('$viewContentLoaded', groupTable.load)
    $scope.groups = Group.query();
  }
]);

groupControllers.controller('GroupDetailCtrl', ['$stateParams', '$scope', 'Group',
  function ($stateParams, $scope, Group) {
    $scope.group = Group.get({
      groupId: $stateParams.groupId
    }, function (group) {
    });
  }
]);

var groupTable = {
  tableId: 'groups-table',
  tableSettings: {
    sPaging: 'groups_table_pagination',
    oLanguage: scaxerciserApp.dataTables.languageSettings,
    aoColumnDefs: [{ "bSortable": false, "aTargets": [ 1 ] }],
  },
  loadDelay: 300,
  load: function () {
    setTimeout(function () {
      if (!$.fn.DataTable.fnIsDataTable($('#' + groupTable.tableId))) {
        $('#' + groupTable.tableId).dataTable(groupTable.tableSettings);
//      $('#' + groupTable.tableId + '_filter').find('input').attr("placeholder", "Filtruj..");
      }
    }, groupTable.loadDelay);
  }
};