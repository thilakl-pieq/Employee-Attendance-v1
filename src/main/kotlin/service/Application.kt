package service

import BasicHealthCheck
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.databind.SerializationFeature
import dao.EmployeeList
import dao.AttendanceList
import io.dropwizard.client.JerseyClientBuilder
import resources.EmployeeResource
import resources.AttendanceResource
import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import com.fasterxml.jackson.module.kotlin.kotlinModule
import config.Configuration
import jakarta.servlet.DispatcherType
import org.eclipse.jetty.servlets.CrossOriginFilter
import java.util.EnumSet

class AppMain : Application<Configuration>() {

    override fun initialize(bootstrap: Bootstrap<Configuration>) {
        bootstrap.objectMapper.registerModule(kotlinModule())
        bootstrap.objectMapper.registerModule(JavaTimeModule())
        bootstrap.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    override fun run(configuration: Configuration, environment: Environment) {
        // Data lists
        val employeeList = EmployeeList()
        val attendanceList = AttendanceList()

        // Dropwizard HTTP client (for AttendanceService's employeeExists check)
        val client = JerseyClientBuilder(environment)
            .build("employee-api-client")

        // Services
        val employeeService = EmployeeService(employeeList)
        val attendanceService = AttendanceService(attendanceList, client)

        // Resources (pass services to them, not lists)
        val employeeResource = EmployeeResource(employeeService)
        val attendanceResource = AttendanceResource(attendanceService)

        // Register resources with Dropwizard
        environment.jersey().register(employeeResource)
        environment.jersey().register(attendanceResource)

        // Basic health check
        val basicHealthCheck = BasicHealthCheck()
        environment.healthChecks().register("basic", basicHealthCheck)

        val cors = environment.servlets().addFilter("CORS", CrossOriginFilter::class.java)
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "http://localhost:3000")  // Or "*" for all origins (less secure)
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin,Authorization")
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD")
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true")

        // Map the filter to all URL patterns
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, "/*")
    }
}

fun main(args: Array<String>) {
    AppMain().run(*args)
}
