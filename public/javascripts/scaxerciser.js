var groupsBloodhound;

$(document).ready(function () {
  var $groupSelectModal = $('#groupSelectModal'), $addNewAssignmentLink = $('#addNewAssignment'),
    $showAssignmentsListLink = $('#showAssignmentsList'),
    $groupsSelect, goToAddNewAssignmentForm, goToAssignmentsList;

  $groupsSelect = $groupSelectModal.find('.typeahead');

  goToAddNewAssignmentForm = function (groupId) {
    var $assignmentForm;
    if (typeof groupId !== 'undefined' && groupId !== '') {
      $groupSelectModal.modal('hide');
      $groupsSelect.typeahead('val', '');
      window.location.hash = '#/groups/' + groupId + '/assignments/new';
      setTimeout(function () {
        $assignmentForm = $('#assignmentForm');
        $assignmentForm.find('input').val('');
        $assignmentForm.find('textarea').val('');
        $assignmentForm.find('button').attr('disabled', false);
      }, 100);
    }
  };

  goToAssignmentsList = function (groupId) {
    if (typeof groupId !== 'undefined' && groupId !== '') {
      $groupSelectModal.modal('hide');
      $groupsSelect.typeahead('val', '');
      $('div[ui-view=main]').html('');
      window.location.hash = '#/groups/' + groupId + '/assignments';
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

  $addNewAssignmentLink.on('click', function (e) {
    e.preventDefault();
    $groupSelectModal.find('#modalNextAction').val('new');
    $groupSelectModal.modal({ show: true, backdrop: false });
  });

  $showAssignmentsListLink.on('click', function (e) {
    e.preventDefault();
    $groupSelectModal.find('#modalNextAction').val('list');
    $groupSelectModal.modal({ show: true, backdrop: false });
  });

  $groupSelectModal.on('shown.bs.modal', function () {
    $(this).find('input').focus();
  });

  $groupsSelect.on('typeahead:selected', function (e, data) {
    var whereToGo = $groupSelectModal.find('#modalNextAction').val();
    if (whereToGo === 'list') {
      goToAssignmentsList(data['_id']['$oid']);
    } else {
      goToAddNewAssignmentForm(data['_id']['$oid']);
    }
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