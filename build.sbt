import sbtdocker.mutable.Dockerfile
import sbtdocker.DockerPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.autoImport.executableScriptName

val Http4sVersion = "0.18.19"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"
val SlickVersion = "3.2.3"
val MySqlConnVersion = "8.0.11"
val JodaVersion = "2.10"
val SlickJodaMapperVersion = "2.3.0"
val CirceVersion = "0.10.0"

lazy val root = (project in file("."))
  .settings(
    organization := "com.tookitaki",
    name := "btc-price-api",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.6",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "joda-time" % "joda-time" % JodaVersion,
      "com.github.tototoshi" %% "slick-joda-mapper" % SlickJodaMapperVersion,
      "org.specs2" %% "specs2-core" % Specs2Version % "test",
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.typesafe.slick" %% "slick" % SlickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion,
      "mysql" % "mysql-connector-java" % MySqlConnVersion,
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")
  )
  .enablePlugins(sbtdocker.DockerPlugin, UniversalPlugin, JavaAppPackaging)
  .settings(
    dockerfile in docker := {
      val targetDir = "/app"
      val appDir: File = stage.value
      new Dockerfile {
        from("frolvlad/alpine-oraclejre8")
        runRaw("apk add --no-cache bash")
        copy(appDir, targetDir)
        entryPointRaw(s"sleep 10s; $targetDir/bin/${executableScriptName.value} -jvm-debug 5005")
      }
    },
    imageNames in docker := Seq(
      ImageName(s"${organization.value}/${name.value}:latest"),
    ),
    buildOptions in docker := BuildOptions(
      cache = true,
      removeIntermediateContainers = BuildOptions.Remove.Always,
      pullBaseImage = BuildOptions.Pull.IfMissing
    )
  )

lazy val sparkRatesDownloader = (project in file("spark-rates-downloader"))
  .settings()

lazy val sparkRatesProcessor = (project in file("spark-rates-processor"))
  .settings()
