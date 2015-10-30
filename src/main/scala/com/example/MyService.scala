package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

// いちいちアクターを起動させないとテストできないのは嫌なので、独立してテスト可能にするため、
// Webサービス用アクターに直接ルーティングを実装しないで（親トレイトの MyService に）分離しています。
class MyServiceActor extends Actor with MyService {

  // HttpService トレイトは、この抽象メンバ1つを定義しているだけです。
  // そして、これがアクター（あるいはテスト）に内包されたサービスの環境（コンテキスト）に接続します。
  def actorRefFactory = context

  // このアクターは私達が作ったルーティング処理のみを行いますが、
  // その他の雑多な処理を追加しても問題ありません。
  // 例えば、リクエストストリームの加工や接続のタイムアウト制御などを追加したりなどです。
  def receive = runRoute(myRoute)
}


// この TrackingService トレイトは、Webサービス用アクターから独立して、私達のサービスの振る舞いを定義します。
trait MyService extends HttpService {

  val myRoute =
    path("") {
      get {
        // デフォルトだと、XMLは text/xml にマーシャリングされてしまうので、単純にここで上書きしています。
        respondWithMediaType(`text/html`) { 
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    }
}