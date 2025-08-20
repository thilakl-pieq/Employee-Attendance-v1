package service

import dao.Attendance
import dao.AttendanceDao
import jakarta.ws.rs.core.Response
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.UUID
import jakarta.ws.rs.client.Client

class AttendanceService(
    private val attendanceDao: AttendanceDao,
    private val httpClient: Client
) {

    private val log = LoggerFactory.getLogger(AttendanceService::class.java)

    private fun employeeExists(employeeId: UUID): Boolean {
        log.debug("Checking if employee with ID $employeeId exists via HTTP call")
        val url = "http://localhost:8081/employees/$employeeId"
        val response = httpClient.target(url).request().get()
        val exists = response.status == 200
        log.debug("Employee $employeeId exists = $exists")
        return exists
    }

    fun checkIn(employeeId: UUID, checkInDateTime: LocalDateTime): Pair<Response.Status, Any> {
        log.info("Attempting check-in for $employeeId at $checkInDateTime")

        if (!employeeExists(employeeId)) {
            log.warn("Check-in failed: Employee $employeeId does not exist")
            return Response.Status.NOT_FOUND to mapOf("error" to "Employee ID $employeeId does not exist")
        }

        if (attendanceDao.hasAlreadyCheckedIn(employeeId, checkInDateTime)) {
            log.warn("Check-in failed: $employeeId already checked in on ${checkInDateTime.toLocalDate()}")
            return Response.Status.CONFLICT to mapOf("error" to "Employee has already checked in on this date and time")
        }

        val attendance = Attendance(employeeId, checkInDateTime, null)
        attendanceDao.insertAttendance(attendance)

        log.info("Check-in successful for $employeeId at $checkInDateTime")
        return Response.Status.CREATED to attendance
    }

    fun checkOut(employeeId: UUID, checkOutDateTime: LocalDateTime): Pair<Response.Status, Any> {
        log.info("Attempting check-out for $employeeId at $checkOutDateTime")

        if (!employeeExists(employeeId)) {
            log.warn("Check-out failed: Employee $employeeId does not exist")
            return Response.Status.NOT_FOUND to mapOf("error" to "Employee ID $employeeId does not exist")
        }

        // Find open attendance (check-in without check-out)
        val openAttendances = attendanceDao.getAttendanceByEmployee(employeeId)
            .filter { it.checkOutDateTime == null }
            .filter { it.checkInDateTime.toLocalDate() == checkOutDateTime.toLocalDate() }

        if (openAttendances.isEmpty()) {
            log.warn("Check-out failed: No open attendance record for $employeeId on date ${checkOutDateTime.toLocalDate()}")
            return Response.Status.NOT_FOUND to mapOf("error" to "No open attendance record found for check-out")
        }

        val openAttendance = openAttendances.first()

        if (checkOutDateTime.isBefore(openAttendance.checkInDateTime)) {
            log.warn("Check-out failed: Check-out time is before check-in time for $employeeId")
            return Response.Status.BAD_REQUEST to mapOf("error" to "Check-out time cannot be before check-in time")
        }

        attendanceDao.updateCheckOut(employeeId, openAttendance.checkInDateTime, checkOutDateTime)

        log.info("Check-out successful for $employeeId at $checkOutDateTime")
        return Response.Status.OK to mapOf("message" to "Check-out successful")
    }
    fun getAllAttendances(limit: Int): List<Attendance> {
        log.info("Fetching all attendances with limit $limit")
        return attendanceDao.getAllAttendance(limit)
    }
    fun getWorkingHoursSummary(from: LocalDateTime, to: LocalDateTime): Pair<Response.Status, Any> {
        log.info("Fetching working hours summary from $from to $to")
        return try {
            val summary = attendanceDao.getWorkingHoursSummaryByDateRange(from, to)
            Response.Status.OK to summary
        } catch (e: Exception) {
            log.error("Failed to fetch working hours summary", e)
            Response.Status.INTERNAL_SERVER_ERROR to mapOf("error" to (e.message ?: "Unknown error"))
        }
    }

}
