package config

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration

class Configuration : Configuration() {

//    @NotEmpty
    @JsonProperty
    var template: String = "Hello, %s!"

//    @NotEmpty
    @JsonProperty
    var defaultName: String = "Stranger"
}