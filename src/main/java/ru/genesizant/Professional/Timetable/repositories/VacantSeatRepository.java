package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.VacantSeat;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacantSeatRepository extends JpaRepository<VacantSeat, Long> {
    List<VacantSeat> findBySpecId(Person specialist);

    Page<VacantSeat> findBySpecId(Person specialist, Pageable pageable);

    Page<VacantSeat> findByDateVacantGreaterThanEqualAndSpecId(LocalDate currentDate, Person specialist, Pageable pageable);

    @Transactional
    @Modifying
    @Query("DELETE FROM VacantSeat v WHERE v.dateVacant = :date AND v.specId.id = :specialistId")
    void deleteByDateAndSpecId(LocalDate date, Long specialistId);

    Page<VacantSeat> findByIdVisitor(Long idVisitor, Pageable pageable);

    // Метод для поиска всех записей от текущей даты, где idVisitor либо null, либо переданное значение
//    Page<VacantSeat> findByDateVacantGreaterThanEqualAndIdVisitorIsNullOrIdVisitor(
//            LocalDate currentDate, Long idVisitor, Pageable pageable);

    @Query("SELECT v FROM VacantSeat v WHERE v.dateVacant >= :currentDate AND (v.idVisitor IS NULL OR v.idVisitor = :idVisitor)")
    Page<VacantSeat> findVacantSeatsByDateAndIdVisitor(
            @Param("currentDate") LocalDate currentDate,
            @Param("idVisitor") Long idVisitor,
            Pageable pageable);
}
