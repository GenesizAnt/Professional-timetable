package ru.genesizant.Professional.Timetable.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.enums.StatusPerson;
import ru.genesizant.Professional.Timetable.enums.StatusRegisteredVisitor;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.Reception;
import ru.genesizant.Professional.Timetable.model.UnregisteredPerson;
import ru.genesizant.Professional.Timetable.model.VacantSeat;
import ru.genesizant.Professional.Timetable.repositories.ReceptionRepository;

import java.time.LocalDate;

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
}
