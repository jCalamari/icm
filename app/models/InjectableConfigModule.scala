package models

import com.typesafe.config.{ConfigObject, ConfigValue, ConfigValueType}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

import scala.collection.JavaConverters._

class InjectableConfigModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val injectableConfig = configuration.getString("play.inject.path").flatMap(configuration.getConfig).getOrElse(configuration).underlying
    injectableConfig.root().entrySet().asScala.map(e => e.getKey -> e.getValue).toSeq.flatMap { case (key, value) =>
      bind(key, value)
    }
  }

  private def bind(key: String, value: ConfigValue): List[Binding[_]] = value.valueType() match {
    case ConfigValueType.OBJECT => bindObject(key, value) ::: bindObjectBody(key, value.asMap())
    case ConfigValueType.LIST => bindListBody(key, value.asList())
    case ConfigValueType.NUMBER => bindNumber(key, value)
    case ConfigValueType.BOOLEAN => bindBoolean(value.asJavaBoolean(), key)
    case ConfigValueType.NULL => Nil
    case ConfigValueType.STRING => bindString(value.unwrapped().toString, key)
  }

  private def bindNumber(key: String, value: ConfigValue): List[Binding[_]] = value.unwrapped() match {
    case i: java.lang.Integer => List(
      bindInteger(i, key),
      bindLong(i.longValue(), key),
      bindDouble(i.doubleValue(), key)
    )
    case l: java.lang.Long => List(
      bindLong(l, key),
      bindDouble(l.doubleValue(), key)
    )
    case d: java.lang.Double => List(bindDouble(d, key))
    case _ => Nil
  }

  private def bindString(s: java.lang.String, key: String): List[Binding[_]] = {
    List(bind[String].qualifiedWith(Configs.configured(key)).toInstance(s))
  }

  private def bindBoolean(b: java.lang.Boolean, key: String): List[Binding[_]] = {
    List(bind[java.lang.Boolean].qualifiedWith(Configs.configured(key)).toInstance(b))
  }

  private def bindInteger(i: java.lang.Integer, key: String): Binding[_] = {
    bind[java.lang.Integer].qualifiedWith(Configs.configured(key)).toInstance(i)
  }

  private def bindLong(l: java.lang.Long, key: String): Binding[_] = {
    bind[java.lang.Long].qualifiedWith(Configs.configured(key)).toInstance(l)
  }

  private def bindDouble(d: java.lang.Double, key: String): Binding[_] = {
    bind[java.lang.Double].qualifiedWith(Configs.configured(key)).toInstance(d)
  }

  private def bindListBody(key: String, values: List[ConfigValue], index: => Int = 0): List[Binding[_]] = values match {
    case Nil => Nil
    case head :: tail => bind(key + "." + index, head) ::: bindListBody(key, tail, index + 1)
  }

  private def bindObject(key: String, value: ConfigValue): List[Binding[_]] = {
    List(
      bind[Configuration].qualifiedWith(Configs.configured(key)).toInstance(value.asConfig()),
      bind[play.Configuration].qualifiedWith(Configs.configured(key)).toInstance(value.asJavaConfig())
    )
  }

  private def bindObjectBody(key: String, value: Map[String, ConfigValue]): List[Binding[_]] = value.flatMap { case (mKey, mVal) =>
    bind(key + "." + mKey, mVal)
  }.toList

  private implicit class RichConfigValue(value: ConfigValue) {
    def asMap(): Map[String, ConfigValue] = value.asInstanceOf[java.util.Map[String, ConfigValue]].asScala.toMap

    def asList(): List[ConfigValue] = value.asInstanceOf[java.util.List[ConfigValue]].asScala.toList

    def asConfig(): Configuration = Configuration(value.asInstanceOf[ConfigObject].toConfig)

    def asJavaConfig(): play.Configuration = new play.Configuration(value.asInstanceOf[ConfigObject].toConfig)

    def asJavaBoolean(): java.lang.Boolean = value.unwrapped().asInstanceOf[java.lang.Boolean]
  }

}
