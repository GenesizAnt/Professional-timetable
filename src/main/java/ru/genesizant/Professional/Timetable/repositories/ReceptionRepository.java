package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.Reception;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.model.VacantSeat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceptionRepository extends JpaRepository<Reception, Long> {
    Page<Reception> findBySpecIdReception(Person specialist, Pageable pageableAp);

    Page<Reception> findByDateVacantGreaterThanEqualAndSpecIdReception(LocalDate currentDate, Person specialist, Pageable pageable);
    Optional<Reception> findByDateVacantAndTimeVacantAndSpecIdReception(LocalDate localDate, LocalTime localTime, Person specialist);

    @Modifying
    @Transactional
    @Query("DELETE FROM Reception r WHERE r.dateVacant = :dateVacant AND r.timeVacant = :timeVacant AND r.specIdReception = :specialist")
    void deleteByDateVacantAndTimeVacantAndSpecIdReception(@Param("dateVacant") LocalDate dateVacant,
                                                           @Param("timeVacant") LocalTime timeVacant,
                                                           @Param("specialist") Person specialist);

    @Query("SELECT sa " +
            "FROM Reception sa " +
            "WHERE sa.visitorIdReception.id = :id " +
            "AND sa.dateVacant >= :localDate " +
            "AND sa.notify24hours = :isNotifyOneDay " +
            "AND sa.notify3hours = :isNotifyThreeHours")
    List<Reception> findVisitorAppointmentsAfterDateWithNotifications(@Param("id") Long id,
                                                                       @Param("localDate") LocalDate localDate,
                                                                       @Param("isNotifyOneDay") boolean isNotifyOneDay,
                                                                       @Param("isNotifyThreeHours") boolean isNotifyThreeHours);

    List<Reception> findBySpecIdReceptionAndAndVisitorIdReceptionAndPrepayment(Person specialist, Person visitor, boolean prepayment);
}
