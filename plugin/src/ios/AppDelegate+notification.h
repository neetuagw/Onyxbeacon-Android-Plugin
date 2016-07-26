#import "AppDelegate.h"

@interface AppDelegate (notification)
- (void)applicationDidBecomeActive:(UIApplication *)application;
- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification;

@property (nonatomic, retain) NSDictionary	*launchNotification;

@end