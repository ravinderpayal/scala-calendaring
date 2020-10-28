name := "calendar"

version := "0.1"

scalaVersion := "2.13.3"

// https://mvnrepository.com/artifact/org.apache.james/apache-mime4j
libraryDependencies += "org.apache.james" % "apache-mime4j" % "0.8.3" pomOnly()

// https://mvnrepository.com/artifact/org.mnode.ical4j/ical4j
libraryDependencies += "org.mnode.ical4j" % "ical4j" % "4.0.0-alpha6"

// https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
// libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.30" % Test
// https://mvnrepository.com/artifact/org.slf4j/slf4j-nop
// https://mvnrepository.com/artifact/org.slf4j/slf4j-nop
libraryDependencies += "org.slf4j" % "slf4j-nop" % "2.0.0-alpha1" % Test
// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.0-alpha1"

// https://mvnrepository.com/artifact/org.ehcache/jcache
libraryDependencies += "org.ehcache" % "jcache" % "1.0.1"


// https://mvnrepository.com/artifact/com.sendgrid/sendgrid-java
libraryDependencies += "com.sendgrid" % "sendgrid-java" % "4.2.1"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.1"