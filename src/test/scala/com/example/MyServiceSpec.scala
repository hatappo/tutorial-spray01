package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

class MyServiceSpec extends Specification with Specs2RouteTest with MyService {
  def actorRefFactory = system
  
  "MyService" should {

    "ルートパスへのGETリクエストに挨拶を返します。" in {
      Get() ~> myRoute ~> check {
        responseAs[String] must contain("Say hello")
      }
    }

    "その他の、制御されないパスへのGETリクエストは失敗します。" in {
      Get("/kermit") ~> myRoute ~> check {
        handled must beFalse
      }
    }

    "ルートパスへのPUTリクエストに MethodNotAllowed エラーを返します。" in {
      Put() ~> sealRoute(myRoute) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}
