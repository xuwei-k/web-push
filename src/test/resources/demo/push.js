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
 * Get the VAPID public key from the URL if present and return an options
 * object for a call to PushManager.subscribe.
 *
 * @returns {*}
 */
function getOptions() {
    if (window.location.search != '') {
        // Strip off the question mark
        var publicKeyBase64Url = window.location.search.substring(1);

        // Turn url-safe base64 to normal base64
        var publicKeyBase64 = publicKeyBase64Url
            .replace(/-/g, '+')
            .replace(/_/g, '/');

        var publicKey = new Uint8Array(atob(publicKeyBase64).split("").map(function (c) {
            return c.charCodeAt(0);
        }));

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
