#import <Cordova/CDV.h>
#import <AFNetworking/AFNetworking.h>
#import <SafariServices/SafariServices.h>



@interface OnyxbeaconPhonegap : CDVPlugin <SFSafariViewControllerDelegate>

- (void)initialiseSDK:(CDVInvokedUrlCommand*)command;
- (void)checkbluetoothState:(CDVInvokedUrlCommand*)command;
- (void)startRanging:(CDVInvokedUrlCommand*)command;



@end
