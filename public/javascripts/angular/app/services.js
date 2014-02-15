'use strict';

var groupServices = angular.module('groupServices', ['ngResource']);

groupServices.factory('Group', ['$resource',
  function ($resource) {
    return $resource('api/groups/:groupId', {}, {
      query: {
        method: 'GET',
        params: {},
        isArray: true
      },
      create: {
        method: 'POST',
        params: {}
      },
      show: {
        method: 'GET',
        params: {
          groupId: '@groupId'
        }
      },
      update: {
        method: 'PUT',
        params: {
          groupId: '@groupId'
        }
      },
      delete: {
        method: 'DELETE',
        params: {
          groupId: '@groupId'
        }
      }
    });
  }
]);