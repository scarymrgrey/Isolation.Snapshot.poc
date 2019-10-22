name := "SnapshotIsolationSample"

version := "0.1"

scalaVersion := "2.11.1"

resolvers += "Sonatype OSS Snapshots" at
  "https://oss.sonatype.org/content/repositories/releases"

libraryDependencies += "com.storm-enroute" %% "scalameter" % "0.18"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.8"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")

parallelExecution in Test := false

fork := true

outputStrategy := Some(StdoutOutput)

connectInput := true