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

//Range Beacons
OnyxbeaconPhonegap.prototype.rangeBeacon = function(success , error){
    var success = function(info) {
        console.log("Successfully Received Beacons List", info);
        cordova.fireDocumentEvent('beaconsReceived', info);
    };

    var error = function(error) {
        console.error(error);
    };

// On receive list of beacons around device send a callback with a list of beacons data. Read more about Callback in Phonegap Plugin documentation

    exec(success,error,'OnyxbeaconPhonegap','startRanging',[]);
};


var onyxbeaconPhonegap = new OnyxbeaconPhonegap();


// Export module.
module.exports = onyxbeaconPhonegap;
