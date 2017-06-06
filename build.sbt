enablePlugins(ScalaJSPlugin)

name := "cherub/dictionary"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "be.doeraene" %%% "scalajs-jquery" % "0.9.1"
)

scalaJSUseMainModuleInitializer := true