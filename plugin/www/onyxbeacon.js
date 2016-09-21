var exec = require('cordova/exec');

/**
 * Main exported module.
 * @namespace onyxbeacon
 * @class OnyxbeaconPhonegap
 */

var OnyxbeaconPhonegap = function OnyxbeaconPhonegap () {
    
    var success = function(info) {
        console.log("Successfully Received Notification", info);
        cordova.fireDocumentEvent('notificationReceived', info);
    };

    var error = function(error) {
        console.error(error);
    };

    exec(success,error,'OnyxbeaconPhonegap','initialiseSDK',[]);
}

/** 
 * startOnyxNotifications
 * used to start Receiving Notifications from Onyx
 */
OnyxbeaconPhonegap.prototype.startOnyxNotifications = function startOnyxNotifications() {
    throw new Error('Not Yet Implemented');
}

/** 
 * startOnyxNotifications
 * used to start Receiving Notifications from Onyx
 */
OnyxbeaconPhonegap.prototype.stopOnyxNotifications = function stopOnyxNotifications() {
    throw new Error('Not Yet Implemented');
}

/**
 * getBluetoothState
 * @returns {Promise<status>} - The Status of the bleutooth on the device
 * @resolve isBluetoothEnabled {boolean}- The Status of the bluetooth, On/Off
 */
OnyxbeaconPhonegap.prototype.getBluetoothState = function getBluetoothState() {
    return new Promise(function(resolve, reject) {
        exec(resolve,reject,'OnyxbeaconPhonegap','checkbluetoothState',[]);
    });
};

/** Range Beacons 
 * @returns {Promise<beacon[]>} - a promise with a beacons array. 
**/
OnyxbeaconPhonegap.prototype.rangeBeacon = function rangeBeacon() {
    return new Promise(function(resolve, reject) {
        exec(resolve,reject,'OnyxbeaconPhonegap','startRanging',[]);
    })
};


var onyxbeacon = new OnyxbeaconPhonegap();
window.onyxbeacon = onyxbeacon;

// Export module.
module.exports = onyxbeacon;
