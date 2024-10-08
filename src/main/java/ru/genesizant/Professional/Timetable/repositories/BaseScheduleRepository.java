package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.BaseSchedule;
import ru.genesizant.Professional.Timetable.model.Person;

import java.util.Optional;

@Repository
public interface BaseScheduleRepository extends JpaRepository<BaseSchedule, Long> {

    Optional<BaseSchedule> findBySpecialistBaseSchedule(Person spec);
}
