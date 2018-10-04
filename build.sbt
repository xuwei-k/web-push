libraryDependencies ++= Seq(
  "com.google.crypto.tink" % "apps-webpush" % "1.2.0",
  "com.auth0" % "java-jwt" % "3.4.0",

  "com.google.code.gson" % "gson" % "2.8.0",
  "org.apache.httpcomponents" % "httpasyncclient" % "4.1.3",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.54",
  "org.bitbucket.b_c" % "jose4j" % "0.5.2",
  "org.apache.httpcomponents" % "fluent-hc" % "4.5.2" % "test",
  "org.junit.jupiter" % "junit-jupiter-api" % "5.0.0-M4" % "test",
  "org.junit.jupiter" % "junit-jupiter-engine" % "5.0.0-M4" % "test",
  "commons-io" % "commons-io" % "2.5" % "test",
  "org.bouncycastle" % "bcpkix-jdk15on" % "1.55" % "test",
  "com.google.guava" % "guava" % "19.0" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

scalaVersion := "2.12.7"
