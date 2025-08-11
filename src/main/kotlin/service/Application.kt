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
    }
}

fun main(args: Array<String>) {
    AppMain().run(*args)
}
