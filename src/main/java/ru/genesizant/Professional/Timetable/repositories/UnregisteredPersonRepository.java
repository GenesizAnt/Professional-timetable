package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.UnregisteredPerson;

import java.util.List;

@Repository
public interface UnregisteredPersonRepository extends JpaRepository<UnregisteredPerson, Long> {

    List<UnregisteredPerson> findAllBySpecialistUnregisteredPersonIdOrderById(Long id);
}
