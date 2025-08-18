package dao

import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime

class AttendanceList : ArrayList<Attendance>() {

    private val log = LoggerFactory.getLogger(AttendanceList::class.java)

    override fun add(attendance: Attendance): Boolean {
        if (this.any { it.id == attendance.id && it.checkOutDateTime == null }) {
            log.warn("Cannot add attendance: ${attendance.id} already has an open record")
            return false
        }
        log.debug("Adding attendance for ${attendance.id} at ${attendance.checkInDateTime}")
        return super.add(attendance)
    }

    fun hasAlreadyCheckedIn(id: String, date: LocalDateTime): Boolean {
        val already = this.any { it.id == id && it.checkInDateTime.toLocalDate() == date.toLocalDate() }
        log.debug("Has $id already checked in on ${date.toLocalDate()}? $already")
        return already
    }

    fun addCheckOutToList(attendance: Attendance, checkOut: LocalDateTime): Pair<Boolean, String> {
        val index = this.indexOfFirst { it.id == attendance.id && it.checkOutDateTime == null }

        if (index == -1) {
            log.warn("Checkout update failed: no open record for ${attendance.id}")
            return false to "No open attendance to update for ID ${attendance.id}"
        }

        if (checkOut.isBefore(attendance.checkInDateTime)) {
            log.warn("Invalid checkout for ${attendance.id}: before check-in time")
            return false to "Checkout time cannot be before checkin time"
        }
        if (checkOut.toLocalDate() != attendance.checkInDateTime.toLocalDate()) {
            log.warn("Invalid checkout for ${attendance.id}: different date than check-in")
            return false to "Check-out must be on the same date as check-in."
        }

        attendance.checkOutDateTime = checkOut

        val duration = Duration.between(attendance.checkInDateTime, checkOut)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        attendance.workingHours = "%02d:%02d:%02d".format(hours, minutes, seconds)

        this[index] = attendance
        log.info("Check-out recorded for ${attendance.id} at $checkOut (${attendance.workingHours} hours)")
        return true to "Check-out successful"
    }

    fun getAttendancesBetween(from: LocalDateTime, to: LocalDateTime): Pair<Boolean, String> {
        log.debug("Searching attendances between $from and $to")
        val attendancesBetween = this.filter {
            it.checkInDateTime.isAfter(from) && it.checkInDateTime.isBefore(to)
        }

        if (attendancesBetween.isEmpty()) {
            log.warn("No attendances found between $from and $to")
            return false to "No attendance records found for the given time range."
        }

        val groupedById = attendancesBetween.groupBy { it.id }

        val summary = groupedById.entries.joinToString("\n") { (id, records) ->
            val totalHours = records.sumOf { a ->
                val parts = a.workingHours.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val s = parts.getOrNull(2)?.toIntOrNull() ?: 0
                h + (m / 60.0) + (s / 3600.0)
            }
            "ID: $id -> Total Working Hours: %.2f hrs".format(totalHours)
        }

        log.info("Attendance summary created for ${groupedById.size} employees")
        return true to summary
    }
}
