import org.scalajs.linker.interface.ModuleSplitStyle


val slinkyVersion = "0.7.4"


lazy val contentProject = ProjectRef(file("../jvmToJsApi"), "fooJS")

val circeVersion = "0.14.5"
lazy val circe: Project => Project = _.settings(
  libraryDependencies ++= Seq(
    "io.circe" %%% "circe-core",
    "io.circe" %%% "circe-generic",
    "io.circe" %%% "circe-parser"
  ).map(_ % circeVersion)
)
lazy val basicLibs: Project => Project = _.settings(
  libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0",
  libraryDependencies += "me.shadaj" %%% "slinky-core" % slinkyVersion, // core React functionality, no React DOM
  libraryDependencies += "me.shadaj" %%% "slinky-web" % slinkyVersion, // React DOM, HTML and SVG tags
//  libraryDependencies += "me.shadaj" %%% "slinky-native" % slinkyVersion, // React Native components
  libraryDependencies += "me.shadaj" %%% "slinky-hot" % slinkyVersion, // Hot loading, requires react-proxy package
  //libraryDependencies += "me.shadaj" %%% "slinky-scalajsreact-interop" % slinkyVersion // Interop with japgolly/scalajs-react
  libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.5.0"
)


lazy val baseSettings: Project => Project =
  _.enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin, ScalablyTypedConverterPlugin)
    .settings(
      version := "0.2",
      name := "testerUI",
      scalaVersion := "3.2.2",
    //scalacOptions ++= ScalacOptions.flags,
      scalaJSUseMainModuleInitializer := true,
      /* disabled because it somehow triggers many warnings */
//      scalaJSLinkerConfig := scalaJSLinkerConfig.value.withSourceMap(false),
      /* for slinky */
      scalacOptions += "-Ytasty-reader",
      scalacOptions += "-Ymacro-annotations",
      scalacOptions += "-Ylog-classpath",
      stFlavour := Flavour.Slinky,
      useYarn := true,

      webpackDevServerPort := 3228,
//      webpack / version := "3.1.4",
//      startWebpackDevServer / version := "3.1.4",

    ).dependsOn(contentProject)

lazy val bundlerSettings: Project => Project =
  _.settings(
    webpackCliVersion := "4.10.0",
    Compile / fastOptJS / webpackDevServerExtraArgs += "--mode=development",
    Compile / fullOptJS / webpackDevServerExtraArgs += "--mode=production"
  )

// specify versions for all of reacts dependencies to compile less since we have many demos here
lazy val npmDeps: Project => Project =
  _.settings(
    stTypescriptVersion := "3.9.3",
    Compile / npmDependencies ++= Seq(
      "react" -> "18.2.0",
      "react-dom" -> "18.2.0",
      "@types/react" -> "18.2.0",
      "@types/react-dom" -> "18.2.0",
      "csstype" -> "2.6.11",
      "@types/prop-types" -> "15.7.8",
      //
      "antd" -> "4.9.4",
      "react-ace" -> "10.1.0",
      "ace-builds" -> "1.18.0",
      "better-react-mathjax" -> "2.0.2"
    )
  )

lazy val withLoaders: Project => Project =
  _.settings(
    /* custom webpack file to include css */
    webpackConfigFile := Some((ThisBuild / baseDirectory).value / "custom.webpack.config.js"),
    Compile / npmDevDependencies ++= Seq(
      "webpack-merge" -> "5.8.0",
      "css-loader" -> "6.7.2",
      "style-loader" -> "3.3.1",
      "file-loader" -> "6.2.0",
      "url-loader" -> "4.1.1"
    )
  )



// Define task to  copy html files
lazy val copyJS = taskKey[Unit]("Copy js")

// Implement task
copyJS := {
  java.nio.file.Files.copy(
    new File("/home/azu/projects/testerUI/target/scala-2.13/scalajs-bundler/main/testerui-fastopt-bundle.js").toPath,
    new File("/home/azu/projects/testerUI/data/testerui-fastopt-bundle.js").toPath,
    java.nio.file.StandardCopyOption.REPLACE_EXISTING
  )

}


/**
 * Custom task to start demo with webpack-dev-server, use as `<project>/start`.
 * Just `start` also works, and starts all frontend demos
 *
 * After that, the incantation is this to watch and compile on change:
 * `~<project>/fastOptJS::webpack`
 */
lazy val start = TaskKey[Unit]("start")

/** Say just `dist` or `<project>/dist` to make a production bundle in
 * `docs` for github publishing
 */
lazy val dist = TaskKey[File]("dist")

/**
 * Implement the `start` and `dist` tasks defined above.
 * Most of this is really just to copy the index.html file around.
 */
lazy val browserProject: Project => Project =
  _.settings(
    start := {
      (Compile / fastOptJS / startWebpackDevServer).value
    },
    dist := {
      import java.nio.file.Files
      import java.nio.file.StandardCopyOption.REPLACE_EXISTING
      val artifacts = (Compile / fullOptJS / webpack).value
      val artifactFolder = (Compile / fullOptJS / crossTarget).value
      val distFolder = (ThisBuild / baseDirectory).value / "docs" / moduleName.value

      distFolder.mkdirs()
      artifacts.foreach { artifact =>
        val target = artifact.data.relativeTo(artifactFolder) match {
          case None          => distFolder / artifact.data.name
          case Some(relFile) => distFolder / relFile.toString
        }

        Files.copy(artifact.data.toPath, target.toPath, REPLACE_EXISTING)
      }

      val indexFrom = baseDirectory.value / "data/index.html"
      val indexTo = distFolder / "index.html"

      val indexPatchedContent = {
        import collection.JavaConverters._
        Files
          .readAllLines(indexFrom.toPath, IO.utf8)
          .asScala
          .map(_.replaceAllLiterally("-fastopt-", "-opt-"))
          .mkString("\n")
      }

      Files.write(indexTo.toPath, indexPatchedContent.getBytes(IO.utf8))
      distFolder
    }
  )

lazy val hotReloadingSettings: Project => Project =
  _.enablePlugins(ScalaJSPlugin)
    .settings(
      fastOptJS / webpackDevServerExtraArgs := Seq("--inline", "--hot"),
      stIgnore += "react-proxy",
      Compile / npmDependencies ++= Seq(
        "react-proxy" -> "1.1.8"
      )
    )



lazy val root = (project in file(".")).configure(baseSettings, basicLibs,circe, npmDeps, withLoaders, browserProject, bundlerSettings, hotReloadingSettings)