== Included JARs ==
Apache Commons IO 2.0.1 (bug in TeeOutputStream, http://stackoverflow.com/questions/9290774/chunkwise-copy-data-from-inputstream-to-outputstream-getting-byte-at-the-end, no Guava solution)
Apache Commons Collections 3.2.1 (should use Guava as collections is dead)
Apache Commons Lang 2.5 (only StringUtils used, could use Guava Strings.isEmptyOrNull)
Apache Commons Exec 1.1,  (available in Orbit, http://download.eclipse.org/tools/orbit/downloads/drops/I20120928145848/)
Apache Commons Codec 1.4, (for Base64 in SerializeHelper, available in Orbit, not available in Guava)

== TODO ==
- use a P2 repository which does already provide those libraries as OSGi bundles
- remove this library
-> use existing bundles from Eclipse Orbit!

== Links ==
http://wiki.eclipse.org/Eclipse_Plug-in_Development_FAQ#I.27m_using_third_party_jar_files_and_my_plug-in_is_not_working...
http://wiki.eclipse.org/FAQ_How_can_I_share_a_JAR_among_various_plug-ins%3F

You have to recreate the plug-in to add further dependencies, with "File-> New ->  Project...-> Plug-in Development -> Plug-in from existing JAR archives",
otherwise the MANIFEST.MF must be updated manually which is really not that easy.


[1] https://docs.sonatype.org/display/TYCHO/Dependency+on+pom-first+artifacts