package dao

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.UUID

class AttendanceDao(private val jdbi: Jdbi) {

    private val log = LoggerFactory.getLogger(AttendanceDao::class.java)

    fun insertAttendance(attendance: Attendance): Int {
        log.info("Inserting attendance record into db from dao layer")
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("""
                INSERT INTO attendance (employee_id, check_in_datetime, check_out_datetime) 
                VALUES (:employeeId, :checkInDateTime, :checkOutDateTime)
            """)
                .bindBean(attendance)
                .execute()
        }
    }

    fun hasAlreadyCheckedIn(employeeId: UUID, checkInDateTime: LocalDateTime): Boolean {
        log.info("Checking if already checkedIn from dao layer")
        return jdbi.withHandle<Boolean, Exception> { handle ->
            val count = handle.createQuery("""
                SELECT COUNT(*) 
                FROM attendance 
                WHERE employee_id = :id AND check_in_datetime = :checkInDateTime
            """)
                .bind("id", employeeId)
                .bind("checkInDateTime", checkInDateTime)
                .mapTo<Int>()
                .one()
            count > 0
        }
    }

    fun hasOpenAttendance(employeeId: UUID): Boolean {
        log.info("Checking if there is an open attendance for employee $employeeId")
        return jdbi.withHandle<Boolean, Exception> { handle ->
            val result = handle.createQuery("""
            SELECT employee_id
            FROM attendance
            WHERE employee_id = :id AND check_out_datetime IS NULL
            LIMIT 1
        """)
                .bind("id", employeeId)
                .mapTo<UUID>()
                .findOne()
            result.isPresent
        }
    }


    fun updateCheckOut(employeeId: UUID, checkInDateTime: LocalDateTime, checkOutDateTime: LocalDateTime): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("""
                UPDATE attendance 
                SET check_out_datetime = :checkOutDateTime
                WHERE employee_id = :employeeId 
                AND check_in_datetime = :checkInDateTime 
                AND check_out_datetime IS NULL
            """)
                .bind("checkOutDateTime", checkOutDateTime)
                .bind("employeeId", employeeId)
                .bind("checkInDateTime", checkInDateTime)
                .execute()
        }
    }

    fun getAttendanceByEmployee(employeeId: UUID): List<Attendance> {
        return jdbi.withHandle<List<Attendance>, Exception> { handle ->
            handle.createQuery("""
                SELECT employee_id AS employeeId, check_in_datetime AS checkInDateTime, check_out_datetime AS checkOutDateTime
                FROM attendance
                WHERE employee_id = :employeeId
            """)
                .bind("employeeId", employeeId)
                .mapTo<Attendance>()
                .list()
        }
    }
    fun getAllAttendance(limit: Int = 20): List<Attendance> {
        return jdbi.withHandle<List<Attendance>, Exception> { handle ->
            handle.createQuery(
                """
            SELECT employee_id AS employeeId,
                   check_in_datetime AS checkInDateTime,
                   check_out_datetime AS checkOutDateTime
            FROM attendance
            ORDER BY check_in_datetime DESC
            LIMIT :limit
            """
            )
                .bind("limit", limit)
                .mapTo<Attendance>()
                .list()
        }
    }
    fun getWorkingHoursSummaryByDateRange(from: LocalDateTime, to: LocalDateTime): List<Map<String, Any>> {
        return jdbi.withHandle<List<Map<String, Any>>, Exception> { handle ->
            handle.createQuery("""
            SELECT  employee_id AS employeeId,
                    SUM(EXTRACT(EPOCH FROM (check_out_datetime - check_in_datetime)) / 3600) AS totalHours
            FROM attendance
            WHERE   check_in_datetime >= :fromDate 
                    AND check_in_datetime <= :toDate
                    AND check_out_datetime IS NOT NULL
            GROUP BY employee_id

        """)
                .bind("fromDate", from)
                .bind("toDate", to)
                .mapToMap()
                .list()
        }
    }


}
