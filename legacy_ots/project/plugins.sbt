addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.0.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "3.4.0")

libraryDependencies ++= Seq(
  "com.h2database"  %  "h2"                  % "1.4.200",
)