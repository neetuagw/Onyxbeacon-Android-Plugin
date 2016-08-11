#import <Cordova/CDV.h>
#import <AFNetworking/AFNetworking.h>
#import <SafariServices/SafariServices.h>

@protocol OnyxbeaconPhonegap;


@interface OnyxbeaconPhonegap : CDVPlugin <SFSafariViewControllerDelegate>

- (void)showSafari:(NSString *)url;
- (void)safariViewControllerDidFinish:(SFSafariViewController *)controller;

- (void)initialiseSDK:(CDVInvokedUrlCommand*)command;
- (void)checkbluetoothState:(CDVInvokedUrlCommand*)command;
- (void)startRanging:(CDVInvokedUrlCommand*)command;


@end
