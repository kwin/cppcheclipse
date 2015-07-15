# Introduction #
After the [installation](Installation.md) you can use these steps to get the first results.

# Get Started #
  1. Open Eclipse-Preferences.
  1. Go to C/C++->cppcheclipse.
  1. Enter [binary path](WorkspacePreferences#General.md) for cppcheck.
  1. Go to [settings](WorkspacePreferences#Settings.md) and select the checks you want to execute (default is to check only for problems).
  1. Go to [problems](WorkspacePreferences#Problems.md) and deselect the issues you want to suppress
  1. Click on OK.
  1. In the Project Explorer select the project you want to check and right click on it.
  1. Select "Run cppcheck" from the contextmenu.
  1. The issues are reported in the problems view (in addition to the output of cppcheck in the console view).
  1. For each issue, there are some quickfixes available.

To configure cppcheck project-specific overwrite some of the settings in the [project properties](ProjectProperties.md). The default for new projects is, that they take over the settings from the Preferences.