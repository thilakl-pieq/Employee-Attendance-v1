package config

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.core.Configuration
import io.dropwizard.db.DataSourceFactory


class Configuration : Configuration() {

    @JsonProperty
    lateinit var template: String

    @JsonProperty
    lateinit var defaultName: String

    @JsonProperty("database")
    lateinit var database: DataSourceFactory
}
