var customDirectives = angular.module('customDirectives', []);

customDirectives.directive('passCheck', [
  function () {
    return {
      require: 'ngModel',
      link: function (scope, elem, attrs, ctrl) {
        scope.$watch(attrs.passCheck, function (passRepeated) {
          if (typeof passRepeated !== 'undefined' && passRepeated.length > 0) {
            var isValid = ctrl.$viewValue === passRepeated;
            ctrl.$setValidity('passMatch', isValid);
          }
        });
      }
    }
  }])
  .directive('hasPermission', ['Auth',
    function (Auth) {
      return {
        link: function (scope, element, attrs, ctrl) {
          var permissions = attrs.hasPermission.trim().split(',').map(function (value) {
            return value.trim()
          });

          function toggleVisibilityBasedOnPermission() {
            var hasPermission = Auth.hasPermission(permissions);
            hasPermission ? element.show() : element.hide();
          }

          toggleVisibilityBasedOnPermission();
        }
      };
    }])
  .directive('hasPermissionInGroup', ['Auth',
    function (Auth) {
      return {
        restrict: 'A',
        scope: {
          hasPermissionInGroup: '='
        },
        link: function (scope, element, attrs, ctrl) {
          var groupRoles = scope.hasPermissionInGroup['groupRoles'], permissions = scope.hasPermissionInGroup['permissions'];

          function toggleVisibilityBasedOnPermission() {
            var currentUserId = Auth.getCurrentPermission()['accountId'];

            for (var i = 0; i < groupRoles.length; i += 1) {
              if (groupRoles[i]['accountId']['$oid'] === currentUserId && permissions.indexOf(groupRoles[i]['roleInGroup']) !== -1) {
                element.show();
                return true;
              }
            }
            element.hide();
            return false;
          }

          if(typeof groupRoles !== "undefined")
            toggleVisibilityBasedOnPermission();
        }
      };
    }]);