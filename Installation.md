# Introduction #

cppcheclipse is an Eclipse plugin, which can be installed over the update mechanism, provided by Eclipse. The plugin is platform independent but still needs a current cppcheck binary. You can get a binary (for Windows) or the source code for all other platforms at http://sourceforge.net/projects/cppcheck/.

# Requirements #
  * Java 1.6 or newer
  * Eclipse 3.6 or newer with CDT
  * cppcheck (1.52 or newer)

# Installation #

## Eclipse Marketplace ##
The easiest way to install is via Eclipse Marketplace which is integrated into Eclipse since 3.6 (Helios).
  1. Click on Help->Eclipse Marketplace...
  1. Enter "cppcheclipse" in the Find field and click on Go
  1. Click on the Install button beneath the entry for cppcheclipse.

## Manually add Eclipse Update Site ##
  1. Click on Help->Install New Software...
  1. Add the new Update Site http://cppcheclipse.eclipselabs.org.codespot.com/svn/update/
  1. Uncheck "Group items by category"
  1. Select cppcheclipse and click on "Next"
  1. Follow the installation wizard, until installation is complete
  1. Restart Eclipse

## Dropin Installation ##
Instead of using the recommended update site installation you can use the provided zip file of the latest cppcheclipse, which is available at Downloads, for using it with the dropins folder of Eclipse. To install it, extract the zip file in a new subfolder within the dropins folder. The name of this subfolder can be arbitrary (e.g. cppcheclipse).
The advantage of this way is, that deinstalling this plugin is as easy as removing the folder from the dropins folder, but there is the disadvantage, that eclipse cannot automatically update those plugins. For more information about this installation method look at http://wiki.eclipse.org/Equinox_p2_Getting_Started#Dropins and http://www.ibm.com/developerworks/opensource/library/os-eclipse-equinox-p2/index.html?ca=dgr-eclpsw02os-eclipse-equinox-p2&S_TACT=105AGX59&S_CMP=GRsiteeclpsw02#N1007A.

# Configuration #
  1. Click on [Preferences->C/C++->cppcheclipse](WorkspacePreferences#General.md) to setup the path to the cppcheck binary on your computer

For more information on how to use this plugin have a look at [Usage](Usage.md).

# Update #
Cppcheclipse checks automatically for updates of cppcheck. This behaviour can be configured via the [Eclipse Preferences->C/C++->cppcheclipse](WorkspacePreferences#General.md). To update cppcheclipse itself, go to Help->Check for Updates.