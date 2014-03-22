var groupsBloodhound;

$(document).ready(function () {
  var $groupSelectModal = $('#groupSelectModal'), $groupsSelect, goToAddNewAssignmentForm;

  $groupsSelect = $groupSelectModal.find('.typeahead');

  goToAddNewAssignmentForm = function (groupId) {
    if (typeof groupId !== 'undefined' && groupId !== '') {
      $groupSelectModal.modal('hide');
      $groupsSelect.typeahead('val', '');
      window.location.hash = '#/groups/' + groupId + '/assignments/new'
    }
  };

  groupsBloodhound = new Bloodhound({
    datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name'),
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    remote: {
      url: '/api/groups',
      filter: function (data) {
        var filteredData = [], substrRegex = new RegExp($groupsSelect.val(), 'i');
        for (var i = 0; i < data.length; i++) {
          if (substrRegex.test(data[i].name)) {
            filteredData.push(data[i]);
          }
        }
        return filteredData;
      }
    }
  });
  groupsBloodhound.initialize();

  $groupsSelect.typeahead(null, {
    name: 'groups',
    displayKey: 'name',
    source: groupsBloodhound.ttAdapter()
  });

  $groupSelectModal.on('shown.bs.modal', function () {
    $(this).find('input').focus();
  });

  $groupsSelect.on('typeahead:selected', function (e, data) {
    goToAddNewAssignmentForm(data['_id']['$oid']);
  });

  $('#wrapper').on('click', 'a#shortEditChangePass', function (e) {
    e.preventDefault();
    $(this).fadeOut();
    $('#editUserFormPasswords').fadeIn();
  });

  $('a.dropdown-toggle').on('click', function (e) {
    e.preventDefault();
  });

});