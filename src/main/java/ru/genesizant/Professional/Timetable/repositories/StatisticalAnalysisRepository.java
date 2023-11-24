package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.StatisticalAnalysis;

@Repository
public interface StatisticalAnalysisRepository extends JpaRepository<StatisticalAnalysis, Long> {
}
