package dao

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Attendance(
    val id: String,
    val checkInDateTime: LocalDateTime,
    var checkOutDateTime: LocalDateTime? = null,
    var workingHours: String = ""
) {

    fun addCheckOutTimeToAttendance(checkOutTime: LocalDateTime): Pair<Boolean, String> {
        if (checkOutTime.isBefore(checkInDateTime)) {
            return false to "Checkout time cannot be before checkin time"
        }
        if (checkOutTime.toLocalDate() != checkInDateTime.toLocalDate()) {
            return false to "Check-out must be on the same date as check-in."
        }
        this.checkOutDateTime = checkOutTime
        val success = calculateWorkingHours()
        return if (success) true to "Check-out successful" else false to "Failed to calculate working hours"
    }

    private fun calculateWorkingHours(): Boolean {
        val checkOutTime = checkOutDateTime ?: return false
        val duration = Duration.between(checkInDateTime, checkOutTime)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60

        workingHours = "%02d:%02d:%02d".format(hours, minutes, seconds)
        return true
    }

    override fun toString(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val checkInStr = checkInDateTime.format(formatter)
        val checkOutStr = checkOutDateTime?.format(formatter) ?: "Not Checked Out"
        return "ID: $id | Check-in: $checkInStr | Check-out: $checkOutStr | Hours: $workingHours"
    }
}
