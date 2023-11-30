package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.DatesAppointments;
import ru.genesizant.Professional.Timetable.model.StatisticalAnalysis;

import java.util.List;

@Repository
public interface DatesAppointmentsRepository extends JpaRepository<DatesAppointments, Long> {
    List<DatesAppointments> findAllBySpecialistDateAppointmentsIdOrderById(long id);
}
