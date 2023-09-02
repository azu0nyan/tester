
name := "otsExtensionsBridge"


version := "0.1"

val circeVersion = "0.14.5"

ThisBuild / scalaVersion := "3.2.2"

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

lazy val root = project.in(file(".")).
  aggregate(foo.js, foo.jvm).
  settings(
    publish := {},
    publishLocal := {},
  )

lazy val foo = crossProject(JSPlatform, JVMPlatform).in(file("."))
  .settings(
    name := "online-test-suite-extensions-bridge",
    version := "0.2-SNAPSHOT",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  ).jvmSettings(
  // Add JVM-specific settings here
//  libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
  ).
  jsSettings(
    // Add JS-specific settings here
    scalaJSUseMainModuleInitializer := true,
  )





//val circeVersion = "0.13.0"
//
//libraryDependencies ++= Seq(
//  "io.circe" %%% "circe-core",
//  "io.circe" %%% "circe-generic",
//  "io.circe" %%% "circe-parser"
//).map(_ % circeVersion)


