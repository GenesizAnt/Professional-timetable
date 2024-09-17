package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.Reception;
import ru.genesizant.Professional.Timetable.model.VacantSeat;

import java.time.LocalDate;

@Repository
public interface ReceptionRepository extends JpaRepository<Reception, Long> {
    Page<Reception> findBySpecIdReception(Person specialist, Pageable pageableAp);

    Page<Reception> findByDateVacantGreaterThanEqualAndSpecIdReception(LocalDate currentDate, Person specialist, Pageable pageable);
}
