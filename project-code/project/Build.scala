import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "ntlm-module"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    //jdbc,
    //anorm
      "com.github.dblock.waffle" % "waffle-jna" % "1.5",
      "javax.servlet" % "servlet-api" % "2.5"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
