package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SpecialistAppointmentsRepository extends JpaRepository<SpecialistAppointments, Long> {

    // Все приемы которые есть у пары Специалист+Клиент
    List<SpecialistAppointments> findByVisitorAppointmentsIdAndSpecialistAppointmentsIdOrderById(Long visitorId, Long specialistId);

    @Query("SELECT sa " +
            "FROM SpecialistAppointments sa " +
            "WHERE sa.visitorAppointments.id = :visitorId " +
            "AND sa.visitDate >= :currentDate")
    List<SpecialistAppointments> findVisitorAppointmentsAfterDate(@Param("visitorId") Long visitorId,
                                                                  @Param("currentDate") LocalDate currentDate);

    @Query("SELECT sa " +
            "FROM SpecialistAppointments sa " +
            "WHERE sa.visitorAppointments.id = :visitorId " +
            "AND sa.visitDate >= :currentDate " +
            "AND sa.notify24hours = :notify24hours " +
            "AND sa.notify3hours = :notify3hours")
    List<SpecialistAppointments> findVisitorAppointmentsAfterDateWithNotifications(
            @Param("visitorId") Long visitorId,
            @Param("currentDate") LocalDate currentDate,
            @Param("notify24hours") boolean notify24hours,
            @Param("notify3hours") boolean notify3hours);
}
