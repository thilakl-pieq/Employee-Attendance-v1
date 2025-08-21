package service

import BasicHealthCheck
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.kotlinModule
import config.Configuration
import dao.AttendanceDao
import dao.EmployeeDao
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import io.dropwizard.jdbi3.JdbiFactory
import org.eclipse.jetty.servlets.CrossOriginFilter
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.core.kotlin.KotlinPlugin
import resources.EmployeeResource
import resources.AttendanceResource
import java.util.EnumSet

class AppMain : Application<Configuration>() {
    override fun initialize(bootstrap: Bootstrap<Configuration>) {
        bootstrap.objectMapper.registerModule(kotlinModule())
        bootstrap.objectMapper.registerModule(JavaTimeModule())
        bootstrap.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    override fun run(configuration: Configuration, environment: Environment) {
        // Setup JDBI
        val factory = JdbiFactory()
        val jdbi: Jdbi = factory.build(environment, configuration.database, "postgresql")
        jdbi.installPlugin(KotlinPlugin())


        val employeeDao = EmployeeDao(jdbi)
        val employeeService = EmployeeService(employeeDao)
        val attendanceDao = AttendanceDao(jdbi)
//        val attendanceService = AttendanceDao(attendanceDao)

        val client = JerseyClientBuilder(environment).build("employee-api-client")
        val attendanceService = AttendanceService(attendanceDao, client)

        environment.jersey().register(EmployeeResource(employeeService))
        environment.jersey().register(AttendanceResource(attendanceService))

        environment.healthChecks().register("basic", BasicHealthCheck(jdbi))

        val cors = environment.servlets().addFilter("CORS", CrossOriginFilter::class.java)
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "http://localhost:3000")
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Authorization")
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD")
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true")
        cors.addMappingForUrlPatterns(EnumSet.allOf(jakarta.servlet.DispatcherType::class.java), true, "/*")
    }

}

fun main(args: Array<String>) {
    AppMain().run(*args)

}
