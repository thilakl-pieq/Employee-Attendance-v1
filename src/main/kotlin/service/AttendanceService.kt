package service

import dao.Attendance
import dao.AttendanceDao
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
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

    /**
     * Attempts check-in for an employee at the provided date and time.
     * @throws NotFoundException if employee does not exist
     * @throws BadRequestException if employee already checked-in at that time or has an open attendance record
     */
    fun checkIn(employeeId: UUID, checkInDateTime: LocalDateTime): Attendance {
        log.info("Attempting check-in for $employeeId at $checkInDateTime")
        if (!employeeExists(employeeId)) {
            log.warn("Check-in failed: Employee $employeeId does not exist")
            throw NotFoundException("Employee ID $employeeId does not exist")
        }
        if (attendanceDao.hasOpenAttendance(employeeId)) {
            log.warn("Check-in failed: Employee $employeeId already has an open attendance record")
            throw BadRequestException("Employee has an open attendance record and cannot check in again")
        }
        if (attendanceDao.hasAlreadyCheckedIn(employeeId, checkInDateTime)) {
            log.warn("Check-in failed: $employeeId already checked in on ${checkInDateTime.toLocalDate()}")
            throw BadRequestException("Employee has already checked in on this date and time")
        }
        val attendance = Attendance(employeeId, checkInDateTime, null)
        attendanceDao.insertAttendance(attendance)
        log.info("Check-in successful for $employeeId at $checkInDateTime")
        return attendance
    }

    /**
     * Attempts check-out for employee at the provided date and time.
     * @throws NotFoundException if employee does not exist or no open attendance record
     * @throws BadRequestException if check-out time is before check-in time
     */
    fun checkOut(employeeId: UUID, checkOutDateTime: LocalDateTime) {
        log.info("Attempting check-out for $employeeId at $checkOutDateTime")
        if (!employeeExists(employeeId)) {
            log.warn("Check-out failed: Employee $employeeId does not exist")
            throw NotFoundException("Employee ID $employeeId does not exist")
        }
        val openAttendances = attendanceDao.getAttendanceByEmployee(employeeId)
            .filter { it.checkOutDateTime == null }
            .filter { it.checkInDateTime.toLocalDate() == checkOutDateTime.toLocalDate() }
        if (openAttendances.isEmpty()) {
            log.warn("Check-out failed: No open attendance record for $employeeId on date ${checkOutDateTime.toLocalDate()}")
            throw NotFoundException("No open attendance record found for check-out")
        }
        val openAttendance = openAttendances.first()
        if (checkOutDateTime.isBefore(openAttendance.checkInDateTime)) {
            log.warn("Check-out failed: Check-out time is before check-in time for $employeeId")
            throw BadRequestException("Check-out time cannot be before check-in time")
        }
        attendanceDao.updateCheckOut(employeeId, openAttendance.checkInDateTime, checkOutDateTime)
        log.info("Check-out successful for $employeeId at $checkOutDateTime")
    }

    /**
     * Returns all attendance records with an optional limit.
     */
    fun getAllAttendances(limit: Int): List<Attendance> {
        log.info("Fetching all attendances with limit $limit")
        return attendanceDao.getAllAttendance(limit)
    }

    /**
     * Returns working hours summary between given from and to date.
     * @throws BadRequestException on error retrieving summary
     */
    fun getWorkingHoursSummary(from: LocalDateTime, to: LocalDateTime): List<Map<String, Any>> {
        log.info("Fetching working hours summary from $from to $to")
        try {
            return attendanceDao.getWorkingHoursSummaryByDateRange(from, to)
        } catch (e: Exception) {
            log.error("Failed to fetch working hours summary", e)
            throw BadRequestException(e.message ?: "Unknown error fetching working hours summary")
        }
    }
}
