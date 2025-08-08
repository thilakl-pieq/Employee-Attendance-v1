package service

import BasicHealthCheck
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.databind.SerializationFeature
import dao.EmployeeList
import dao.AttendanceList
import resources.EmployeeResource
import resources.AttendanceResource
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import com.fasterxml.jackson.module.kotlin.kotlinModule
import config.Configuration

class Application : Application<Configuration>() {

    override fun initialize(bootstrap: Bootstrap<Configuration>) {

        bootstrap.objectMapper.registerModule(kotlinModule())


        bootstrap.objectMapper.registerModule(JavaTimeModule())


        bootstrap.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    override fun run(configuration: Configuration, environment: Environment) {
        val employeeList = EmployeeList()
        val attendanceList = AttendanceList()
        val employeeResource = EmployeeResource(employeeList)
        val attendanceResource = AttendanceResource(attendanceList)
        environment.jersey().register(employeeResource)
        environment.jersey().register(attendanceResource)
        val basicHealthCheck = BasicHealthCheck()
        environment.healthChecks().register("basic", basicHealthCheck)
    }
}

fun main(args: Array<String>) {
    Application().run(*args)
}
