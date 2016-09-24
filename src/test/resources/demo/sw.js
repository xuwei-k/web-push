/**
 * Service worker file.
 *
 * NOTE: This file MUST be located in the root.
 */

'use strict';

console.log('Started', self);

self.addEventListener('install', function (event) {
  self.skipWaiting();

  console.log('Installed', event);
});

self.addEventListener('activate', function (event) {
  console.log('Activated', event);
});

self.addEventListener('push', function (event) {
  console.log('Push message', event);

  self.clients.matchAll({includeUncontrolled: true}).then(function (all) {
    all.map(function (client) {
      client.postMessage(event.data.text());
    });
  });
});
