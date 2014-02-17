$(document).ready(function() {
  $('#wrapper').on('click', 'a#shortEditChangePass', function (e) {
    e.preventDefault();
    $(this).fadeOut();
    $('#editUserFormPasswords').fadeIn();
  });

  $('a.dropdown-toggle').on('click', function (e) {
    e.preventDefault();
  });
});