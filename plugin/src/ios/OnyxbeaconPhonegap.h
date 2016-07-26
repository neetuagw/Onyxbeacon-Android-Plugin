#import <Cordova/CDV.h>
#import <AFNetworking/AFNetworking.h>


@interface OnyxbeaconPhonegap : CDVPlugin

- (void)initialiseSDK:(CDVInvokedUrlCommand*)command;
- (void)checkbluetoothState:(CDVInvokedUrlCommand*)command;
- (void)startRanging:(CDVInvokedUrlCommand*)command;



@end
