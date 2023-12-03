package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.genesizant.Professional.Timetable.model.DatesAppointments;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DatesAppointmentsRepository extends JpaRepository<DatesAppointments, Long> {

    List<DatesAppointments> findAllBySpecialistDateAppointmentsIdOrderById(long id);

    Optional<DatesAppointments> findByVisitDateAndSpecialistDateAppointmentsIdOrderById(LocalDate date, long id);

    @Transactional
    void deleteByVisitDate(LocalDate date);

    //    List<DatesAppointments> findAllBySpecialistDateAppointmentsOrderById(long id);
}
