libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "19.0",
  "org.apache.httpcomponents" % "fluent-hc" % "4.5.2",
  "commons-io" % "commons-io" % "2.5",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.54",
  "org.json" % "json" % "20160212" % "test",
  "org.bitbucket.b_c" % "jose4j" % "0.5.2",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

scalaVersion := "2.11.8"

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

val unusedWarnings = (
  "-Ywarn-unused" ::
  "-Ywarn-unused-import" ::
  Nil
)

scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
  case Some((2, v)) if v >= 11 => unusedWarnings
}.toList.flatten

Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) ~= {_.filterNot(unusedWarnings.toSet)}
)