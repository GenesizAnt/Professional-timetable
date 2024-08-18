package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.VacantSeat;

import java.util.List;

@Repository
public interface VacantSeatRepository extends JpaRepository<VacantSeat, Long> {
    List<VacantSeat> findBySpecId(Person specialist);

    Page<VacantSeat> findBySpecId(Person specialist, Pageable pageable);
}
