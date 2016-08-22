#import <CoreBluetooth/CoreBluetooth.h>
#import "OnyxbeaconPhonegap.h"
#import <OnyxBeaconLib/OnyxBeacon.h>
#import <AFNetworking/AFNetworking.h>
#import <SafariServices/SafariServices.h>

@interface OnyxbeaconPhonegap ()

@property (nonatomic, strong) NSArray *coupons;

/**
  * Class-wide instance of the CDVInvolkedUrlCommand: which gets returned with the callback
  */
@property (nonatomic, strong) CDVInvokedUrlCommand *com;

@property (nonatomic, strong) CBCentralManager *bluetoothManager;
@property (nonatomic, strong) NSMutableArray *rangedBeacons;
@property (nonatomic, strong) NSString *url;
@property (nonatomic, strong) NSString *couponCallbackId;

@end

@implementation OnyxbeaconPhonegap

- (void)showSafari:(NSString *)url {
    SFSafariViewController *sfvc = [[SFSafariViewController alloc] initWithURL:[NSURL URLWithString:url]];
    sfvc.delegate = self;
    [self.viewController presentViewController:sfvc
        animated:YES
        completion:nil
    ];
}

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
    NSString* clientid = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"SAClientId"];
    NSString* secret = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"SASecret"];

    [[OnyxBeacon sharedInstance] requestAlwaysAuthorization];
    [[OnyxBeacon sharedInstance] startServiceWithClientID:clientid secret:secret];
    [[OnyxBeacon sharedInstance] setDelegate:self];
    [[OnyxBeacon sharedInstance] setContentDelegate:self];
    self.com = command;
    self.couponCallbackId = self.com.callbackId;
    self.rangedBeacons = [[NSMutableArray alloc] init];
}

- (void)checkbluetoothState:(CDVInvokedUrlCommand*)command {
    self.bluetoothManager = [[CBCentralManager alloc]
                              initWithDelegate:self
                              queue:dispatch_get_main_queue()
                              options:@{CBCentralManagerOptionShowPowerAlertKey: @(NO)}];
}

- (void)startRanging:(CDVInvokedUrlCommand*)command {
     CDVPluginResult* pluginResult = nil;

     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:self.rangedBeacons];
     [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
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

    if (self.couponCallbackId) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnCoupon];
        [pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.couponCallbackId];
    } else {
        self.couponCallbackId = self.com.callbackId;
    }
}

- (void)didRangeBeacons:(NSArray *)beacons inRegion:(OBBeaconRegion *)region {
     NSDictionary *beacon = nil;

     for (OBBeacon *b in beacons) {
        beacon = [NSDictionary dictionaryWithObjectsAndKeys:
            b.uuid ? [b.uuid UUIDString] : @"", @"uuid",
            b.major ? b.major : @"", @"major",
            b.minor ? b.minor : @"", @"minor",
            nil
        ];

         if (![self.rangedBeacons containsObject:beacon]) {
             [self.rangedBeacons addObject:beacon];
         }
     }
}


- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex {
    if (buttonIndex == 1) { // the user clicked OK
        [self showSafari:self.url];
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
    if (self.com.callbackId) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:state];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.com.callbackId];
    }

}


@end