name := "otsExtensionsBridge"

version := "0.1"

scalaVersion := "2.13.3"

scalacOptions ++= Seq(
  "-encoding", "utf8", // Option and arguments on same  line
  "-Xfatal-warnings",  // New lines for each options
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)

//val circeVersion = "0.13.0"
//
//libraryDependencies ++= Seq(
//  "io.circe" %%% "circe-core",
//  "io.circe" %%% "circe-generic",
//  "io.circe" %%% "circe-parser"
//).map(_ % circeVersion)
