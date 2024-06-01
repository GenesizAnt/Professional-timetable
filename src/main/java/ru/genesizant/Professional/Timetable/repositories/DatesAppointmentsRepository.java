package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.genesizant.Professional.Timetable.model.DatesAppointments;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DatesAppointmentsRepository extends JpaRepository<DatesAppointments, Long> {

    //Получить расписание (даты и вермя) по ИД спеца
    List<DatesAppointments> findAllBySpecialistDateAppointmentsIdOrderById(long id);

    //Получить Конкретную дату и конкретного Спеца
    Optional<DatesAppointments> findByVisitDateAndSpecialistDateAppointmentsIdOrderById(LocalDate date, long specialistId);

    //Удалить полный День из доступных для выбора дат
    @Transactional
    void deleteByVisitDate(LocalDate date);

    //Удалить Диапазон Дней из доступных для выбора дат
    @Transactional
    void deleteByVisitDateBetween(LocalDate startDateRange, LocalDate endDateRange);

    //Удалить все Даты из календаря до указанной даты
    @Transactional
    void deleteByVisitDateBefore(LocalDate date);

    //Получить доступное время на конкретный день
//    Optional<DatesAppointments> findByVisitDate(LocalDate date);

    //Найти все даты для приемов у специалиста
    List<DatesAppointments> findAllVisitDatesBySpecialistDateAppointmentsId(Long id);
}
