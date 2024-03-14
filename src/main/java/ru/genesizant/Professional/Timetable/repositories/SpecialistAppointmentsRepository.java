package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;

@Repository
public interface SpecialistAppointmentsRepository extends JpaRepository<SpecialistAppointments, Long> {

}
