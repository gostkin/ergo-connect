package org.ergoplatform.connect

import java.util.concurrent.Executors

import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl.{asyncHttpClient, config}
import org.ergoplatform.connect.NodeApiClient.Address
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.concurrent._
import scala.io.Source
import scala.util.{Failure, Success}

class ConnectApp(override val client: AsyncHttpClient) extends NodeApiClient {
  override def restAddress: String = "127.0.0.1"

  override def nodeRestPort: Int = 9052

  override def blockDelay: FiniteDuration = new FiniteDuration(20, MILLISECONDS)

  override implicit def ec: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  override def apiKey: String = "e88524573de35fc8e108814c29bc2bc2dd5f6b3ec9f09c7deed7b47337603d0f" // replace with own value

}

object ConnectApp extends StrictLogging {
  @inline protected def log = logger

  def main(args: Array[String]): Unit = new ConnectApp(asyncHttpClient(config()
    .setMaxConnections(50)
    .setMaxConnectionsPerHost(10)
    .setMaxRequestRetry(1)
    .setReadTimeout(10000)
    .setRequestTimeout(10000))) {
    if (args.length < 1) {
      throw new RuntimeException("Sigma contract name is not provided")
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

        val transferValue = 2000000
        val fee           = 900000
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

