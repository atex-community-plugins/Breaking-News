window.jc = window.jc || {};
window.jc.breakingNews = window.jc.breakingNews || {};

(function(global) {
    "use strict";

    window.console = window.console || { log: function(s) {} };

    window.console.debug = window.console.debug || function(s) { window.console.log(s); };
    window.console.info = window.console.info || function(s) { window.console.log(s); };
    window.console.warn = window.console.warn || function(s) { window.console.log(s); };
    window.console.error = window.console.error || function(s) { window.console.log(s); };

    window.jc.breakingNews = (function() {

      // this will hold all the breakingNews instances
      // it will also be used to check if a breakingNews
      // (identified by the article contentId) has already
      // been initialized.

      var initMap = {};

      // those variables will be initialized in
      // the init method, they can be overridden
      // passing a dictionary with the named variable.

      var initialDelay, debug, cookieExpire, cookieName;

      function log(msg) {
        if (debug) {
          console.debug(msg);
        }
      }

      // object that will be instantiated for each breaking news
      // it will handle the counter and cookie handling.

      var breakingNewsBanner = function(breakingNews) {
        var $breakingNews = breakingNews;
        var $breakingNewsClose = breakingNews.find('.js-breaking-news-close');
        var $breakingNewsCount = breakingNews.find('.js-count');
        var count = $breakingNewsCount.data('count');
        var contentId = breakingNews.data('contentid');
        var counterInterval;

        return {
          hideBanner: function() {
            clearInterval(counterInterval);
            $breakingNews.removeClass('is-open');
          },

          displayBanner: function() {
            log('start display');

            $breakingNewsCount.html(count);
            $breakingNews.addClass('is-open');

            var self = this;

            counterInterval = setInterval(function () {
              if (count > 0) {
                $breakingNewsCount.html(count--);
              } else {
                self.hideBanner();
              }
            }, 1000);

          },

          checkArticleInCookie: function(articleId) {
            var articles = $.cookie(cookieName);
            if (articles) {
              var articlesArray = articles.split('.');
              log('array ' + articlesArray);
              for (var i = 0; i < articlesArray.length; i++) {
                if (articlesArray[i] == articleId) {
                  return true;
                }
              }
            }
            return false;
          },

          addArticleInCookie: function (articleId) {
            if (!this.checkArticleInCookie(articleId)) {
              log('adding ' + articleId);
              var articles = $.cookie(cookieName) || '';
              var articlesArray = (articles.length > 0) ? articles.split('.') : [];
              articlesArray.push(articleId);
              log(articlesArray);
              articles = articlesArray.join('.');
              $.cookie(cookieName, articles, {
                  path: "/",
                  expires: cookieExpire
              });
            }
          },

          init: function(articleId) {
            if (this.checkArticleInCookie(articleId)) {
              log(articleId + ' is hidden');
            } else {
              var self = this;

              setTimeout(function() {
                self.displayBanner();
              }, initialDelay);

              $breakingNewsClose.on('click', function() {
                self.hideBanner();
                self.addArticleInCookie(articleId);
              });
            }
          }
        };
      };

      // initialize all the widgets, the options argument is optional
      // and can be used to set various properties.

      function init(options) {
        options = options || {};
        debug = options.debug || false;
        initialDelay = options.initialDelay || 2000;
        cookieExpire = options.cookieExpire || 10;
        cookieName = options.cookieName || 'hiddenbreakingnews';

        console.info('Initializing Breaking News');

        // look for all the breaking news and initialize then
        // only once (the same breaking news may appear twice
        // because, in some projects, a sticky header is used
        // which "copy" the html element)".

        $('.js-breaking-news').each(function(index) {
          var $breakingNews = $(this);

          var contentId = $breakingNews.data('contentid');
          log(contentId);

          if (initMap[contentId] === undefined) {
            var banner = breakingNewsBanner($breakingNews);
            initMap[contentId] = banner;
            banner.init(contentId);
          } else {
            log('already shown');
          }
        });
      }

      // Public interface
      return {
          init:     init
      };
    })();

    //window.jc.breakingNews.init({ 'debug': true });
    window.jc.breakingNews.init();

}(this));
