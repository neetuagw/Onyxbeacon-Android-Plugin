//
//  OBBeacon.h
//  OnyxBeacon
//
//  Created by Igor Stirbu on 12/12/13.
//  Copyright (c) 2013 RomVentures SRL. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

@class OBDevice;

#define OBBeaconUpdatedProximity @"OBBeaconUpdatedProximity"
#define OBBeaconUpdatedProximityBeaconKey @"beacon"

@interface OBBeacon : NSObject<NSCoding>

@property (nonatomic, strong, readonly) NSUUID *uuid;
@property (nonatomic, strong, readonly) NSNumber *major;
@property (nonatomic, strong, readonly) NSNumber *minor;

@property (nonatomic, assign, readonly) CLProximity proximity;
@property (nonatomic, assign, readonly) CLProximity lastProximity;
@property (nonatomic, assign, readonly) BOOL proximityChanged;
@property (nonatomic, assign, readonly) NSInteger rssi;
@property (nonatomic, readonly) CLLocationAccuracy accuracy;

@property (nonatomic, strong, readonly) NSDate *rangedTime;
@property (nonatomic, strong, readonly) NSDate *lastSeen;
@property (nonatomic, strong, readonly) NSString *umm;

@property (nonatomic, strong, readonly) OBDevice *device;

@property (nonatomic, assign, readonly) BOOL realTime;

@property (nonatomic, strong, readonly) NSSet *tags;


+ (instancetype)beaconWithCLBeacon:(CLBeacon *)beacon;
- (BOOL)isEqualToCLBeacon:(CLBeacon *)beacon;
- (void)updateWithBeacon:(CLBeacon *)beacon;
- (BOOL)expired;

@end
