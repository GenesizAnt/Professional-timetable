package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;

import java.util.List;

@Repository
public interface SpecialistAppointmentsRepository extends JpaRepository<SpecialistAppointments, Long> {

    // Все приемы которые есть у пары Специалист+Клиент
    List<SpecialistAppointments> findByVisitorAppointmentsIdAndSpecialistAppointmentsIdOrderById(Long visitorId, Long specialistId);
}
