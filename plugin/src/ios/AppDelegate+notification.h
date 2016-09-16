#import "AppDelegate.h"
#import "OnyxbeaconPhonegap.h"
#import <SafariServices/SafariServices.h>

@interface AppDelegate (notification) <SFSafariViewControllerDelegate>
    
@property (nonatomic, retain) NSDictionary	*launchNotification;

- (void)applicationDidBecomeActive:(UIApplication *)application;
- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification;

@end
    