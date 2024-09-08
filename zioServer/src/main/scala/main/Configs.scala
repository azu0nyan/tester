package main

import pureconfig.ConfigSource

object Configs {
  val resourceConfig = ConfigSource.resources("application.conf")
  val fileConfig = ConfigSource.file("../workdir/application.conf")
  val config = fileConfig.optional.withFallback(resourceConfig)
}
