import sbtdocker.mutable.Dockerfile
import sbtdocker.DockerPlugin.autoImport._
import com.typesafe.sbt.SbtNativePackager.autoImport.executableScriptName

val ScalaVersion = "2.11.9"
val Http4sVersion = "0.18.19"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"
val SlickVersion = "3.2.3"
val MySqlConnVersion = "8.0.11"
val JodaVersion = "2.10"
val SlickJodaMapperVersion = "2.3.0"
val CirceVersion = "0.10.0"
val SparkVersion = "2.2.0"
val CucumberVersion = "2.0.1"

val kafkaDependencies = Seq("org.apache.kafka" % "kafka_2.11" % "0.10.1.0")

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

lazy val root = (project in file("."))
  .settings(
    organization := "com.tookitaki",
    name := "btc-price-api",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := ScalaVersion,
    sbtVersion := "1.0.0",
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
      "org.scalatest" %% "scalatest" % "3.2.0-SNAP7" % Test,
      "org.mockito" %% "mockito-scala" % "1.0.0" % Test
    ) ++ kafkaDependencies,
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
  .aggregate(kafkaServices)
  .dependsOn(kafkaServices)

lazy val sparkRatesDownloader = (project in file("spark-rates-downloader"))
  .settings(
    organization := "com.tookitaki",
    name := "btc-price-api",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := ScalaVersion,
    sbtVersion := "1.0.0",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core",
      "org.apache.spark" %% "spark-sql",
      "org.apache.spark" %% "spark-streaming"
    ).map(_ % SparkVersion % "provided") ++ Seq(
      "io.circe" %% "circe-core" % CirceVersion,
      "io.circe" %% "circe-parser" % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "joda-time" % "joda-time" % "2.10.1"
    ) ++ kafkaDependencies
  )
  .disablePlugins(sbtdocker.DockerPlugin, UniversalPlugin, JavaAppPackaging)
  .settings(
    assemblyMergeStrategy in assembly := {
      case PathList("org", "aopalliance", xs@_*) => MergeStrategy.last
      case PathList("javax", "inject", xs@_*) => MergeStrategy.last
      case PathList("javax", "servlet", xs@_*) => MergeStrategy.last
      case PathList("javax", "activation", xs@_*) => MergeStrategy.last
      case PathList("org", "apache", xs@_*) => MergeStrategy.last
      case PathList("io", "circe", xs@_*) => MergeStrategy.last
      case PathList("com", "google", xs@_*) => MergeStrategy.last
      case PathList("com", "esotericsoftware", xs@_*) => MergeStrategy.last
      case PathList("com", "codahale", xs@_*) => MergeStrategy.last
      case PathList("com", "yammer", xs@_*) => MergeStrategy.last
      case "about.html" => MergeStrategy.rename
      case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
      case "META-INF/mailcap" => MergeStrategy.last
      case "META-INF/mimetypes.default" => MergeStrategy.last
      case "plugin.properties" => MergeStrategy.last
      case "log4j.properties" => MergeStrategy.last
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

lazy val kafkaServices = (project in file("kafka-services"))
  .disablePlugins(sbtdocker.DockerPlugin, UniversalPlugin, JavaAppPackaging)
  .settings(
    organization := "com.tookitaki",
    name := "kafka-services",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := ScalaVersion,
    sbtVersion := "1.0.0",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.2"
    ) ++ kafkaDependencies
  )


lazy val sparkRatesDownloaderItSpec = (project in file("spark-rates-downloader-specs"))
  .disablePlugins(sbtdocker.DockerPlugin, UniversalPlugin, JavaAppPackaging)
  .settings(
    organization := "com.tookitaki",
    scalaVersion := ScalaVersion,
    name := "btc-price-api-itspec",
    libraryDependencies := Seq(
      "io.cucumber" % "cucumber-core" % CucumberVersion % "test",
      "io.cucumber" % "cucumber-junit" % CucumberVersion % "test",
      "io.cucumber" %% "cucumber-scala" % CucumberVersion % "test",
      "org.scalatest" %% "scalatest" % "3.2.0-SNAP7" % Test,
      "org.apache.kafka" % "kafka_2.11" % "0.10.1.0" % "test",
      "com.typesafe" % "config" % "1.3.2"
    )
  )
  .enablePlugins(CucumberPlugin)
  .settings(
    fork in Test := true,
    CucumberPlugin.monochrome := false,
    CucumberPlugin.glue := "com/tookitaki/rates/itapp",
    CucumberPlugin.features := List("spark-rates-downloader-specs/src/test/resources/features/"),
    CucumberPlugin.beforeAll := { () => println("** Regression Test **") },
    CucumberPlugin.afterAll := { () => println("** Test Completed **") }
  )
