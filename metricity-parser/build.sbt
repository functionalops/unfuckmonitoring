name          := "metricity.parser"

libraryDependencies ++=
  Seq(
    "org.scalaz"    %%  "scalaz-core"                 % metricity.Versions.scalaz,
    "org.scalaz"    %%  "scalaz-effect"               % metricity.Versions.scalaz,
    //"org.scalaz"    %%  "scalaz-concurrent"           % metricity.Versions.scalaz,
    //"org.scalaz"    %%  "scalaz-stream"               % metricity.Versions.scalaz,
    "org.parboiled" %% "parboiled-scala"              % metricity.Versions.parboiled,
    "org.scalatest" %% "scalatest"                    % metricity.Versions.scalatest % "test",
    "org.scalaz"    %%  "scalaz-scalacheck-binding"   % metricity.Versions.scalaz  % "test"
  )
