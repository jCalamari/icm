package models

import com.typesafe.config.{ConfigObject, ConfigValue, ConfigValueType}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

import scala.collection.JavaConverters._
import scala.language.postfixOps

class InjectableConfigModule(basePath: Option[String] = None) extends Module {

  def this() {
    this(None)
  }

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val injectableConfig = basePath.flatMap(configuration.getConfig).getOrElse(configuration).underlying
    injectableConfig.root().entrySet().asScala.map(e => e.getKey -> e.getValue).toSeq.flatMap { case (key, value) =>
      bind(key, value)
    }
  }

  private def bind(key: String, value: ConfigValue): List[Binding[_]] = {
    value.valueType() match {
      case ConfigValueType.OBJECT => bindObject(key, value) ::: bindObjectBody(key, value.asInstanceOf[java.util.Map[String, ConfigValue]].asScala.toMap)
      case ConfigValueType.LIST => bindListBody(key, value.asInstanceOf[java.util.List[ConfigValue]].asScala.toList)
      case ConfigValueType.NUMBER => bindNumber(key, value)
      case ConfigValueType.BOOLEAN => List(bind[java.lang.Boolean].qualifiedWith(Configs.config(key)).toInstance(value.unwrapped().asInstanceOf[java.lang.Boolean]))
      case ConfigValueType.NULL => Nil
      case ConfigValueType.STRING => List(bind[String].qualifiedWith(Configs.config(key)).toInstance(value.unwrapped().toString))
    }
  }

  private def bindNumber(key: String, value: ConfigValue): List[Binding[_]] = {
    val qualifier = Configs.config(key)
    value.unwrapped() match {
      case i: java.lang.Integer => List(
        bind[java.lang.Integer].qualifiedWith(qualifier).toInstance(i),
        bind[java.lang.Long].qualifiedWith(qualifier).toInstance(java.lang.Long.valueOf(i.longValue())),
        bind[java.lang.Double].qualifiedWith(qualifier).toInstance(java.lang.Double.valueOf(i.doubleValue()))
      )
      case l: java.lang.Long => List(
        bind[java.lang.Long].qualifiedWith(qualifier).toInstance(l),
        bind[java.lang.Double].qualifiedWith(qualifier).toInstance(java.lang.Double.valueOf(l.doubleValue()))
      )
      case d: java.lang.Double => List(bind[java.lang.Double].qualifiedWith(qualifier).toInstance(d))
      case _ => Nil
    }
  }

  private def bindListBody(key: String, values: List[ConfigValue], index: Int = 0): List[Binding[_]] = {
    values match {
      case Nil => Nil
      case head :: tail => bind(key + "." + index, head) ::: bindListBody(key, tail, index + 1)
    }
  }

  private def bindObject(key: String, value: ConfigValue): List[Binding[_]] = {
    List(
      bind[Configuration].qualifiedWith(Configs.config(key)).toInstance(Configuration(value.asInstanceOf[ConfigObject].toConfig)),
      bind[play.Configuration].qualifiedWith(Configs.config(key)).toInstance(new play.Configuration(value.asInstanceOf[ConfigObject].toConfig))
    )
  }

  private def bindObjectBody(key: String, value: Map[String, ConfigValue]): List[Binding[_]] = {
    value.flatMap { case (mKey, mVal) =>
      bind(key + "." + mKey, mVal)
    }.toList
  }

}
