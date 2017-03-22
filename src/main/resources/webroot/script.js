// script.js

// create the module and name it blogApp
var blogApp = angular.module('blogApp', ['ngRoute','ngAnimate','ui.bootstrap']);

//ng-routes
blogApp.config(function($routeProvider) {
        $routeProvider

            // route for the home page
            .when('/', {
                templateUrl : 'pages/home.html',
                controller  : 'mainCtrl'
            })

            // route for the about page
            .when('/about', {
                templateUrl : 'pages/about.html',
                controller  : 'aboutCtrl'
            })

            // route for the notes page
            .when('/notes', {
                templateUrl : 'pages/notes.html',
                controller  : 'notesCtrl'
            })

            .otherwise({
                           redirectTo: '/'
                        });

    });

// create the controller and inject Angular's $scope
blogApp.controller('mainCtrl', function($scope, $http, $log, $sce) {
    $scope.blogSection = 'Main';

    $scope.maxSize = 5;
    $scope.bigCurrentPage = 1;


    $scope.trustBlogHtml = function(html) {
              return $sce.trustAsHtml(html);
            };

    //categories
    $http({
      method: 'GET',
      url: 'rest/blog/listCategories'
    }).then(function successCallback(response) {
        // this callback will be called asynchronously
        // when the response is available
        $scope.categories = response;
      }, function errorCallback(response) {
        // called asynchronously if an error occurs
        // or server returns response with an error status.
        $scope.errorResponse = response;
      });


    //when page changes, fetch new data
    $scope.pageChanged = function() {
        $http.get('rest/blog/blogItems/' + $scope.blogSection + '/' + $scope.bigCurrentPage).then(function(response) {
                   $scope.blogItems = response.data;
                   window.scrollTo(0,0);
            });
      };

    //function to get updated blog count, specially when section changes
    $scope.getBlogCount = function() {
    //blog count
       $http.get('rest/blog/blogCount/'+$scope.blogSection).then(function(response) {
                      $scope.bigTotalItems = response.data;
           });
    };

    $scope.getBlogCount();
    $scope.pageChanged();


});

// create the controller and inject Angular's $scope
blogApp.controller('aboutCtrl', function($scope) {

});

// create the controller and inject Angular's $scope
blogApp.controller('notesCtrl', function($scope) {

});
