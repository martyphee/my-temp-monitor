

object Options {

  def scalacOptions(scalaVersion: String, optimize: Boolean) = {
    val baseOptions = Seq(
      "-feature",
      "-deprecation",
      "-explaintypes",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-language:higherKinds",
      "-language:existentials",
      "-Xfatal-warnings",
      "-Xlint:-byname-implicit,_",
      "-Ywarn-value-discard",
      "-Ywarn-numeric-widen",
      "-Ywarn-extra-implicit",
      "-Ywarn-unused",
      "-Ywarn-value-discard"
    )

    val optimizeOptions =
      if (optimize) {
        Seq(
          "-opt:l:inline"
        )
      } else Seq.empty

    baseOptions ++ optimizeOptions
  }
}
