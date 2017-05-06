'use strict';

self.addEventListener('push', function (event) {
  console.log('Received push');

  // Decrypt the data and read it as plain text
  var dataText = event.data.text();

  // Send the data to the client, so it let the runner know we received the message
  self.clients.matchAll({includeUncontrolled: true}).then(function (all) {
    all.map(function (client) {
      client.postMessage(dataText);
    });
  });

  var title = 'Push message';
  var options = {
    body: dataText,
    tag: 'simple-push-demo-notification',
    data: {
      url: 'https://developers.google.com/web/fundamentals/getting-started/push-notifications/'
    }
  };

  // Show the notification (though not necessary for the test)
  event.waitUntil(self.registration.showNotification(title, options))
});
