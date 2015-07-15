# Introduction #
You can check a file with cppcheck in different ways.
  1. Automatically check a file, whenever it is built (must be enabled in the [project properties](ProjectProperties#Build.md)).
  1. Manually check a file via the context menu on either the package/project explorer or within the C editor.
  1. Manually check a file via the shortcut Ctrl+Shift+C (on Mac: Cmd+Shift+C).

All issues detected by cppcheck are reported in the Problems view of Eclipse. Additionally to that you can see the original output of cppcheck in the console view.
If you use cppcheclipse for the first time, please follow the [first steps](GetStarted.md).

![http://cppcheclipse.googlecode.com/svn/images/main.gif](http://cppcheclipse.googlecode.com/svn/images/main.gif)

# Configuration #

You can either configure cppcheclipse for the [whole workspace](WorkspacePreferences.md) or separately for the [individual projects](ProjectProperties.md).