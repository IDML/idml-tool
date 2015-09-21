import java.io.File

import com.datasift.ptolemy.{PtolemyJson, Ptolemy}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object IdmlTool {
  val ptolemy = new Ptolemy()
  val log = LoggerFactory.getLogger("idml-tool")

  val parser = new scopt.OptionParser[IdmlToolConfig]("idml") {
    head("Ptolemy IDML command line tool.")
    help("help") text ("Show usage information and flags")
    opt[Boolean]("pretty") action { (x, c) =>
      c.copy(pretty = x)
    } text ("Enable pretty printing of output")
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
        missing.foreach { f => println("Couldn't load mapping from %s".format(f)) }
        sys.exit(1)
      case true =>
        val chain = ptolemy.newChain(found.map(f => ptolemy.fromFile(f.getAbsolutePath)): _*)
        io.Source.stdin.getLines().filter(!_.isEmpty).map { s: String =>
          Try {
            chain.run(PtolemyJson.parse(s))
          }
        }.foreach {
          case Success(json) =>
            config.pretty match {
              case true => println(PtolemyJson.pretty(json))
              case false => println(PtolemyJson.compact(json))
            }
            Console.flush()
          case Failure(e) =>
            log.error("Unable to process input", e)
        }
    }
  }
}
