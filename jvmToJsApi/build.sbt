
name := "jvmToJsApi"


version := "0.1"

val circeVersion = "0.14.5"

ThisBuild / scalaVersion := "3.3.0"

scalacOptions ++= Seq(
  "-encoding", "utf8", // Option and arguments on same  line
  "-Xfatal-warnings", // New lines for each options
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

lazy val legacyExtensionBridgeJVM = ProjectRef(file("../legacy_extensions_bridge"), "fooJVM")
lazy val legacyExtensionBridgeJS = ProjectRef(file("../legacy_extensions_bridge"), "fooJS")


lazy val foo = crossProject(JSPlatform, JVMPlatform).in(file("."))
  .settings(
    name := "online-test-suite-extensions-bridge",
    version := "0.2-SNAPSHOT",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
//      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )
  .jvmConfigure(_.dependsOn(legacyExtensionBridgeJVM))
  .jsConfigure(_.dependsOn(legacyExtensionBridgeJS))






