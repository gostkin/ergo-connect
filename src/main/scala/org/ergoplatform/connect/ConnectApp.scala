package org.ergoplatform.connect

import java.util.concurrent.Executors

import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl.{asyncHttpClient, config}
import org.ergoplatform.connect.NodeApiClient.Address
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext
import scala.concurrent._
import scala.io.Source
import scala.util.parsing.json.JSON
import scala.util.{Failure, Success}


class ConnectSettings(val restAddress: String = "127.0.0.1", val nodeRestPort: Int = 9052,
                      val apiKey: String = "") {

}

object ConnectSettings {
  def fromFile(filename: String): ConnectSettings = {
    var fileContents: String = ""
    val dataFlow = Source.fromFile(filename)
    for(line <- dataFlow.getLines()) {
      fileContents = fileContents + line + "\n"
    }
    dataFlow.close()

    val result = JSON.parseFull(fileContents)
    result match {
      case Some(m: Map[_,_]) if m.keySet.forall(_.isInstanceOf[String]) => {
        val map = m.asInstanceOf[Map[String,Any]]
        val restAddressCandidate = map.getOrElse("rest_address", "127.0.0.1").toString
        val apiKeyCandidate = map.getOrElse("api_key", "").toString
        val restPortCandidate = map.getOrElse("rest_port", "").toString.toFloat.toInt
        return new ConnectSettings(restAddressCandidate, restPortCandidate, apiKeyCandidate)
      }
      case _ => {
        throw new RuntimeException("Can't parse settings")
      }
    }
  }
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
    .setRequestTimeout(10000)),
    if (args.isDefinedAt(3)) ConnectSettings.fromFile(args(3)) else new ConnectSettings(),
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2)))
  {
    if (args.length < 4) {
      throw new RuntimeException("You should provide sigma contract path, transfer value and fee")
    }
    private val filename: String = args(0)
    private var fileContents: String = ""
    for(line <- Source.fromFile(filename).getLines()) {
      fileContents = fileContents + line + "\n"
    }

    private var addr: String = ""
    private val compileResult: Future[Address] = compileTransaction(scala.util.parsing.json.JSONObject(Map[String, String](
      "source" -> fileContents
    )).toString())

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

