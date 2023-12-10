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

    //Получить расписание (даты и вермя) по ИД спеца
    List<DatesAppointments> findAllBySpecialistDateAppointmentsIdOrderById(long id);

    //Получить Конкретную дату и конкретного Спеца
    Optional<DatesAppointments> findByVisitDateAndSpecialistDateAppointmentsIdOrderById(LocalDate date, long id);

    @Transactional
    void deleteByVisitDate(LocalDate date);

    @Transactional
    void deleteByVisitDateBetween(LocalDate startDateRange, LocalDate endDateRange);

    @Transactional
    void deleteByVisitDateBefore(LocalDate date);

    Optional<DatesAppointments> findByVisitDate(LocalDate date);

}
