scalaVersion := "3.3.1"

val Http4sVersion     = "1.0.0-M40"
val CatsEffectVersion = "3.5.3"
val DoobieVersion     = "1.0.0-RC4"
val CirceVersion      = "0.14.6"
val MunitVersion = "1.0.0-M11"
val Fs2Version = "3.12.2"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

lazy val root = project
  .in(file("."))
  .settings(
    name := "PriceStream",
    idePackagePrefix := Some("org.aranadedoros.pricestream"),
    resolvers += Resolver.mavenCentral,
    libraryDependencies ++= Seq(

      // ========================
      // Http4s
      // ========================
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-dsl"          % Http4sVersion,
      "org.http4s" %% "http4s-circe"        % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,

      // ========================
      // Cats Effect
      // ========================
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,

      // ========================
      // Doobie (Postgres)
      // ========================
      "org.tpolecat" %% "doobie-core"      % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari"    % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres" %  DoobieVersion, // Match your version

      // PostgreSQL JDBC driver
      "org.postgresql" % "postgresql" % "42.7.3",

      // ========================
      // Circe
      // ========================
      "io.circe" %% "circe-core"    % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser"  % CirceVersion,

      // ========================
      // Logging
      // ========================
      "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
      "ch.qos.logback" % "logback-classic" % "1.4.11",

      // ========================
      // Testing
      // ========================
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test,

      // For config
      "com.typesafe" % "config" % "1.4.5",

      // For jobs
      "co.fs2" %% "fs2-core" % Fs2Version,

    ),
    testFrameworks += new TestFramework("munit.Framework")
  )

// Recommended settings for Scala 3
scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Wunused:all", // Warn if an unused or unreferenced definition is not private.
  "-Wvalue-discard", // Warn when non-Unit expression results are unused.
  "-Ykind-projector", // Enable kind-projector syntax
)
