package org.ergoplatform.connect

import java.util.concurrent.Executors

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl.{asyncHttpClient, config}
import org.ergoplatform.connect.NodeApiClient.Status
import scorex.core.api.http.ApiRoute
import scorex.core.network.message.MessageSpec
import scorex.core.settings.Settings
import scorex.util.ScorexLogging

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Failure, Success, Try}


class ConnectApp(override val client: AsyncHttpClient) extends NodeApiClient {
  override def restAddress: String = "0.0.0.0"

  override def nodeRestPort: Int = 9052

  override def blockDelay: FiniteDuration = new FiniteDuration(20, MILLISECONDS)

  override implicit def ec: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
}

object ConnectApp extends ScorexLogging {
  def main(args: Array[String]): Unit = new ConnectApp(asyncHttpClient(config()
    .setMaxConnections(50)
    .setMaxConnectionsPerHost(10)
    .setMaxRequestRetry(1)
    .setReadTimeout(10000)
    .setRequestTimeout(10000))) {
    private val resultFuture = status

    resultFuture onComplete {
      case Success(item: Status) => println(item)
      case Failure(t) => println("An error has occurred: " + t.getMessage)
    }
  }

  /*def forceStopApplication(code: Int = 1): Nothing = sys.exit(code)

  def shutdown(system: ActorSystem, actors: Seq[ActorRef]): Unit = {
    log.warn("Terminating Actors")
    actors.foreach { a => a ! PoisonPill }
    log.warn("Terminating ActorSystem")
    val termination = system.terminate()
    Await.result(termination, 60.seconds)
    log.warn("Application has been terminated.")
  }*/
}

