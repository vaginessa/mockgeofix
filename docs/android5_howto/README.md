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
  
  <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/4-location%20device%20only.png" height="400" alt="allow mock locations" />
5. Start MockGeoFix
  - now start MockGeoFix and click on *start* and notice the IP address that is on your Wi-Fi network 

  <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/5-start%20mockgeofix.png" height="400" alt="allow mock locations" />
6. Telnet to MockGeoFix (port 5554)
  - Open a terminal on your computer and telnet to the IP address from the previous step to port 5554
  
  <img src="https://raw.githubusercontent.com/luv/mockgeofix/master/docs/android5_howto/6-telnet.png" height="400" alt="allow mock locations" />

7. Bonus step: verify in Google Maps
  - Open Google Mapps and if prompted whether to improve accuracy by also using Wi-Fi for geolocation, even though it seems counterintuitive say yes.
