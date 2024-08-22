package ru.genesizant.Professional.Timetable.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.enums.StatusPerson;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.Reception;
import ru.genesizant.Professional.Timetable.model.VacantSeat;
import ru.genesizant.Professional.Timetable.repositories.ReceptionRepository;

@Service
@RequiredArgsConstructor
public class ReceptionService {

    private final ReceptionRepository receptionRepository;

    public void recordNewReception(VacantSeat vacantSeat, Person client, Person specialist, StatusPerson statusPerson) {
        Reception reception = new Reception();
        reception.setDate_vacant(vacantSeat.getDateVacant());
        reception.setTime_vacant(vacantSeat.getTimeVacant());
        reception.setSpecIdReception(specialist);
        reception.setVisitorIdReception(client);
        reception.setPrepayment(false);
        reception.setPrepaymentVisitor(false);
//                switch (statusPerson) {
//                    case VISITOR -> objectNode.put(NEED_CONFIRMATION.getStatus(), personFullName.toString());
//                    case SPECIALIST -> objectNode.put(CONFIRMED.getStatus(), personFullName.toString());
//                }
        receptionRepository.save(reception);
    }
}
