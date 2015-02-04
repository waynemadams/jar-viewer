# jar-viewer
A simple utility for searching Java .jar files, or nested directories of .jar files, for class references.  Nothing particularly sophisticated, but I've been using it for 10 years and sometimes it comes in really handy.

This is an SBT project.  You can run it with

    sbt run

choosing the JarView main, or from a shell at the top-level of the project with

    java -cp target/scala-2.11/classes/ com.adamsresearch.jarview.JarView

The main usefulness of this utility is searching through a directory structure (of .jar files) for a specific Java class.

