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
    } ~
      //
      // ---------- 確認用の雑多なレスポンス処理群。
      //
      (pathPrefix("misc") & (get | put)) {
        pathEndOrSingleSlash {
          complete {
            ""
          }
        } ~ 
          miscRoute
      }


  /**
   * 雑多なルーティングをまとめた。
   */
  lazy val miscRoute =
    //
    // ---------- 文字列 "ok" を返す。
    //
    (pathPrefix("health") & pathEndOrSingleSlash) {
      complete {
        "ok"
      }
    } ~
      //
      // ---------- HTMLで現在日時を返す。
      //
      (pathPrefix("date") & pathEndOrSingleSlash) {
        respondWithMediaType(`text/html`) {
          complete {
            import java.text.{DateFormat, SimpleDateFormat}
            import java.util.Date
            val fmt: DateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS")
            s"<h1> ${fmt.format(new Date())} </h1>"
          }
        }
      } ~
      //
      // ---------- 単純にリクエストパラメータ一覧を列挙。
      //
      (pathPrefix("param") & pathEndOrSingleSlash & parameterMap) { params =>
        def inspectParam(p: (String, String)): String = s"${p._1} = ${p._2}"
        val start = "the Parameters are below.\n" + ("-" * 40) + "\n\t"
        val sep = "\n\t"
        val end = "\n" + "-" * 40
        complete(params.map(inspectParam).mkString(start, sep, end))
      } ~
      //
      // ---------- 単純にヘッダから「Remote Address」を取得。
      //
      (pathPrefix("header") & pathEndOrSingleSlash & headerValueByType[HttpHeaders.`User-Agent`]()) { ua =>
        complete(ua.toString())
      } ~
      //
      // ---------- オープン・リダイレクタ
      //
      (pathPrefix("redirect") & pathEndOrSingleSlash & parameters('url ? "http://localhost:8080/", 'code ?)) { (url, code) =>
        val statusCode = code.getOrElse("302") match {
          case "301" => StatusCodes.MovedPermanently
          case "302" => StatusCodes.Found
          case _     => StatusCodes.Found
        }
        redirect(url, statusCode)
      } ~
      //
      // ---------- 503 Service Unavailable
      //
      (pathPrefix("sorry") & pathEndOrSingleSlash) {
        failWith(new RequestProcessingException(StatusCodes.ServiceUnavailable))
      }

}
