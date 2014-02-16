'use strict';

// ----- Group Services
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

// ----- User Services
var userServices = angular.module('userServices', ['ngResource']);

userServices.factory('User', ['$resource',
  function ($resource) {
    return $resource('api/users/:id', {}, {
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
          id: '@id'
        }
      },
      update: {
        method: 'PUT',
        params: {
          id: '@id'
        }
      },
      delete: {
        method: 'DELETE',
        params: {
          id: '@id'
        }
      }
    });
  }
]);