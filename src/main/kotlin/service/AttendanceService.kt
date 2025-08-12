package service

import dao.AttendanceList
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.core.Response
import java.time.LocalDateTime

class AttendanceService(
    private val attendanceList: AttendanceList,
    private val httpClient: Client
) {
    private fun employeeExists(employeeId: String): Boolean {
        val url = "http://localhost:8080/employees/$employeeId"
        val response = httpClient.target(url).request().get()
        return response.status == 200
    }

    fun checkIn(employeeId: String, checkInDateTime: LocalDateTime): Pair<Response.Status, Any> {
        if (employeeId.isBlank()) {
            return Response.Status.BAD_REQUEST to mapOf("error" to "Employee ID cannot be empty")
        }
        if (!employeeExists(employeeId)) {
            return Response.Status.NOT_FOUND to mapOf("error" to "Employee ID $employeeId does not exist")
        }
        if (attendanceList.hasAlreadyCheckedIn(employeeId, checkInDateTime)) {
            return Response.Status.CONFLICT to mapOf("error" to "Employee has already checked in on this date")
        }
        if (attendanceList.any { it.id == employeeId && it.checkOutDateTime == null }) {
            return Response.Status.CONFLICT to mapOf("error" to "Employee already has an open attendance entry")
        }

        val attendance = Attendance(employeeId, checkInDateTime)
        if (!attendanceList.add(attendance)) {
            return Response.Status.CONFLICT to mapOf("error" to "Could not add attendance entry")
        }

        return Response.Status.CREATED to attendance
    }

    fun checkOut(employeeId: String, checkOutDateTime: LocalDateTime): Pair<Response.Status, Any> {
        if (employeeId.isBlank()) {
            return Response.Status.BAD_REQUEST to mapOf("error" to "Employee ID cannot be empty")
        }
        if (!employeeExists(employeeId)) {
            return Response.Status.NOT_FOUND to mapOf("error" to "Employee ID $employeeId does not exist")
        }

        val openAttendance = attendanceList.find { it.id == employeeId && it.checkOutDateTime == null }
            ?: return Response.Status.NOT_FOUND to mapOf("error" to "No open attendance record found")

        val (success, message) = attendanceList.addCheckOutToList(openAttendance, checkOutDateTime)

        return if (success) {
            Response.Status.OK to openAttendance
        } else {
            Response.Status.BAD_REQUEST to mapOf("error" to message)
        }
    }

    fun getAttendanceBySummary(from: LocalDateTime,to: LocalDateTime) {


    }

    fun getAllAttendances(): List<Attendance> = attendanceList
}
