package controllers

import javax.inject.Inject

import models.Config
import play.api.Configuration
import play.api.mvc._

class Application @Inject()(@Config(value = "icm.test.0") test0: Int,
                            @Config(value = "icm.test.0") test01: java.lang.Integer,

                            @Config(value = "icm.test.1") test1: String,
                            @Config(value = "icm.test.1") test12: java.lang.String,

                            @Config(value = "icm.test.2") test: Configuration,
                            @Config(value = "icm.test.2.inner") inner: Configuration,

                            @Config(value = "icm.test.2.inner.lol") lol: String,

                            @Config(value = "icm.test.3") d: Double,
                            @Config(value = "icm.test.3") d2: java.lang.Double,

                            @Config(value = "icm.test.4") b: Boolean,
                            @Config(value = "icm.test.4") b2: java.lang.Boolean,

                            @Config(value = "icm.test2.test3.test4.test5") hiho: String) extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready." + test12))
  }

}
