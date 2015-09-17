import java.io.File

import com.datasift.ptolemy.{PtolemyJson, Ptolemy}

object IdmlTool {
  val ptolemy = new Ptolemy()

  val parser = new scopt.OptionParser[IdmlToolConfig]("idml") {
    head("Ptolemy IDML command line tool.")
    help("help") text ("Show usage information and flags")
    opt[Boolean]("pretty") action { (x, c) =>
      c.copy(pretty = x)
    } text("Enable pretty printing of output")
    arg[File]("<file>...") unbounded() required() action { (x, c) =>
      c.copy(files = c.files :+ x)
    } text ("one or more mapping files to run the data through")
  }

  def main(args: Array[String]): Unit = {
    parser.parse(args, IdmlToolConfig()) match {
      case Some(config) =>
        runChain(config)
      case None =>
        sys.exit(1)
    }
  }

  def runChain(config: IdmlToolConfig): Unit = {
    val (found, missing) = config.files.partition(_.exists())
    missing.isEmpty match {
      case false =>
        missing.foreach{f => println("Couldn't load mapping from %s".format(f))}
        sys.exit(1)
      case true =>
        val chain = ptolemy.newChain(found.map(f => ptolemy.fromFile(f.getAbsolutePath)) :_*)
        io.Source.stdin.getLines().map { s: String =>
          chain.run(PtolemyJson.parse(s))
        }.foreach { r =>
          config.pretty match {
            case true  => println(PtolemyJson.pretty(r))
            case false => println(PtolemyJson.compact(r))
          }
          Console.flush()
        }
    }
  }
}
