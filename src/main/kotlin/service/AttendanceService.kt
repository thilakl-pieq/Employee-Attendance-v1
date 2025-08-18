package service

import dao.Attendance
import dao.AttendanceList
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.core.Response
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

class AttendanceService(
    private val attendanceList: AttendanceList,
    private val httpClient: Client
) {

    private val log = LoggerFactory.getLogger(AttendanceService::class.java)

//    private val employeeList: EmployeeList
//    private fun employeeExists(employeeId: String): Boolean {
//        return employeeList.employeeExists(employeeId)
//    }

    private fun employeeExists(employeeId: String): Boolean {
        log.debug("Checking if employee with ID $employeeId exists via HTTP call")
        val url = "http://localhost:8080/employees/$employeeId"
        val response = httpClient.target(url).request().get()
        val exists = response.status == 200
        log.debug("Employee $employeeId exists = $exists")
        return exists
    }

    fun checkIn(employeeId: String, checkInDateTime: LocalDateTime): Pair<Response.Status, Any> {
        log.info("Attempting check-in for $employeeId at $checkInDateTime")

        if (employeeId.isBlank()) {
            log.warn("Check-in failed: blank employee ID")
            return Response.Status.BAD_REQUEST to mapOf("error" to "Employee ID cannot be empty")
        }
        if (!employeeExists(employeeId)) {
            log.warn("Check-in failed: Employee $employeeId does not exist")
            return Response.Status.NOT_FOUND to mapOf("error" to "Employee ID $employeeId does not exist")
        }
        if (attendanceList.hasAlreadyCheckedIn(employeeId, checkInDateTime)) {
            log.warn("Check-in failed: $employeeId already checked in on ${checkInDateTime.toLocalDate()}")
            return Response.Status.CONFLICT to mapOf("error" to "Employee has already checked in on this date")
        }
        if (attendanceList.any { it.id == employeeId && it.checkOutDateTime == null }) {
            log.warn("Check-in failed: $employeeId already has an open attendance entry")
            return Response.Status.CONFLICT to mapOf("error" to "Employee already has an open attendance entry")
        }

        val attendance = Attendance(employeeId, checkInDateTime)
        if (!attendanceList.add(attendance)) {
            log.error("Check-in failed: Could not add attendance for $employeeId")
            return Response.Status.CONFLICT to mapOf("error" to "Could not add attendance entry")
        }

        log.info("Check-in successful for $employeeId at $checkInDateTime")
        return Response.Status.CREATED to attendance
    }

    fun checkOut(employeeId: String, checkOutDateTime: LocalDateTime): Pair<Response.Status, Any> {
        log.info("Attempting check-out for $employeeId at $checkOutDateTime")

        if (employeeId.isBlank()) {
            log.warn("Check-out failed: blank employee ID")
            return Response.Status.BAD_REQUEST to mapOf("error" to "Employee ID cannot be empty")
        }
        if (!employeeExists(employeeId)) {
            log.warn("Check-out failed: Employee $employeeId does not exist")
            return Response.Status.NOT_FOUND to mapOf("error" to "Employee ID $employeeId does not exist")
        }

        val openAttendance = attendanceList.find { it.id == employeeId && it.checkOutDateTime == null }
            ?: run {
                log.warn("Check-out failed: No open attendance record for $employeeId")
                return Response.Status.NOT_FOUND to mapOf("error" to "No open attendance record found")
            }

        val (success, message) = attendanceList.addCheckOutToList(openAttendance, checkOutDateTime)

        return if (success) {
            log.info("Check-out successful for $employeeId at $checkOutDateTime")
            Response.Status.OK to openAttendance
        } else {
            log.warn("Check-out failed for $employeeId: $message")
            Response.Status.BAD_REQUEST to mapOf("error" to message)
        }
    }

    fun getAttendanceBySummary(from: LocalDateTime, to: LocalDateTime): Pair<Response.Status, Any> {
        log.info("Fetching attendance summary from $from to $to")
        val (success, message) = attendanceList.getAttendancesBetween(from, to)
        return if (success) {
            log.debug("Summary retrieved successfully")
            Response.Status.OK to message
        } else {
            log.warn("Summary fetch failed: $message")
            Response.Status.BAD_REQUEST to mapOf("error" to message)
        }
    }

    fun getAllAttendances(): List<Attendance> {
        log.debug("Fetching all attendances (total=${attendanceList.size})")
        return attendanceList
    }
}
