## Injectable Configuration Module

An extension to `Playframework` adding injectable configuration.
Having a following `application.conf`:

```
icm.int = 121535
icm.long = 1768619760268566049
icm.double = 2.5

icm.false = false
icm.true = true

icm.nonEmptyString = "character sequence"
icm.emptyString = ""

icm.nullRef = null

icm.emptyList = []
icm.ints = [523969, 177618]
icm.longs = [5272471683069627553, 789875541540841016]
icm.doubles = [3.5, 4.5]

icm.emptyConf = {

  innerEmptyConf = {

  }

}

icm.nonEmptyConf = {
  innerKey = "innerValue"
}

icm.complex = [0, {
  object = {
    hello = "world"
  },
  flat = "flat"
}, "boom", false]
```

This module allows injecting body of configuration in following manner:

```
class Application @Inject()(@Config("icm.int") intScala: Int,
                            @Config("icm.int") intJava: java.lang.Integer,
                            @Config("icm.int") intLongScala: Long,
                            @Config("icm.int") intLongJava: java.lang.Long,
                            @Config("icm.int") intDoubleScala: Double,
                            @Config("icm.int") intDoubleJava: java.lang.Double,

                            @Config("icm.complex.1") configScala: Configuration,
                            @Config("icm.complex.1") configJava: play.Configuration,

                            @Config("icm.complex.2") boom: String,

                            @Config("icm.nonEmptyString") stringScala: String,
                            @Config("icm.nonEmptyString") stringScala: java.lang.String) extends Controller {

}
```

### Limits

- No `Duration` handling due to Typesafe parsing API being private.

