libraryDependencies ++= Seq(
  "com.google.crypto.tink" % "apps-webpush" % "1.2.0",
  "com.beust" % "jcommander" % "1.72",
  "com.google.code.gson" % "gson" % "2.8.0",
  "org.apache.httpcomponents" % "httpasyncclient" % "4.1.3",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.54",
  "org.bitbucket.b_c" % "jose4j" % "0.5.2"
)

fork in Test := true
fork in run := true

scalaVersion := "2.12.7"

scalacOptions ++= (
  "-deprecation" ::
  "-unchecked" ::
  "-Xlint" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  "-Yno-adapted-args" ::
  Nil
)

val unusedWarnings = "-Ywarn-unused:imports,locals" :: Nil

scalacOptions ++= unusedWarnings

Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) --= unusedWarnings
)
