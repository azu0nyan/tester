ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")

ThisBuild / cancelable := true

ThisBuild / connectInput := true

//val scalaVer = "3.3.0"
val scalaVer = "2.13.10"

val quillDependencies = Seq(
//  "org.postgresql" % "postgresql" % "9.4.1208",
  "io.getquill" %% "quill-jdbc" % "4.6.1",
//  "io.getquill" %% "quill-sql" % "4.6.1",
//  "io.getquill" %% "quill-jdbc-zio" % "4.6.1",
//  "io.getquill" %% "quill-jdbc-zio" % "4.6.0.1",
  "org.postgresql" % "postgresql" % "42.3.1"
)

lazy val legacyOts = ProjectRef(file("../../online-test-suite"), "fooJVM")
val utils = RootProject(file("../../scalaUtils"))

val dbFromMongoMigration = (project in file("."))
  .settings(
    version := "3.2.2",
    scalaVersion := scalaVer,
    name := "dbFromMongoMigration",
    libraryDependencies ++= quillDependencies,
    scalacOptions += "-Ytasty-reader",
//    scalacOptions += "-Xignore-scala2-macros"
//    excludeDependencies += "com.lihaoyi" % "sourcecode_3",
//    excludeDependencies += "com.typesafe.scala-logging" % "scala-logging_3",
  ).dependsOn(legacyOts, utils)

