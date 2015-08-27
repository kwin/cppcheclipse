<wiki:gadget url="http://eclipse-marketplace-gadgets.googlecode.com/svn/files/favorite/v1/gadget.xml" title="Favorite this @ Eclipse Marketplace" width ="200" height ="70" up\_nodeNo="1027" border="0"/>&lt;wiki:gadget url="http://www.ohloh.net/p/463456/widgets/project\_users\_logo.xml" height="43" border="0"/&gt;&lt;wiki:gadget url="http://www.ohloh.net/p/463456/widgets/project\_thin\_badge.xml" height="38" border="0"/&gt;

cppcheclipse is an Eclipse plugin which integrates cppcheck (http://sourceforge.net/projects/cppcheck/) with the CDT project. You can run/configure cppcheck from the Eclipse UI.

Click on [Installation](wiki/Installation) for help how to install this plugin.

### Development ###
The latest code is built with Cloudbees at http://cppcheclipse.ci.cloudbees.com/.
Latest build status: http://cppcheclipse.ci.cloudbees.com/job/cppcheclipse%20CI/badge/icon?name=status.png|http://cppcheclipse.ci.cloudbees.com/job/cppcheclipse%20CI/


### New Update Site ###
cppcheclipse has moved from googlecode.com to eclipselabs.org. Therefore the URL of the Update Site has changed to http://cppcheclipse.eclipselabs.org.codespot.com/svn/update/. You have to [add that update site url manually](wiki/Installation) in your Eclipse installation, even if you have a previous version of cppcheclipse installed.

## News ##
  * 2014/02/29 - Version 1.0.0 fixes the following issues: [issue 39](https://code.google.com/p/cppcheclipse/issues/detail?id=39), [issue 44](https://code.google.com/p/cppcheclipse/issues/detail?id=44), [issue 54](https://code.google.com/p/cppcheclipse/issues/detail?id=54), [issue 56](https://code.google.com/p/cppcheclipse/issues/detail?id=56), [issue 57](https://code.google.com/p/cppcheclipse/issues/detail?id=57), [issue 58](https://code.google.com/p/cppcheclipse/issues/detail?id=58), [issue 60](https://code.google.com/p/cppcheclipse/issues/detail?id=60), [issue 63](https://code.google.com/p/cppcheclipse/issues/detail?id=63) and [issue 65](https://code.google.com/p/cppcheclipse/issues/detail?id=65). Please observe the updated requirements. You need at least Java 6, Eclipse 3.6 CDT and cppcheclipse 1.56 with this version. Due to changes from Google Code with regards to downloads (http://google-opensource.blogspot.de/2013/05/a-change-to-google-code-download-service.html) I can no longer provide zip files for manual installation in the dropin folder. Please rather use the Eclipse Update Site from above.
  * 2012/11/21 - Version 0.9.9 fixes some problems with 0.9.8. The P2 update repository in the previous version was not complete ([issue 52](https://code.google.com/p/cppcheclipse/issues/detail?id=52)). Just try the new version. Please observe the updated requirements. You need at least Java 6, Eclipse 3.6 CDT and cppcheclipse 0.52 with this version.
  * 2012/11/18 - Version 0.9.8 support new features in cppcheck ([issue 40](https://code.google.com/p/cppcheclipse/issues/detail?id=40), [issue 41](https://code.google.com/p/cppcheclipse/issues/detail?id=41), [issue 42](https://code.google.com/p/cppcheclipse/issues/detail?id=42)). Bugfixes for the [issue 39](https://code.google.com/p/cppcheclipse/issues/detail?id=39) and [issue 47](https://code.google.com/p/cppcheclipse/issues/detail?id=47) have been applied. Also the build is now done via [Tycho](http://www.eclipse.org/tycho/) and all the 3rd party dependencies are downloaded from [Eclipse Orbit](http://www.eclipse.org/orbit/). There were some automated unit tests added to the project. You need to use at least version 1.52 of cppcheck with this version of cppcheclipse.
  * 2011/04/09 - Version 0.9.7 fixes some incompatibilities with the newest cppcheck versions and adds support for symbols ([issue 33](https://code.google.com/p/cppcheclipse/issues/detail?id=33)), considers Eclipse's proxy settings for the update check ([issue 34](https://code.google.com/p/cppcheclipse/issues/detail?id=34)), and fixes lots of small bugs ([issue 20](https://code.google.com/p/cppcheclipse/issues/detail?id=20), [issue 21](https://code.google.com/p/cppcheclipse/issues/detail?id=21), [issue 27](https://code.google.com/p/cppcheclipse/issues/detail?id=27), [issue 31](https://code.google.com/p/cppcheclipse/issues/detail?id=31), [issue 35](https://code.google.com/p/cppcheclipse/issues/detail?id=35)). You need to use at least version 1.47 of cppcheck with this version of cppcheclipse.
  * 2010/05/16 - Version 0.9.6 comes with a big internal change: Now cppcheck is only started once for all files to be checked ([issue 14](https://code.google.com/p/cppcheclipse/issues/detail?id=14)). Additionally the option --append is supported ([issue 16](https://code.google.com/p/cppcheclipse/issues/detail?id=16)), as well as passing include directories to cppcheck ([issue 17](https://code.google.com/p/cppcheclipse/issues/detail?id=17)). This version is again compatible with cppcheck 1.43 ([issue 15](https://code.google.com/p/cppcheclipse/issues/detail?id=15)). Another bug with cppcheclipse not appearing in the project properties ([issue 13](https://code.google.com/p/cppcheclipse/issues/detail?id=13)) was also fixed.
  * 2010/02/04 - Just one day later I have to release 0.9.5 since the previous one (and also 0.9.3 I guess) suffered  from a serious bug ([issue 10](https://code.google.com/p/cppcheclipse/issues/detail?id=10)), where some of the command line arguments where not correctly transmitted to cppcheck. Furthermore [issue 09](https://code.google.com/p/cppcheclipse/issues/detail?id=09) and [issue 11](https://code.google.com/p/cppcheclipse/issues/detail?id=11) were fixed.
  * 2010/02/03 - Release 0.9.4 available which fixes [issue 7](https://code.google.com/p/cppcheclipse/issues/detail?id=7) and [issue 8](https://code.google.com/p/cppcheclipse/issues/detail?id=8). Additionally it adds support for calling it via a shortcut (Ctrl+Shift+C). This release requires cppcheck in at least version 1.40, as it uses its new --enable-flags. Some other bugfixes regarding marker deletion, problem list handling and incremental builder were also included.
  * 2009/11/22 - New release 0.9.3 available which fixes [issue 4](https://code.google.com/p/cppcheclipse/issues/detail?id=4), [issue 5](https://code.google.com/p/cppcheclipse/issues/detail?id=5) and [issue 6](https://code.google.com/p/cppcheclipse/issues/detail?id=6). The latter could lead to a nasty error, where the error mark was at the wrong positon. Some bugfixes for suppressions were added.
  * 2009/10/31 - New release 0.9.2 is available which is compatible with Java 1.5 and Eclipse 3.4 and 3.5. It adds support for check suppressions, an automatic update check for cppcheck and lots of bugfixes which include [issue 2](https://code.google.com/p/cppcheclipse/issues/detail?id=2) and [issue 3](https://code.google.com/p/cppcheclipse/issues/detail?id=3).
  * 2009/10/22 - Bugfix release 0.9.1 available which fixes [issue 1](https://code.google.com/p/cppcheclipse/issues/detail?id=1). This bug caused Eclipse to hang under some circumstances.
  * 2009/10/18 - First public version 0.9 of cppcheclipse available

## Screenshot ##
![http://cppcheclipse.googlecode.com/svn/images/main.gif](http://cppcheclipse.googlecode.com/svn/images/main.gif)
