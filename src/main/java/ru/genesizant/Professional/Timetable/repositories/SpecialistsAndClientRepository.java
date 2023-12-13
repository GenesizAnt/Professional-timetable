package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistsAndClient;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialistsAndClientRepository extends JpaRepository<SpecialistsAndClient, Long> {

    List<SpecialistsAndClient> findAllBySpecialistListIdOrderById(long id);

    Optional<SpecialistsAndClient> findBySpecialistListAndAndVisitorList(Person idSpecialist, Person idVisitor);

}
