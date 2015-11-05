# FlickrSearch
A simple Flickr browser.
This is an example of Android application and example os some techniques for automated testing (final tests coverage is insufficient for real applications).

FlickrSearch takes your search request and asks Flickr for public photos relevant to that request and shows the result in ListView. Each ListView's item can be clicked to open in dedicated Activity for comfortable watching.


Cache
-----
Disk cache for network responses is used in this application. 
Every time when a search request is given, the Cache is checked for stored responses with URL as a key. 
If stored response exists then it will be used and no network transmission happened.  

If "Refresh" button is pressed then the Cache won't be used.
The expiration time for responses in the Cache for online mode is stored at RestClient.CACHE_EXPIRATION_TIME (1 hour by default).

The **offline mode** (when Internet connection is unavailable) is supported. 
Stored responses will be used if they exists else a error message will appear. 
The expiration time for offline mode is stored at RestClient.CACHE_STALE_TOLERANCE (1 day by default).

-----


**To install application on device:**

Run command (do not forget setup ANDROID_HOME environment variable):
* for Purple flavor:
    * `./gradlew installPurpleColoredRelease` # on Linux, MacOS
    * `./gradlew.bat installPurpleColoredRelease` # on Windows
* or for Green flavor:   
    * `./gradlew installGreenColoredRelease` # on Linux, MacOS
    * `./gradlew.bat installGreenColoredRelease` # on Windows

------

**To run Android Instrumentation Tests run:**

(device should be attached and unlocked)
* `./gradlew cAT` # on Linux, MacOS
* `./gradlew.bat cAT` # on Windows

**To run JUnit test run:**
* `./gradlew test` # on Linux, MacOS
* `./gradlew.bat test` # on Windows

------

**Used 3d party Libraries:**
* Retrofit (OkHttp, Gson) http://square.github.io/retrofit/
* Picasso http://square.github.io/picasso/
* ListViewAnimations https://github.com/nhaarman/ListViewAnimations

**For tests:**
* Espresso https://google.github.io/android-testing-support-library/
* JUnit4 http://junit.org/
* Hamcrest http://hamcrest.org/
* MockWebServer https://github.com/square/okhttp/tree/master/mockwebserver
