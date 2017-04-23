'use strict';

self.addEventListener('push', function (event) {
  console.log('Received push');

  var notificationTitle = 'Received Push';
  var notificationOptions = {
    body: 'Thanks for sending this push msg.',
    tag: 'simple-push-demo-notification',
    data: {
      url: 'https://developers.google.com/web/fundamentals/getting-started/push-notifications/'
    }
  };

  // Only VAPID messages support a payload
  if (event.data) {
    var dataText = event.data.text();

    notificationOptions.body = 'Push data: \'' + dataText + '\'';

    console.log(dataText);

    // Notify the client that we've received the message, so it can update the DOM.
    self.clients.matchAll({includeUncontrolled: true}).then(function (all) {
      all.map(function (client) {
        client.postMessage(dataText);
      });
    });

    // Show the notification
    event.waitUntil(self.registration.showNotification(notificationTitle, notificationOptions));
  } else {
    // Show the notification
    event.waitUntil(self.registration.showNotification(notificationTitle, notificationOptions));
  }
});
