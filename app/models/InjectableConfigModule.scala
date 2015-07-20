package models

import com.typesafe.config.{ConfigObject, ConfigValue, ConfigValueType}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

import scala.collection.JavaConverters._
import scala.language.postfixOps


class InjectableConfigModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    configuration.entrySet.flatMap { case (key, value) =>
      decide(key, value)
    } toSeq
  }

  private def decide(key: String, value: ConfigValue): List[Binding[_]] = {
    value.valueType() match {
      case ConfigValueType.OBJECT => bindObject(key, value) :: bindObjectBody(key, value.asInstanceOf[java.util.Map[String, ConfigValue]].asScala.toMap)
      case ConfigValueType.LIST => bindListBody(key, value.asInstanceOf[java.util.List[ConfigValue]].asScala.toList)
      case ConfigValueType.NUMBER => bindNumber(key, value)
      case ConfigValueType.BOOLEAN => List(bind[java.lang.Boolean].qualifiedWith(Configs.config(key)).toInstance(value.unwrapped().asInstanceOf[java.lang.Boolean]))
      case ConfigValueType.NULL => Nil
      case ConfigValueType.STRING => List(bind[String].qualifiedWith(Configs.config(key)).toInstance(value.unwrapped().toString))
    }
  }

  private def bindNumber(key: String, value: ConfigValue): List[Binding[_]] = {
    value.unwrapped() match {
      case d: java.lang.Double => List(bind[java.lang.Double].qualifiedWith(Configs.config(key)).toInstance(d))
      case i: java.lang.Integer => List(bind[java.lang.Integer].qualifiedWith(Configs.config(key)).toInstance(i))
      case l: java.lang.Long => List(bind[java.lang.Long].qualifiedWith(Configs.config(key)).toInstance(l))
      case _ => Nil
    }
  }

  private def bindListBody(key: String, values: List[ConfigValue], index: Int = 0): List[Binding[_]] = {
    values match {
      case Nil => Nil
      case head :: tail =>
        decide(key + "." + index, head) :::
          bindListBody(key, tail, index + 1)
    }
  }

  private def bindObject(key: String, value: ConfigValue): Binding[_] = {
    bind[Configuration].qualifiedWith(Configs.config(key)).toInstance(Configuration(value.asInstanceOf[ConfigObject].toConfig))
  }

  private def bindObjectBody(key: String, value: Map[String, ConfigValue]): List[Binding[_]] = {
    value.flatMap { case (mapKey, mapValue) =>
      decide(key + "." + mapKey, mapValue)
    }.toList
  }

}
