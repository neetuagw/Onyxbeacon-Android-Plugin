//
//  OnyxBeacon.h
//  OnyxBeacon
//
//  Created by Igor Stirbu on 11/02/14.
//  Copyright (c) 2014 RomVentures SRL. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "OBContent.h"
#import "OBBeacon.h"
#import "OBBeaconRegion.h"
#import "OBTag.h"

extern NSString * const OBLocationServiceChangedNotification;
extern NSString * const OBRangedBeaconsChangedNotification;
extern NSString * const OBRangedBeaconsChangedBeaconsKey;
@class OnyxBeacon;

@protocol OnyxBeaconDelegate <NSObject>

@optional

- (void)onyxBeaconError:(NSError *)error;
- (void)didRangeBeacons:(NSArray *)beacons inRegion:(OBBeaconRegion *)region;
- (void)locationManagerDidEnterRegion:(CLRegion *)region;
- (void)locationManagerDidExitRegion:(CLRegion *)region;

@end

@protocol OnyxBeaconContentDelegate <NSObject>

- (void)didReceiveContent:(NSArray *)coupons;
- (void)didRequestInfo:(OBContent *)content inViewController:(UIViewController *)viewController;

@end

extern const NSString *OnyxBeaconErrorDomain;
typedef NS_ENUM(NSInteger, OnyxBeaconErrorCodes) {
    OnyxBeaconLocationServicesDisabled = 1,
    OnyxBeaconLocationServicesRestricted,
    OnyxBeaconErrorReadingBeaconInfo,
    OnyxBeaconTimedOutScanningBeacons,
    OnyxBeaconBackgroundRefreshDisabled,
    OnyxBeaconBackgroundRefreshRestricted,
    OnyxBeaconBluetoothDisabled,
    OnyxBeaconBLEError,
    OnyxBeaconConfigRejected,
    OnyxBeaconConfigInvalidPacket,
    OnyxBeaconAuthError,
    OnyxBeaconFirmwareUpgradeInvalidImage,
    OnyxBeaconFirmwareUpgradeDeviceReset,
    OnyxBeaconFirmwareUpgradeCouldNotConnect,
    OnyxBeaconFirmwareUpgradeInvalidResponse,
    OnyxBeaconFirmwareUpgradeInvalidBlock,
    OnyxBeaconFirmwareUpgradeImageNotFound,
    OnyxBeaconPeripheralConnTimedOutError,
};

typedef NS_ENUM(NSInteger, OBContentAction) {
    OBContentActionOpened,
    OBContentActionTapped,
    OBContentActionRemoved,
    OBContentActionLast
};

typedef void (^OnyxBeaconLogger)(NSString *message);
typedef void (^OnyxBeaconSendLogHandler)(NSError*);
typedef void (^OnyxBeaconPushNotificationsHandler)(NSDictionary*,  NSError*);

@interface OnyxBeacon : NSObject

@property (nonatomic, strong) id<OnyxBeaconDelegate> delegate;
@property (nonatomic, strong) id<OnyxBeaconContentDelegate> contentDelegate;

@property (nonatomic, assign) BOOL showBluetoothPowerAlertKey; //system alert about bluetooth status

+ (instancetype)sharedInstance;

@property (nonatomic, strong, readonly) NSString *version;

// Call in application:didFinishLaunchingWithOptions:
- (void)setYourInstallIdentifier:(NSString*)installIdentifier;
- (void)startServiceWithClientID:(NSString *)clientID secret:(NSString *)secret;
- (void)resetService;

- (void)setLogger:(OnyxBeaconLogger)logger;

// Call in applicationDidEnterBackground:
- (void)didEnterBackground;

// Call in applicationWillEnterForeground:
- (void)willEnterForeground;

// For iOS8 message for authorization should be added to Info.plist.
// Right-click on Info.plist and select Show Raw Key/Values. Add
// a new entry with key NSLocationAlwaysUsageDescription and
// value description of the reason for the 'Always' authorization.
// For 'WhenInUse' authorization add the key 'NSLocationWhenInUseUsageDescription'
// with the reason for this authorization. If the corresponding
// description will not be added then the OS will not present the
// authorization request.
- (void)requestAlwaysAuthorization;
- (void)requestWhenInUseAuthorization;

// Methods for Coupon App
- (NSArray *)getContent;
- (void)deleteContent:(OBContent *)content;

- (UIViewController *)viewControllerForContent:(OBContent *)content;
- (void)showContentInfo:(OBContent *)content inViewController:(UIViewController *)viewController;
- (void)clearCoupons;

- (void)contentOpened:(OBContent *)content;
- (void)contentTapped:(OBContent *)content;

- (void)sendUserMetrics:(NSDictionary *)userMetrics;

- (NSArray *)getTags;
- (NSSet *)getSelectedTags;
- (void)setTags:(NSSet *)tags;

- (UIViewController *)viewControllerForTags;

- (void)sendReport:(NSData *)data reporter:(NSString *)reporter message:(NSString *)message handler:(OnyxBeaconSendLogHandler)handler;

// deviceToken - Apple device token using "application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken" method
// pushProvider = @"IBMBluemix"
- (void)registerForPushNotificationWithDeviceToken:(NSData *)deviceToken
                                       forProvider:(NSString*)pushProvider
                                           handler:(OnyxBeaconPushNotificationsHandler)handler;

//providerDeviceToken = deviceToken from push provider
- (void)sendPushNotificationProviderDeviceToken:(NSString *)providerDeviceToken;

@end
