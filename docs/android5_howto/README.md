# Running MockGeoFix on Android <=5.1 - Step by Step Guide

A bit too detailed guide on how to run MockGeoFix on Android from Gingerbread to Lollipop (the process is a little different on Android 6 and newer)

1. Install MockGeoFix
  - you can install [directly from Google Play](https://play.google.com/store/apps/details?id=github.luv.mockgeofix) or [get an APK](https://github.com/luv/mockgeofix/releases) here.
2. Enable developer options
  - go to *Settings->About phone* and click on *Build number* 5 times
  
  <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/1-enable%20developer%20options.png" height="400" alt="enable developer options" />
3. Enable *Allow mock locations* in *Settings->Developer opions*
  - open *Settings->Developer opions*
  
  <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/2-developer%20options.png" height="400" alt="developer options" />
  - enable *Allow mock locations*
  
  <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/3-allow%20mock%20locations.png" height="400" alt="allow mock locations" />
4. Change location strategy to "device only"
  - open *Settings->Personal->Location* and click on *Mode* and select *Device only*
  
  <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/4-location%20device%20only.png" height="400" alt="location device only" />
5. Start MockGeoFix
  - now start MockGeoFix and click on *start* and notice the IP address that is on your Wi-Fi network 

  <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/5-start%20mockgeofix.png" height="400" alt="start mockgeofix" />
6. Telnet to MockGeoFix (port 5554)
  - Open a terminal on your computer and telnet to the IP address from the previous step to port 5554
  
  <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/6-telnet.png" height="400" alt="telnet" />

7. Bonus step: verify in Google Maps
  - Open Google Mapps and if prompted whether to improve accuracy by also using Wi-Fi for geolocation, even though it seems counter-intuitive say yes. Then go back to *Settings->Personal->Location* and change *Mode* to *Device Only*. (At least, when testing MockGeoFix on CyanogenMod12 I had to follow this strange procedure to get location mocking working in Google Mapps, YMMV.)
  
  <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/7-google%20maps.png" height="400" alt="google maps" />
  - click on the "locate me" button and here we go! (if nothing happens re-enter the geo fix command in the telnet session and try again)
  
  <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/7-google%20maps-2.png" height="400" alt="google maps" />

8. Bonus step: verify in ZachR's GPSDump
  - Another, more direct and foolproof way, to test that MockGeoFix is working is to use ZachR's GPSDump ([org.ZachR.GPSDump in Google Play](https://play.google.com/store/apps/details?id=org.ZachR.GPSDump))

    <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/8-zachr-gps%20dump.png" height="400" alt="zachr gpsdump" />
    
  - After tapping the "GPS Dump" button you should see "Lat" and "Long" fields changed to the values sent with the "geo fix" telnet command
  
  <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/8-zachr-gps%20dump-2.png" height="400" alt="zachr gpsdump" />

