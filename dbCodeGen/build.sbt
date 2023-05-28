
lazy val root = (project in file("."))
  .settings(
    scalaVersion := "2.13.10",
    name := "dbModel",
    libraryDependencies += "io.getquill" %% "quill-codegen-jdbc" % "4.6.0",
    libraryDependencies += "org.postgresql" % "postgresql" % "42.3.1"
  )
