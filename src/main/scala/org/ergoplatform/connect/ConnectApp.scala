package org.ergoplatform.connect

import java.util.concurrent.Executors

import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl.{asyncHttpClient, config}
import org.ergoplatform.connect.NodeApiClient.Address
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext
import scala.concurrent._
import scala.io.Source
import scala.util.{Failure, Success}


class ConnectSettings(val restAddress: String = "127.0.0.1", val nodeRestPort: Int = 9052,
                      val apiKey: String = "") {

}

class ConnectApp(override val client: AsyncHttpClient,
                 val settings: ConnectSettings,
                 val execContext: ExecutionContext
                ) extends NodeApiClient {

  override val restAddress: String = settings.restAddress
  override val nodeRestPort: Int = settings.nodeRestPort
  override val apiKey: String = settings.apiKey
  override implicit val ec: ExecutionContext = execContext
}

object ConnectApp extends StrictLogging {
  @inline protected def log = logger

  /**
    *
    * @param args  the first argument should be a valid path to transaction script
    */
  def main(args: Array[String]): Unit = new ConnectApp(asyncHttpClient(config()
    .setMaxConnections(50)
    .setMaxConnectionsPerHost(10)
    .setMaxRequestRetry(1)
    .setReadTimeout(10000)
    // default settings are used, replace with your own
    .setRequestTimeout(10000)), new ConnectSettings("127.0.0.1", 9052, "e88524573de35fc8e108814c29bc2bc2dd5f6b3ec9f09c7deed7b47337603d0f"), ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2)))
  {
    if (args.length < 3) {
      throw new RuntimeException("You should provide sigma contract path, transfer value and fee")
    }
    private val filename: String = args(0)
    private var fileContents: String = ""
    for(line <- Source.fromFile(filename).getLines()) {
      fileContents = fileContents + line + "\n"
    }

    private val contractSourceMap = Map[String, String](
      "source" -> fileContents
    )

    private var addr: String = ""
    private val compileResult: Future[Address] = compileTransaction(scala.util.parsing.json.JSONObject(contractSourceMap).toString())

    compileResult onComplete {
      case Success(item: Address) => {
        addr = item.address
        log.info("Contract address is " + addr)

        val transferValue = args(1).toInt
        val fee           = args(2).toInt // should be at least 100000
        val transactionBody = s"""{"requests":[{"address": "$addr",\n"value": "$transferValue"}],\n"fee": "$fee"}"""

        val submitResult: Future[Address] = submitTransaction(transactionBody)

        submitResult onComplete {
          case Success(item: Address) => {
            addr = item.address
            log.info("Transaction address is " + addr)
          }

          case Failure(t) => throw new RuntimeException("An error has occurred: " + t.getMessage)
        }
      }

      case Failure(t) => throw new RuntimeException("An error has occurred: " + t.getMessage)
    }
  }
}

