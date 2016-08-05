#import <CoreBluetooth/CoreBluetooth.h>
#import "OnyxbeaconPhonegap.h"
#import <OnyxBeaconLib/OnyxBeacon.h>
#import <AFNetworking/AFNetworking.h>
#import <SafariServices/SafariServices.h>


#define SA_CLIENTID @"430af915ede1e38d26a55d41d559279a4a6d4fe9"
#define SA_SECRET   @"898b52e410d444a9f854ecf76b586fecc560ff2d"

@interface OnyxbeaconPhonegap ()

@property (nonatomic, strong) NSArray *coupons;

/**
  * Class-wide instance of the CDVInvolkedUrlCommand: which gets returned with the callback
  */
@property (nonatomic, strong) CDVInvokedUrlCommand *com;

@property (nonatomic, strong) CBCentralManager *bluetoothManager;
@property (nonatomic, strong) NSArray *rangedBeacons;
@property (nonatomic, strong) NSString *url;

@end

@implementation OnyxbeaconPhonegap

#pragma mark - Plugin calls

- (void)initialiseSDK:(CDVInvokedUrlCommand*)command {
    /*[[OnyxBeacon sharedInstance] setLogger:^(NSString *message) {
        NSLog(@"OnyxBeacon: %@", message);
    }];*/


    // Permissions
        if ([[UIApplication sharedApplication] respondsToSelector:@selector(registerUserNotificationSettings:)]) {
            UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:(UIUserNotificationTypeBadge
                                                                                                 |UIUserNotificationTypeSound
                                                                                                 |UIUserNotificationTypeAlert)
                                                                                     categories:nil];
            [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
        }
    [[UIApplication sharedApplication] registerForRemoteNotifications];

    // Onyx
    [[OnyxBeacon sharedInstance] requestAlwaysAuthorization];
    [[OnyxBeacon sharedInstance] startServiceWithClientID:SA_CLIENTID secret:SA_SECRET];
    [[OnyxBeacon sharedInstance] setDelegate:self];
    [[OnyxBeacon sharedInstance] setContentDelegate:self];
    self.com = command;
}

- (void)checkbluetoothState:(CDVInvokedUrlCommand*)command {
    self.bluetoothManager = [[CBCentralManager alloc]
                              initWithDelegate:self
                              queue:dispatch_get_main_queue()
                              options:@{CBCentralManagerOptionShowPowerAlertKey: @(NO)}];
}

- (void)startRanging:(CDVInvokedUrlCommand*)command {
     CDVPluginResult* pluginResult = nil;
     NSDictionary *returnBeacon = nil;

     for (OBBeacon *b in self.rangedBeacons) {
        returnBeacon = [NSDictionary dictionaryWithObjectsAndKeys:
            b.uuid ? [b.uuid UUIDString] : @"", @"uuid",
            b.major ? [NSNumber numberWithInt:b.major] : @"", @"major",
            b.minor ? [NSNumber numberWithInt:b.minor] : @"", @"minor",
            nil
        ];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnBeacon];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
     }
}


#pragma mark - Onyx framework methods


- (void)didReceiveContent:(NSArray *)coupons {
    [self loadContent];
}

- (void)loadContent {
    CDVPluginResult* pluginResult = nil;
    NSDictionary *returnCoupon = nil;

    NSArray *coupons = [[OnyxBeacon sharedInstance] getContent];
    for (OBContent *coupon in coupons) {
        if (coupon.contentState == ContentStateUnread) {

            returnCoupon = [NSDictionary dictionaryWithObjectsAndKeys:
                coupon.action ? [NSString stringWithString:coupon.action] : @"", @"action",
                coupon.contentState ? [NSNumber numberWithInt:coupon.contentState] : @"0", @"contentState",
                coupon.contentType ? [NSNumber numberWithInt:coupon.contentType] : @"0", @"contentType",
                coupon.createTime ? [NSDateFormatter localizedStringFromDate:coupon.createTime dateStyle:NSDateFormatterMediumStyle timeStyle:NSDateFormatterMediumStyle] : @"", @"createTime",
                coupon.expirationDate ? [NSDateFormatter localizedStringFromDate:coupon.expirationDate dateStyle:NSDateFormatterMediumStyle timeStyle:NSDateFormatterMediumStyle] : @"", @"expirationDate",
                coupon.title ? [NSString stringWithString:coupon.title] : @"", @"title",
                coupon.path ? [NSString stringWithString:coupon.path] : @"", @"path",
                coupon.message ? [NSString stringWithString:coupon.message] : @"", @"message",
                coupon.uuid ? [NSNumber numberWithInt:coupon.uuid] : @"", @"uuid",
                nil
            ];

            self.url = [returnCoupon objectForKey:@"action"];

            UILocalNotification *notification = [[UILocalNotification alloc] init];
            notification.alertBody = coupon.message;
            notification.userInfo = @{@"url": [returnCoupon objectForKey:@"action"]};
            notification.soundName = UILocalNotificationDefaultSoundName;
            [[UIApplication sharedApplication] presentLocalNotificationNow:notification];

            UIApplicationState state = [[UIApplication sharedApplication] applicationState];
            if (state == UIApplicationStateActive) {

                if ([returnCoupon objectForKey:@"action"] == @"") {
                    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:coupon.title
                                                                    message:coupon.message
                                                                   delegate:self
                                                                   cancelButtonTitle:@"Close"
                                                                    otherButtonTitles:nil];
                    [alert show];
                } else {
                    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:coupon.title
                                                                    message:coupon.message
                                                                   delegate:self
                                                                   cancelButtonTitle:@"Close"
                                                                    otherButtonTitles:@"View", nil];
                    [alert show];
                }
            }
        }
    }

    [[OnyxBeacon sharedInstance] clearCoupons];

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnCoupon];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.com.callbackId];
}

- (void)didRangeBeacons:(NSArray *)beacons inRegion:(OBBeaconRegion *)region {
    self.rangedBeacons = beacons;
}


- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex {
    // the user clicked OK
    if (buttonIndex == 1) {
        SFSafariViewController *sfvc = [[SFSafariViewController alloc] initWithURL:[NSURL URLWithString:self.url]];
        sfvc.delegate = self;
        [self.viewController presentViewController:sfvc animated:YES completion:nil];
    }
}





#pragma mark - Bluetooth Methods

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    CDVPluginResult* pluginResult = nil;

    Boolean *state = nil;

    if ([central state] == CBCentralManagerStatePoweredOn) {
        state = YES;
    }
    else {
        state = NO;
    }

    NSLog(state ? @"BT? Yes" : @"BT? No");
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:state];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.com.callbackId];

}


@end