#import "OnyxbeaconPhonegap.h"


    
@implementation OnyxbeaconPhonegap

@synthesize notificationCallbackId, checkBluetoothCallbackId, RangedBeaconList, rangeBeaconsCallbackId;
    
#pragma mark - Plugin calls
    
- (void)initialiseSDK:(CDVInvokedUrlCommand*)command {
        
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
    
    notificationCallbackId = command.callbackId;
    
    RangedBeaconList = [[NSMutableArray alloc] init];
}

- (void)checkbluetoothState:(CDVInvokedUrlCommand*)command {
    self.bluetoothManager = [[CBCentralManager alloc]
                              initWithDelegate:self
                              queue:dispatch_get_main_queue()
                              options:@{CBCentralManagerOptionShowPowerAlertKey: @(NO)}];
    
    checkBluetoothCallbackId = command.callbackId;
}

- (void)startRanging:(CDVInvokedUrlCommand*)command {
     RangedBeaconList = [[NSMutableArray alloc] init];
     rangeBeaconsCallbackId = command.callbackId;
    
    [self performSelector:@selector(sendRangedBeacons) withObject:nil afterDelay:1.0];
}

-(void)sendRangedBeacons {
    CDVPluginResult* pluginResult = nil;
     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:RangedBeaconList];
     [self.commandDelegate sendPluginResult:pluginResult callbackId:rangeBeaconsCallbackId];
}

- (void)handleNotification:(NSDictionary *)coupon {
        CDVPluginResult* pluginResult = nil;
		
    	if( notificationCallbackId ) {
        	pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:coupon];
        	[pluginResult setKeepCallback:[NSNumber numberWithBool:true]];
        	[self.commandDelegate sendPluginResult:pluginResult callbackId:notificationCallbackId];
        }
  }

#pragma mark - Onyx framework methods

- (void)didReceiveContent:(NSArray *)coupons {
    [self loadContent];
}

- (void)loadContent {
    // CDVPluginResult* pluginResult = nil;
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
                coupon.couponDescription ? [NSString stringWithString:coupon.couponDescription] : @"", @"couponDescription",
                coupon.uuid ? [NSNumber numberWithInt:coupon.uuid] : @"", @"uuid",
                nil
            ];

            UILocalNotification* notification = [[UILocalNotification alloc] init];
                                 notification.alertBody = coupon.message;
                                 notification.userInfo = returnCoupon;
                                 notification.soundName = UILocalNotificationDefaultSoundName;
            
            [[UIApplication sharedApplication] presentLocalNotificationNow:notification];
        }
    }

    [[OnyxBeacon sharedInstance] clearCoupons];
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

         if (![RangedBeaconList containsObject:beacon]) {
             [RangedBeaconList addObject:beacon];
         }
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
    if (checkBluetoothCallbackId) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:state];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:checkBluetoothCallbackId];
    }

}


@end
    
    
    
    
