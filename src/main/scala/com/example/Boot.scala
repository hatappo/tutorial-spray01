package com.example

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object Boot extends App {

  // まず ActorSystem インスタンスが必要です。アプリケーションは、この ActorSystem 上にホスティングされます。
  implicit val system = ActorSystem("on-spray-can")

  // Webサービス用アクターを生成し起動します。
  val service = system.actorOf(Props[MyServiceActor], "demo-service")

  implicit val timeout = Timeout(5.seconds)
  // 先述のWebサービス用アクターをハンドラとして、HTTPサーバを8080で立ち上げます。
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}
