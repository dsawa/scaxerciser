$(document).ready(function() {
  $('#groups-table').dataTable({
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
  });

  $('.dataTables_filter').find('input').attr("placeholder", "Filtruj..");
});