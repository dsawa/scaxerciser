'use strict';

var groupControllers = angular.module('groupControllers', []);

groupControllers.controller('GroupListCtrl', ['$scope', 'Group',
  function ($scope, Group) {
    $scope.$on('$viewContentLoaded', groupTable.load)
    $scope.groups = Group.query();
  }
]);

groupControllers.controller('GroupDetailCtrl', ['$scope', '$routeParams', 'Group',
  function ($scope, $routeParams, Group) {
    $scope.group = Group.get({
      groupId: $routeParams.groupId
    }, function (group) {
      console.log(group);
    });
  }
]);

var groupTable = {
  tableId: '#groups-table',
  tableSettings: {
    sPaging: 'groups_table_pagination',
    oLanguage: {
      sLengthMenu: "Pokaż _MENU_",
      sZeroRecords: "",
      sSearch: "",
      sInfo: "",
      sInfoEmpty: "",
      sInfoFiltered: "",
      oPaginate: {
        sFirst: "Pierwsza",
        sLast: "Ostatnia",
        sNext: "&#10093;",
        sPrevious: "&#10092;"
      }
    }
  },
  loadDelay: 300,
  load: function () {
    setTimeout(function () {
      $(groupTable.tableId).dataTable(groupTable.tableSettings);
      $(groupTable.tableId + '_filter').find('input').attr("placeholder", "Filtruj..");
    }, groupTable.loadDelay);
  }
};