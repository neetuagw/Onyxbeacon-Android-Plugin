var exec = require('cordova/exec');

/**
 * Main exported module.
 * @namespace onyxbeacon
 */

var OnyxbeaconPhonegap = function OnyxbeaconPhonegap (){
    this.initialise();
};

//Initialise the Onyx SDK
OnyxbeaconPhonegap.prototype.initialise = function(){
    
    var success = function(info) {
        console.log("Successfully Received Notification", info);
        cordova.fireDocumentEvent('notificationReceived', info);
    };

    var error = function(error) {
        console.error(error);
    };
    
    exec(success,error,'OnyxbeaconPhonegap','initialiseSDK',[]);
}

//Check Bluetooth State
OnyxbeaconPhonegap.prototype.bluetoothState = function(success , error){
    exec(success,error,'OnyxbeaconPhonegap','checkbluetoothState',[]);
};

/** Range Beacons 
 * @returns {Promise} - a promise with a beacons array. 
**/
OnyxbeaconPhonegap.prototype.rangeBeacon = function() {
    return new Promise(function(resolve, reject) {
        exec(resolve,reject,'OnyxbeaconPhonegap','startRanging',[]);
    })
};


var onyxbeaconPhonegap = new OnyxbeaconPhonegap();
window.onyxbeacon = onyxbeacon;

// Export module.
module.exports = onyxbeaconPhonegap;
