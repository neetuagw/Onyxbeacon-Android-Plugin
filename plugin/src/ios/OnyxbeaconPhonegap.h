#import <Cordova/CDV.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import <OnyxBeaconLib/OnyxBeacon.h>
#import <AFNetworking/AFNetworking.h>

@protocol OnyxbeaconPhonegap;


@interface OnyxbeaconPhonegap : CDVPlugin
    
@property (nonatomic, strong) NSString* notificationCallbackId;
@property (nonatomic, strong) NSString* checkBluetoothCallbackId;
@property (nonatomic, strong) NSString* rangeBeaconsCallbackId;

@property (nonatomic, strong) NSMutableArray* RangedBeaconList;

@property (nonatomic, strong) NSArray *coupons;
@property (nonatomic, strong) CBCentralManager *bluetoothManager;
@property (nonatomic, strong) NSMutableArray *rangedBeacons;
@property (nonatomic, strong) NSString *url;  


- (void)initialiseSDK:(CDVInvokedUrlCommand *)command;
- (void)checkbluetoothState:(CDVInvokedUrlCommand *)command;
- (void)startRanging:(CDVInvokedUrlCommand *)command;
- (void)handleNotification:(NSDictionary *)coupon;

@end

    