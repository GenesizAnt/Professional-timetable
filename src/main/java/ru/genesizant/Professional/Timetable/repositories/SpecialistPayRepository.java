package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.genesizant.Professional.Timetable.model.SpecialistPay;

import java.util.Optional;

public interface SpecialistPayRepository extends JpaRepository<SpecialistPay, Long> {
    Optional<SpecialistPay> findBySpecialistPay_Id(Long id);
}
