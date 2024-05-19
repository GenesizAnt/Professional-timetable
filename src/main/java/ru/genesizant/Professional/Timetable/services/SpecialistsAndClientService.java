package ru.genesizant.Professional.Timetable.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistsAndClient;
import ru.genesizant.Professional.Timetable.repositories.SpecialistsAndClientRepository;

import java.util.*;

@Service
public class SpecialistsAndClientService {

    private final SpecialistsAndClientRepository specialistsAndClientRepository;
    private final ModelMapper modelMapper;
    private final PersonService personService;

    @Autowired
    public SpecialistsAndClientService(SpecialistsAndClientRepository specialistsAndClientRepository, ModelMapper modelMapper, PersonService personService) {
        this.specialistsAndClientRepository = specialistsAndClientRepository;
        this.modelMapper = modelMapper;
        this.personService = personService;
    }

    //Список закрепленных за специалистом клиентов
    public List<PersonFullName> getClientsBySpecialistList(long id) {
        List<PersonFullName> clientsBySpecialist = new ArrayList<>();
        List<SpecialistsAndClient> visitorList = specialistsAndClientRepository.findAllBySpecialistListIdOrderById(id);
        for (SpecialistsAndClient person : visitorList) {
            clientsBySpecialist.add(
                    modelMapper.map(personService.findById(person.getVisitorList().getId()), PersonFullName.class)
            );
        }
        return clientsBySpecialist;
    }

    public void appointSpecialist(Long visitorId, Long specialistId) {
        SpecialistsAndClient newAppointSpecialist = new SpecialistsAndClient();
        newAppointSpecialist.setVisitorList(personService.findById(visitorId).get());
        newAppointSpecialist.setSpecialistList(personService.findById(specialistId).get());
        specialistsAndClientRepository.save(newAppointSpecialist);
    }

    // Найти пару Спец-Клиент по ИД клиента
    public Optional<SpecialistsAndClient> findByVisitorListId(Long idVisitor) {
        return specialistsAndClientRepository.findByVisitorListId(idVisitor);
    }

    public void newPair(Person newPerson, String specialistPhone) {
        Optional<Person> specialistByPhoneNumber = personService.findSpecialistByPhoneNumber(specialistPhone);
//        Map<String, String> fio = getFIO(specialistPhone);
//        Optional<Person> specialist = personService.findByFullName(
//                fio.get("username"),
//                fio.get("surname"),
//                fio.get("patronymic"));
//        Optional<Person> visitor = personService.findByFullName(newPerson.getUsername(), newPerson.getSurname(), newPerson.getPatronymic());

        SpecialistsAndClient newAppointSpecialist = new SpecialistsAndClient();
        newAppointSpecialist.setVisitorList(newPerson);
        newAppointSpecialist.setSpecialistList(specialistByPhoneNumber.get());
        specialistsAndClientRepository.save(newAppointSpecialist);
    }

    private Map<String, String> getFIO(String specialistName) {
        Map<String, String> fio = new HashMap<>();
        String[] fioArray = specialistName.split(" ");
        if (fioArray.length == 3) {
            fio.put("surname", fioArray[0]); // Фамилия
            fio.put("username", fioArray[1]); // Имя
            fio.put("patronymic", fioArray[2]); // Отчество
        }
        return fio;
    }
}
