# **Onyx Cordova/Phonegap Plugin** #

This plugin specifically developed for hybrid applications. Once a beacon is in range it will fire an event with a beacon content and also sends notification.

## **Supported Platforms** ##

**1. iOS** 

**2. Android**

## **Installation** ##

To use this plugin you need to make sure that you have registered your application to Onyx CMS and received ClientID and Secret key. 

**1.** You need to provide ClientId and Secret Key as a variable while installing the plugin. You can install the plugin in following way:


```
#!python

cordova plugin add https://bitbucket.org/spartadigital/onyx-cordovaplugin.git --save --variable SA_CLIENTID="123456789" --variable SA_SECRET="myApplication"
```
**2.** **[ANDROID ONLY] - ** Add the following code in Android Manifest file:
You can find the file here : application > platform > android > AndroidManifest.xml


```
#!python

<provider
	android:authorities="your.package.name.provider"
	android:name="com.onyxbeaconservice.provider.IBeaconProvider"
	android:enabled="true"
	android:exported="true"></provider>
```
**3.** **[ANDROID ONLY] - ** Add notification icon image called **'ic_notification.png'** in your android resources here : application > platform > android > res > drawable. You can also make a copy of your application icon image and name it **'ic_notification.png'**

## **Methods** ##

**1.** Check Bluetooth State

```
#!python


onyxbeacon.bluetoothState(function(){//success}, function(){//error});
```


**2.** Receive content on click notification:
Call following function on device ready

```
#!java
document.addEventListener('notificationReceived', function(info) {
    console.log("Content", info);
    var title = info.title, 
    message  = info.message ? info.message : '', 
    dealid   = info.description ? info.description.trim(): '',
    dealUrl  = info.action ? encodeURI( info.action ) : '',
    type     = info.contentType ? info.contentType : 0;
}

```

**3.** Range list of beacons around [Only for IOS]


```
#!python


onyxbeacon.rangeBeacons().then(function(info) { //success }, function(e) { //err });

```