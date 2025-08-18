package config

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.core.Configuration

class Configuration : Configuration() {  // Notice the fully qualified import ensures you extend the Dropwizard class
    @JsonProperty
    lateinit var template: String

    @JsonProperty
    lateinit var defaultName: String


}
