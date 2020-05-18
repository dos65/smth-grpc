val commonSettings = Seq(
  scalaVersion := "2.12.11",
)

lazy val core = project.in(file("modules/core"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.1.1",
      "org.typelevel" %% "alleycats-core" % "2.1.1",
      "io.grpc" % "grpc-protobuf" % "1.29.0"
    )
  )

lazy val catsEffect = project.in(file("modules/cats-effect"))
  .dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "2.1.3",
    )
  )
  

lazy val example = project.in(file("modules/example"))
  .dependsOn(catsEffect)
  .settings(commonSettings: _*)
  .settings(
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      "io.grpc" % "grpc-netty-shaded" % "1.29.0"
    )
  )