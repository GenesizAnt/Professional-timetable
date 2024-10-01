package ru.genesizant.Professional.Timetable.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.enums.StatusPerson;
import ru.genesizant.Professional.Timetable.enums.StatusRegisteredVisitor;
import ru.genesizant.Professional.Timetable.model.*;
import ru.genesizant.Professional.Timetable.repositories.ReceptionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static ru.genesizant.Professional.Timetable.enums.DayOfWeekRus.getRusDayWeekShort;

@Service
@RequiredArgsConstructor
public class ReceptionService {

    private final ReceptionRepository receptionRepository;

    public void recordNewReception(VacantSeat vacantSeat, Person client, UnregisteredPerson unregisteredPerson, Person specialist, StatusPerson statusPerson, StatusRegisteredVisitor statusRegistered) {
        Reception reception = new Reception();
        reception.setDateVacant(vacantSeat.getDateVacant());
        reception.setDayOfWeek(getRusDayWeekShort(vacantSeat.getDateVacant().getDayOfWeek().name()));
        reception.setTimeVacant(vacantSeat.getTimeVacant());
        reception.setSpecIdReception(specialist);
        switch (statusRegistered) {
            case REGISTERED -> reception.setVisitorIdReception(client);
            case UNREGISTERED -> reception.setUnregisteredPerson(unregisteredPerson.getFullName());
        }
        reception.setPrepayment(false);
        reception.setPrepaymentVisitor(false);
        switch (statusPerson) {
            case VISITOR -> {
                reception.setConfirmedVisitor(true);
                reception.setConfirmedSpecialist(false);
            }
            case SPECIALIST -> {
                reception.setConfirmedVisitor(false);
                reception.setConfirmedSpecialist(true);
            }
        }
        receptionRepository.save(reception);
    }

    public Page<Reception> findAll(PageRequest pageable) {
        return receptionRepository.findAll(pageable);
    }

    public Page<Reception> getReceptionsPage(Person specialist, Pageable pageableAp) {
        return receptionRepository.findByDateVacantGreaterThanEqualAndSpecIdReception(LocalDate.now(), specialist, pageableAp);
    }

    public Reception findById(Long id) {
        return receptionRepository.findById(id).orElseThrow();
    }

    public void save(Reception reception) {
        receptionRepository.save(reception);
    }

    public Optional<Reception> findByVacantSeat(VacantSeat vacantSeat, Person specialist) {
        return receptionRepository.findByDateVacantAndTimeVacantAndSpecIdReception(vacantSeat.getDateVacant(), vacantSeat.getTimeVacant(), specialist);
    }
    public void removeByVacantSeat(VacantSeat vacantSeat, Person specialist) {
        receptionRepository.deleteByDateVacantAndTimeVacantAndSpecIdReception(vacantSeat.getDateVacant(), vacantSeat.getTimeVacant(), specialist);
    }

    // получить список приемов по конкретному клиенту после определенной даты с учетом были ли уведомления ранее
    public List<Reception> findVisitorAppointmentsAfterDateWithNotifications(Long id, LocalDate localDate, Boolean isNotifyOneDay, Boolean isNotifyThreeHours) {
        return receptionRepository.findVisitorAppointmentsAfterDateWithNotifications(
                id,
                localDate,
                isNotifyOneDay,
                isNotifyThreeHours);
    }
}
