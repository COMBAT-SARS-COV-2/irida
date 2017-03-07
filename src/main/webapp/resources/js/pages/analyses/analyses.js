(function (angular, _, moment, tl, page) {
  "use strict";

  /**
   * Service to hold the current state of the analyses filter
   * @returns {{search: string}}
   * @constructor
   */
  function AnalysisFilterService() {
    return {};
  }

  /**
   * The filter function for the analyses.
   * @param filter
   * @returns {Function}
   * @constructor
   */
  function AnalysesFilter(filter, $rootScope) {
    function _filterAnalysis(analysis) {
      var result = true;
      _.forOwn(filter, function (value, key) {
        var item = analysis[key];
        if (key === 'minDate' || key == 'maxDate') {
          item = analysis['createdDate'];
        }
        if(item === null) return;
        
        if(key === 'analysisState' || key === 'workflowId') {
          if(value.length > 0 && value !== 'ALL' && item !== value) result = false;
        }
        else if (key === 'minDate' && item < value) {
          result = false;
        }
        else if (key === 'maxDate' && item > value) {
          result = false;
        }
        else if (angular.isString(value) && item.toLowerCase().indexOf(value.toLowerCase()) === -1) {
          result = false;
        }
      });
      return result;
    }

    return function (analyses) {
      var filtered = _.filter(analyses, function (analysis) {
        return _filterAnalysis(analysis)
      });

      if(filtered.length === 0) {
        $rootScope.$broadcast('NO_ANALYSIS');
      }
      else {
        $rootScope.$broadcast('ANALYSIS_AVAILABLE');
      }
      return filtered;
    }
  }

  /**
   * Service for querying the server for analyses
   * @param $http
   * @returns {{load: _loadData}}
   * @constructor
   */
  function AnalysisService($http) {
    function _loadData() {
      return $http.get(page.URLS.analyses)
        .error(function(data) {
          window.location = tl.BASE_URL + data.error.url;
        });
    }

    return {
      load: _loadData
    }
  }

  /**
   * Handles events in the view for the filter.
   * @param filter
   * @constructor
   */
  function FilterController(filter) {
    var vm = this;
    vm.showAnalyses = true;
    vm.opened = {};

    function _setDefaults() {
      vm.search = "";
      vm.state = "ALL";
      vm.workflowId = "ALL";
      vm.minDate = "";
      vm.maxDate = "";
      vm.submitter = "";
      vm.max = new Date();
    }

    vm.clear = function () {
      _.forOwn(filter, function (value, key) {
        delete filter[key];
      });
      _setDefaults();
    };

    vm.doSearch = function () {
      filter.label = vm.search;
    };

    vm.searchSubmitter = function() {
      filter.submitter = vm.submitter;
    };

    vm.doState = function () {
      filter.analysisState = vm.state;
    };

    vm.doType = function () {
      filter.workflowId = vm.workflowId;
    };

    vm.open = function (e, value) {
      e.preventDefault();
      e.stopPropagation();
      vm.opened[value] = true;
    };

    vm.doDateFilter = function (key) {
      var value = vm[key];
      if (value === null) {
        delete filter[key];
        return;
      }
      var date = new Date(value);
      if (key === 'maxDate') {
        date.setDate(date.getDate() + 1);
      }
      filter[key] = date.getTime();
    };

    _setDefaults();
  }

  /**
   * Controller for the actual analyses list.
   * @constructor
   * @param svc
   * @param ngTableParams
   * @param $scope
   */
  function AnalysisController(svc, ngTableParams, $scope) {
    var vm = this;
        vm.analyses = [];
    vm.loading = true;

    vm.convertTime = function(duration) {
      if(!isNaN(duration)) {
        return moment.duration(parseInt(duration)).humanize();
      }
      return "";
    };

    vm.tableParams = new ngTableParams({
      sorting: {'createdDate':'desc'}
    });

    vm.createClass = function(state) {
      return  state.toLowerCase().replace(" ", "_");
    };

    vm.download = function (id) {
      var iframe = document.createElement("iframe");
      iframe.src = page.URLS.download + id;
      iframe.style.display = "none";
      document.body.appendChild(iframe);
    };

    vm.busy = svc.load()
      .then(function(data) {
        vm.analyses = data.analyses;
        vm.loading = false;
      });

    $scope.$on('NO_ANALYSIS', function () {
      vm.showAnalyses = false;
    });

    $scope.$on('ANALYSIS_AVAILABLE', function () {
      vm.showAnalyses = true;
    })
  }

  angular.module('irida.analysis.user', ['ngTable'])
    .filter('analysesFilter', ['analysisFilterService', '$rootScope', AnalysesFilter])
    .service('analysisService', ['$http', AnalysisService])
    .service('analysisFilterService', [AnalysisFilterService])
    .controller('analysisController', ['analysisService', 'ngTableParams', '$scope', AnalysisController])
    .controller('filterController', ['analysisFilterService', FilterController])
  ;
})(window.angular, window._, window.moment, window.TL, window.PAGE);