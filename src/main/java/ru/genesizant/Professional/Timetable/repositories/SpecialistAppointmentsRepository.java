package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;

import java.util.List;

@Repository
public interface SpecialistAppointmentsRepository extends JpaRepository<SpecialistAppointments, Long> {
//    List<SpecialistAppointments> findByVisitorAppointmentsIdAndSpecialistAppointmentsId(Long visitorId, Long specialistId);
//    List<SpecialistAppointments> findBySpecialist_appointmentsIdOrderByIdAndVisitor_appointmentsIdOrderById(Long visitorId, Long specialistId);

    List<SpecialistAppointments> findByVisitorAppointmentsIdAndSpecialistAppointmentsIdOrderById(Long visitorId, Long specialistId);
}
