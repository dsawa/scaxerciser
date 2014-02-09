'use strict';

var groupServices = angular.module('groupServices', ['ngResource']);

groupServices.factory('Group', ['$resource',
  function($resource){
    return $resource('api/groups/:groupId', {}, {
      query: {method:'GET', params: {groupId:''}, isArray:true}
    });
  }]);
