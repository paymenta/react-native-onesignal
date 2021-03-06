/**
 * @providesModule Notifications
 */

'use strict';

import { NativeModules, DeviceEventEmitter, NetInfo, Platform } from 'react-native';
import invariant from 'invariant';

const { RNOneSignal } = NativeModules;

var Notifications = {
	onError: false,
	onNotificationOpened: false,
	onNotificationsRegistered: false
};

var _pendingNotifications = [];
var DEVICE_NOTIF_EVENT = 'remoteNotificationOpened';
var DEVICE_NOTIF_REG_EVENT = 'remoteNotificationsRegistered';
var DEVICE_IDS_AVAILABLE = 'idsAvailable';

/**
 * Configure local and remote notifications
 * @param {Object}		options
 * @param {function}	options.onNotificationOpened - Fired when a remote notification is received.
 * @param {function} 	options.onError - None
 */
Notifications.configure = function(options: Object) {
	if ( typeof options.onError !== 'undefined' ) {
		this.onError = options.onError;
	}

	if ( typeof options.onNotificationOpened !== 'undefined' ) {
		this.onNotificationOpened = options.onNotificationOpened;

		if (_pendingNotifications.length > 0) {
			var notification = _pendingNotifications.pop();
			this._onNotificationOpened(notification.result);
		}
	}

	if( typeof options.onNotificationsRegistered !== 'undefined' ) {
		this.onNotificationsRegistered = options.onNotificationsRegistered;
	}

	if( typeof options.onIdsAvailable !== 'undefined' ) {
		this.onIdsAvailable = options.onIdsAvailable;
	}

	function handleConnectionStateChange(isConnected) {
    if(!isConnected) return;

    RNOneSignal.configure();
    NetInfo.isConnected.removeEventListener('change', handleConnectionStateChange);
  }

  NetInfo.isConnected.fetch().then(isConnected => {
    if(isConnected) return RNOneSignal.configure();
    NetInfo.isConnected.addEventListener('change', handleConnectionStateChange);
  }).catch((...args)=>this.onError(args));
};

/* Unregister */
Notifications.unregister = function() {
	this.onNotificationOpened = false;
};

Notifications.requestPermissions = function(permissions) {
	var requestedPermissions = {};
	if (Platform.OS == 'ios') {
		if (permissions) {
			requestedPermissions = {
				alert: !!permissions.alert,
				badge: !!permissions.badge,
				sound: !!permissions.sound
			};
		} else {
			requestedPermissions = {
				alert: true,
				badge: true,
				sound: true
			};
		}
		RNOneSignal.requestPermissions(requestedPermissions);
	} else {
		console.log("This function is not supported on Android");
	}
};

Notifications.registerForPushNotifications = function(){
	if (Platform.OS == 'ios') {
		RNOneSignal.registerForPushNotifications();
	} else {
		console.log("This function is not supported on Android");
	}
};

Notifications.checkPermissions = function(callback: Function) {
	if (Platform.OS == 'ios') {
		invariant(
			typeof callback === 'function',
			'Must provide a valid callback'
		);
		RNOneSignal.checkPermissions(callback);
	} else {
		console.log("This function is not supported on Android");
	}
};

Notifications._onNotificationOpened = function(result) {
	if ( this.onNotificationOpened === false ) {
		var notification = {result: result};
		_pendingNotifications.push(notification);
		return;
	}
	this.onNotificationOpened(result);
};

Notifications._onNotificationsRegistered = function(payload) {
	if ( this.onNotificationsRegistered === false ) {
		return;
 	}
 	this.onNotificationsRegistered(payload);
};

Notifications.sendTag = function(key, value) {
	RNOneSignal.sendTag(key, value);
};

Notifications.sendTags = function(tags) {
	RNOneSignal.sendTags(tags || {});
};

Notifications.getTags = function(next) {
	RNOneSignal.getTags(next);
};

Notifications.deleteTag = function(key) {
	RNOneSignal.deleteTag(key);
};

Notifications.enableVibrate = function(enable) {
	if (Platform.OS == 'android') {
		RNOneSignal.enableVibrate(enable);
	} else {
		console.log("This function is not supported on iOS");
	}
};

Notifications.enableSound = function(enable) {
	if (Platform.OS == 'android') {
		RNOneSignal.enableSound(enable);
	} else {
		console.log("This function is not supported on iOS");
	}
};

Notifications.enableNotificationsWhenActive = function(enable) {
	if (Platform.OS == 'android') {
		RNOneSignal.enableNotificationsWhenActive(enable);
	} else {
		console.log("This function is not supported on iOS");
	}
};

Notifications.enableInAppAlertNotification = function(enable) {
	RNOneSignal.enableInAppAlertNotification(enable);
};

Notifications.setSubscription = function(enable) {
	RNOneSignal.setSubscription(enable);
};

Notifications.promptLocation = function() {
	if (Platform.OS == 'android') {
		RNOneSignal.promptLocation();
	} else {
		console.log("This function is not supported on iOS");
	}
};

Notifications.postNotification = function(contents, data, player_id) {
	if (Platform.OS == 'android') {
		RNOneSignal.postNotification(JSON.stringify(contents), JSON.stringify(data), player_id);
	} else {
		RNOneSignal.postNotification(contents, data, player_id);
	}
};

Notifications.clearOneSignalNotifications = function() {
	if (Platform.OS == 'android') {
		RNOneSignal.clearOneSignalNotifications();
	} else {
		console.log("This function is not supported on iOS");
	}
};

Notifications.cancelNotification = function(id) {
	if (Platform.OS == 'android') {
		RNOneSignal.cancelNotification(id);
	} else {
		console.log("This function is not supported on iOS");
	}
};

Notifications.idsAvailable = function(idsAvailable) {
	console.log('Please use the onIdsAvailable event instead, it can be defined in the register options');
};

DeviceEventEmitter.addListener(DEVICE_NOTIF_EVENT, function(result) {
	/*var message = notifData.message;
	var data = null;
	if (notifData.additionalData != null && notifData.additionalData != '{}') {
		if (typeof notifData.additionalData === 'object') {
			data = notifData.additionalData;
		} else {
			data = JSON.parse(notifData.additionalData);	
		}
    	}
	var isActive = notifData.isActive;*/
	Notifications._onNotificationOpened(result);
});

DeviceEventEmitter.addListener(DEVICE_NOTIF_REG_EVENT, function(notifData) {
	Notifications._onNotificationsRegistered(notifData)
});

DeviceEventEmitter.addListener(DEVICE_IDS_AVAILABLE, function(idsAvailable) {
	if (Notifications.onIdsAvailable) {
		Notifications.onIdsAvailable(idsAvailable);
	}
});

module.exports = Notifications;
