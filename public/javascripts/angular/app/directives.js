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
}]);