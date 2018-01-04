cppcheclipse is an Eclipse plugin which integrates [cppcheck](http://sourceforge.net/projects/cppcheck/) with the [CDT project](https://eclipse.org/cdt/). You can run/configure cppcheck from the Eclipse UI.

To build the project on the command line it requires maven and Linux platform (maven under Windows will not compile the project properly). To compile run following commands:
```bash
cd com.googlecode.cppcheclipse.parent
mvn clean verify
```
It will not increment the version number nor deploy/publish/release the artifact. You can find the built p2 repository now in ../com.googlecode.cppcheclipse.repository/target in zip format.


Further information on how to use and install cppcheclipse can be found in the [wiki](https://github.com/kwin/cppcheclipse/wiki).