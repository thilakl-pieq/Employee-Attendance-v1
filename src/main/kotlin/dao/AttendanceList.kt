package dao

import service.Attendance
import java.time.Duration
import java.time.LocalDateTime

class AttendanceList : ArrayList<Attendance>() {

    override fun add(attendance: Attendance): Boolean {
        if (this.any { it.id == attendance.id && it.checkOutDateTime == null }) {
            return false
        }
        return super.add(attendance)
    }

    fun hasAlreadyCheckedIn(id: String, date: LocalDateTime): Boolean {
        return this.any { it.id == id && it.checkInDateTime.toLocalDate() == date.toLocalDate() }
    }

    fun addCheckOutToList(attendance: Attendance, checkOut: LocalDateTime): Pair<Boolean, String> {
        val index = this.indexOfFirst { it.id == attendance.id && it.checkOutDateTime == null }

        if (index == -1) {
            return false to "No open attendance to update for ID ${attendance.id}"
        }

        // Moved logic from Attendance.addCheckOutTimeToAttendance here:
        if (checkOut.isBefore(attendance.checkInDateTime)) {
            return false to "Checkout time cannot be before checkin time"
        }
        if (checkOut.toLocalDate() != attendance.checkInDateTime.toLocalDate()) {
            return false to "Check-out must be on the same date as check-in."
        }

        attendance.checkOutDateTime = checkOut

        val duration = Duration.between(attendance.checkInDateTime, checkOut)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        attendance.workingHours = "%02d:%02d:%02d".format(hours, minutes, seconds)

        this[index] = attendance
        return true to "Check-out successful"
    }

    fun getActiveAttendances(): List<Attendance> {
        return this.filter { it.checkOutDateTime == null }
    }

    fun getAttendancesBetween(from: LocalDateTime, to: LocalDateTime): String {
        val attendancesBetween = this.filter {
            it.checkInDateTime.isAfter(from) && it.checkInDateTime.isBefore(to)
        }

        if (attendancesBetween.isEmpty()) {
            return "No attendance records found for the given time range."
        }

        val groupedById = attendancesBetween.groupBy { it.id }

        return groupedById.entries.joinToString("\n") { (id, records) ->
            val totalHours = records.sumOf { attendance ->
                val parts = attendance.workingHours.split(":")
                val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val seconds = parts.getOrNull(2)?.toIntOrNull() ?: 0
                hours + (minutes / 60.0) + (seconds / 3600.0)
            }
            "ID: $id -> Total Working Hours: %.2f hrs".format(totalHours)
        }
    }

    override fun toString(): String {
        if (this.isEmpty()) return "No attendance records found."
        return this.joinToString("\n") { it.toString() }
    }
}
