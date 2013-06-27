import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "ntlm-module-samplej"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    "ntlm-module" % "ntlm-module_2.10" % "1.0-SNAPSHOT"
    //javaJdbc,
    //javaEbean
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    resolvers += "Local Play Repository" at "file://c:/dev/play-2.1.1/repository/local"
  )

}
