package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.SpecialistsAndClient;
import ru.genesizant.Professional.Timetable.model.UnregisteredPerson;

@Repository
public interface SpecialistsAndClientRepository extends JpaRepository<SpecialistsAndClient, Long> {
}
