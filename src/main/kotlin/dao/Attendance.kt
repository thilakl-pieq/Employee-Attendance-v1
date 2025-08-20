package dao

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class Attendance(

    @get:NotBlank(message = "Employee ID cannot be blank")
    @get:JsonProperty("id")
    val employeeId: UUID,

    @get:NotNull(message = "Check-in date and time is required")
    @get:JsonProperty("checkInDateTime")
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    val checkInDateTime: LocalDateTime,

    @get:JsonProperty("checkOutDateTime")
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    var checkOutDateTime: LocalDateTime? = null,


) {
    override fun toString(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val checkInStr = checkInDateTime.format(formatter)
        val checkOutStr = checkOutDateTime?.format(formatter) ?: "Not Checked Out"
        return "ID: $employeeId | Check-in: $checkInStr | Check-out: $checkOutStr"
    }
}
