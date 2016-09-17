window.addEventListener('load', registerServiceWorker, false);

function registerServiceWorker() {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/sw.js').then(initialiseState);
    } else {
        document.write('Service workers are not supported in this browser.');
    }
}

function initialiseState() {
    if (!('showNotification' in ServiceWorkerRegistration.prototype)) {
        document.write('Notifications aren\'t supported.');
        return;
    }

    if (Notification.permission === 'denied') {
        document.write('The user has blocked notifications.');
        return;
    }

    if (!('PushManager' in window)) {
        document.write('Push messaging isn\'t supported.');
        return;
    }

    navigator.serviceWorker.ready.then(function (serviceWorkerRegistration) {
        serviceWorkerRegistration.pushManager.getSubscription().then(function (subscription) {
                if (!subscription) {
                    subscribe();

                    return;
                }

                // Keep your server in sync with the latest subscriptionId
                sendSubscriptionToServer(subscription);
            })
            .catch(function (err) {
                document.write('Error during getSubscription()', err);
            });
    });
}

function subscribe() {
    navigator.serviceWorker.ready.then(function (serviceWorkerRegistration) {
        serviceWorkerRegistration.pushManager.subscribe(getOptions())
            .then(function (subscription) {
                return sendSubscriptionToServer(subscription);
            })
            .catch(function (e) {
                if (Notification.permission === 'denied') {
                    document.write('Permission for Notifications was denied');
                } else {
                    document.write('Unable to subscribe to push.', e);
                }
            });
    });
}

/**
 * Return an options object for a call to PushManager.subscribe.
 *
 * @returns {*}
 */
function getOptions() {
    if (window.location.search.contains('vapid')) {
        var publicKey = new Uint8Array([0x04,0xd1,0x43,0x3b,0x53,0x14,0x9c,0xda,0x71,0xd1,0x2b,0x90,0xc9,0x07,0x00,0x01,0x66,0x04,0x4d,0xad,0xbe,0x5b,0xcc,0xff,0xb9,0xce,0x6c,0xc4,0x1c,0x9f,0xfd,0x46,0x4b,0x1f,0x00,0x21,0x14,0xbc,0x04,0x0f,0x14,0xb9,0x88,0x1e,0xb9,0xe0,0xa1,0xe2,0xaf,0x2a,0xb4,0x03,0x10,0xe1,0x15,0x74,0xbb,0x0d,0x5a,0x97,0x0c,0xa8,0xe3,0x37,0xef]);

        return {
            userVisibleOnly: true,
            applicationServerKey: publicKey
        };
    } else {
        return {
            userVisibleOnly: true
        };
    }
}

function sendSubscriptionToServer(subscription) {
    var key = subscription.getKey ? subscription.getKey('p256dh') : '';
    var auth = subscription.getKey ? subscription.getKey('auth') : '';

    document.getElementById('subscription').value = JSON.stringify(subscription);

    console.log({
        endpoint: subscription.endpoint,
        key: key ? btoa(String.fromCharCode.apply(null, new Uint8Array(key))) : '',
        auth: auth ? btoa(String.fromCharCode.apply(null, new Uint8Array(auth))) : ''
    });

    return Promise.resolve();

    return fetch('/profile/subscription', {
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        },
        method: 'POST',
        body: JSON.stringify({
            endpoint: subscription.endpoint,
            key: key ? btoa(String.fromCharCode.apply(null, new Uint8Array(key))) : '',
            auth: auth ? btoa(String.fromCharCode.apply(null, new Uint8Array(auth))) : ''
        })
    });
}
