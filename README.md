cppcheclipse is an Eclipse plugin which integrates [cppcheck](http://sourceforge.net/projects/cppcheck/) with the [CDT project](https://eclipse.org/cdt/). You can run/configure cppcheck from the Eclipse UI.

To build the project by hand it requires maven and Linux platform (maven under Windows will not compile the project properly). To compile run following commands:
```bash
cd com.googlecode.cppcheclipse.parent
mvn clean verify
ls ../com.googlecode.cppcheclipse.repository/target/*.zip -l
```
It will not increment the version number, will not create a relese nor deploy/publish the artifact.

Further information on how to use and install cppcheclipse can be found in the [wiki](https://github.com/kwin/cppcheclipse/wiki).