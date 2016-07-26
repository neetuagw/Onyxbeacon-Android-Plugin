#import "AppDelegate+notification.h"
#import "OnyxbeaconPhonegap.h"
#import <OnyxBeaconLib/OnyxBeacon.h>
#import <objc/runtime.h>

@implementation AppDelegate (notification)

// its dangerous to override a method from within a category.
// Instead we will use method swizzling. we set this up in the load call.
+ (void)load
{
    Method original, swizzled;

    original = class_getInstanceMethod(self, @selector(init));
    swizzled = class_getInstanceMethod(self, @selector(swizzled_init));
    method_exchangeImplementations(original, swizzled);
}

- (AppDelegate *)swizzled_init {
	// This actually calls the original init method over in AppDelegate. Equivilent to calling super
	// on an overrided method, this is not recursive, although it appears that way. neat huh?
	return [self swizzled_init];
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    [[OnyxBeacon sharedInstance] didEnterBackground];
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    [[OnyxBeacon sharedInstance] willEnterForeground];
}


- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification {
    if ([application applicationState] == UIApplicationStateInactive) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[notification.userInfo objectForKey:@"url"]]];
    }
}


@end