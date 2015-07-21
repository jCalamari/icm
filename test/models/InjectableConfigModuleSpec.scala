package models

import com.google.inject.ConfigurationException
import org.specs2.mutable.Specification
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceApplicationLoader}
import play.api.inject.{BindingKey, BuiltinModule, QualifierInstance}
import play.api.{ApplicationLoader, Configuration, Environment}

object InjectableConfigModuleSpec extends Specification {

  "InjectableConfigModule" should {

    "inject numbers" in {
      val injector = playInjector()

      // Int
      injector.instanceOf(binding(classOf[Int], "int")) must equalTo[Int](121535)
      injector.instanceOf(binding(classOf[Long], "int")) must equalTo[Long](121535L)
      injector.instanceOf(binding(classOf[Double], "int")) must equalTo[Double](121535.00)

      // Long
      injector.instanceOf(binding(classOf[Int], "long")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Long], "long")) must equalTo[Long](1768619760268566049L)
      injector.instanceOf(binding(classOf[Double], "long")) must equalTo[Double](1768619760268566049.00)

      // Double
      injector.instanceOf(binding(classOf[Int], "double")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Long], "double")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Double], "double")) must equalTo[Double](2.5D)
    }

    "inject booleans" in {
      val injector = playInjector()
      injector.instanceOf(binding(classOf[Boolean], "false")) must equalTo[Boolean](false)
      injector.instanceOf(binding(classOf[Boolean], "true")) must equalTo[Boolean](true)
    }

    "inject strings" in {
      val injector = playInjector()
      injector.instanceOf(binding(classOf[String], "nonEmptyString")) must equalTo[String]("character sequence")
      injector.instanceOf(binding(classOf[String], "emptyString")) must equalTo[String]("")
    }

    "inject list of ints" in {
      val injector = playInjector()
      injector.instanceOf(binding(classOf[Int], "ints.0")) must equalTo[Int](523969)
      injector.instanceOf(binding(classOf[Long], "ints.0")) must equalTo[Long](523969L)
      injector.instanceOf(binding(classOf[Double], "ints.0")) must equalTo[Double](523969D)

      injector.instanceOf(binding(classOf[Int], "ints.1")) must equalTo[Int](177618)
      injector.instanceOf(binding(classOf[Long], "ints.1")) must equalTo[Long](177618L)
      injector.instanceOf(binding(classOf[Double], "ints.1")) must equalTo[Double](177618D)

      injector.instanceOf(binding(classOf[Double], "ints.2")) must throwAn[ConfigurationException]
    }

    "inject list of longs" in {
      val injector = playInjector()

      injector.instanceOf(binding(classOf[Int], "longs.0")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Long], "longs.0")) must equalTo[Long](5272471683069627553L)
      injector.instanceOf(binding(classOf[Double], "longs.0")) must equalTo[Double](5272471683069627553D)

      injector.instanceOf(binding(classOf[Int], "longs.1")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Long], "longs.1")) must equalTo[Long](789875541540841016L)
      injector.instanceOf(binding(classOf[Double], "longs.1")) must equalTo[Double](789875541540841016D)

      injector.instanceOf(binding(classOf[Double], "longs.2")) must throwAn[ConfigurationException]
    }

    "inject list of doubles" in {
      val injector = playInjector()

      injector.instanceOf(binding(classOf[Int], "doubles.0")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Long], "doubles.0")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Double], "doubles.0")) must equalTo[Double](3.5D)

      injector.instanceOf(binding(classOf[Int], "doubles.1")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Long], "doubles.1")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Double], "doubles.1")) must equalTo[Double](4.5D)

      injector.instanceOf(binding(classOf[Double], "doubles.2")) must throwAn[ConfigurationException]
    }

    "inject nested non empty config" in {
      val injector = playInjector()
      val nonEmptyConf = injector.instanceOf(binding(classOf[Configuration], "nonEmptyConf"))
      nonEmptyConf must not be empty
      nonEmptyConf.getString("innerKey") must beSome("innerValue")

      injector.instanceOf(binding(classOf[String], "nonEmptyConf.innerKey")) must equalTo[String]("innerValue")
    }

    "inject empty config" in {
      val injector = playInjector()
      injector.instanceOf(binding(classOf[Configuration], "emptyConf")).entrySet must beEmpty
      injector.instanceOf(binding(classOf[Configuration], "emptyConf.innerEmptyConf")).entrySet must beEmpty
    }

    "not inject nulls" in {
      val injector = playInjector()
      injector.instanceOf(binding(classOf[Int], "nullRef")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Long], "nullRef")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Double], "nullRef")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[String], "nullRef")) must throwAn[ConfigurationException]
    }

    "not inject empty list" in {
      val injector = playInjector()
      injector.instanceOf(binding(classOf[Int], "emptyList")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Long], "emptyList")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[Double], "emptyList")) must throwAn[ConfigurationException]
      injector.instanceOf(binding(classOf[String], "emptyList")) must throwAn[ConfigurationException]
    }

  }

  def fakeContext = ApplicationLoader.createContext(Environment.simple())

  def binding[T](clazz: Class[T], qualifier: String) = {
    // @Config("qualifier")
    new BindingKey(clazz, Some(QualifierInstance(Configs.config(qualifier))))
  }

  def playInjector() = {
    val builder = new GuiceApplicationBuilder().load(new BuiltinModule, new InjectableConfigModule(Some("icm")))
    val loader = new GuiceApplicationLoader(builder)
    val app = loader.load(fakeContext)
    app.injector
  }

}
