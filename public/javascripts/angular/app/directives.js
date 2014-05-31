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
  .directive('checkPermissionInGroup', ['Auth',
    function (Auth) {
      return {
        restrict: 'A',
        scope: {
          group: '=groupToCheck',
          permissions: "@allowPermission"
        },
        link: function ($scope, $element, $attrs, ctrl) {
          var permissionsThatAllow, toggleVisibilityBasedOnPermission;

          permissionsThatAllow = $scope.permissions.trim().split(',').map(function (value) {
            return value.trim();
          });

          toggleVisibilityBasedOnPermission = function (group) {
            Auth.hasPermissionInGroup(group, permissionsThatAllow) ? $element.show() : $element.hide();
          };

          $scope.$watch($attrs['groupToCheck'], function (value) {
            if (typeof value.$promise === 'undefined') {
              toggleVisibilityBasedOnPermission(value);
            } else {
              value.$promise.then(function (group) {
                toggleVisibilityBasedOnPermission(group);
              })
            }
          });
        }
      };
    }
  ]);