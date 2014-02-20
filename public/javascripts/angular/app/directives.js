var customDirectives = angular.module('customDirectives', []);

customDirectives.directive('passCheck', [
  function () {
    return {
      require: 'ngModel',
      link: function (scope, elem, attrs, ctrl) {
        scope.$watch(attrs.passCheck, function (passRepeated) {
          var isValid = ctrl.$viewValue === passRepeated;
          ctrl.$setValidity('passMatch', isValid);
        });
      }
    }
  }])
  .directive('hasPermission', ['Auth',
    function (Auth) {
      return {
        link: function (scope, element, attrs, ctrl) {
          var value = attrs.hasPermission.trim();

          if (typeof value !== "string") throw "hasPermission value must be String";

          function toggleVisibilityBasedOnPermission() {
            var hasPermission = Auth.hasPermission(value);
            hasPermission ? element.show() : element.hide();
          }
          toggleVisibilityBasedOnPermission();
        }
      };
    }]);