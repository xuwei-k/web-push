window.addEventListener('load', registerServiceWorker, false);

function registerServiceWorker() {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/sw.js').then(initialiseState);
    } else {
        console.warn('Service workers are not supported in this browser.');
    }
}

function initialiseState() {
    if (!('showNotification' in ServiceWorkerRegistration.prototype)) {
        console.warn('Notifications aren\'t supported.');
        return;
    }

    if (Notification.permission === 'denied') {
        console.warn('The user has blocked notifications.');
        return;
    }

    if (!('PushManager' in window)) {
        console.warn('Push messaging isn\'t supported.');
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
        .catch(function(err) {
            console.warn('Error during getSubscription()', err);
        });
    });
}

function subscribe() {
    const publicKey = new Uint8Array([0x04,0xe1,0xfc,0x9d,0x34,0x00,0xe6,0x26,0x61,0x97,0x6d,0xfe,0x34,0x2c,0xc6,0x1b,0xda,0x6b,0xbc,0xe6,0x79,0x04,0x4d,0x0c,0x25,0x70,0x56,0xf8,0x65,0x24,0x40,0x8b,0xd1,0x55,0x35,0x41,0xdf,0x62,0x71,0x99,0x7d,0x15,0xd6,0x3e,0xb3,0xd2,0xbe,0xeb,0x9d,0x3e,0xfe,0x6e,0x08,0xba,0x7f,0x68,0x39,0x7c,0xc3,0xe9,0x02,0x1e,0x5b,0xae,0xa3]);

    navigator.serviceWorker.ready.then(function (serviceWorkerRegistration) {
        serviceWorkerRegistration.pushManager.subscribe({
            userVisibleOnly: true,
            // applicationServerKey: publicKey
        })
        .then(function (subscription) {
            return sendSubscriptionToServer(subscription);
        })
        .catch(function (e) {
            if (Notification.permission === 'denied') {
                console.warn('Permission for Notifications was denied');
            } else {
                console.error('Unable to subscribe to push.', e);
            }
        });
    });
}

function sendSubscriptionToServer(subscription) {
    var key = subscription.getKey ? subscription.getKey('p256dh') : '';
    var auth = subscription.getKey ? subscription.getKey('auth') : '';

    window.subscription = JSON.stringify(subscription);

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