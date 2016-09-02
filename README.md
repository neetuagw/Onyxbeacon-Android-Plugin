# **Onyx Cordova/Phonegap Plugin** #

This plugin specifically developed for hybrid applications. Once a beacon is in range it will fire an event with a beacon content and also sends notification.

## **Installation** ##

You need to provide your application ClientID and Secret key ( Onyx provides the ClientID and Secret key on register your application in their backend.) as a variable while installing the plugin. You can install the plugin in following way:


```
#!python

$ cordova plugin add cordova-plugin-facebook4 --save --variable APP_ID="123456789" --variable APP_NAME="myApplication"
```