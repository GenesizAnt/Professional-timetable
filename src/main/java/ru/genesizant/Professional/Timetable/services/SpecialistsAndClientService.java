package ru.genesizant.Professional.Timetable.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistsAndClient;
import ru.genesizant.Professional.Timetable.repositories.SpecialistsAndClientRepository;

import java.util.ArrayList;
import java.util.List;

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

    public List<PersonFullName> getClientsBySpecialistList(long id) {
        List<PersonFullName> clientsBySpecialist = new ArrayList<>();
        List<SpecialistsAndClient> visitorList = specialistsAndClientRepository.findAllBySpecialistListIdOrderById(id);
        for (SpecialistsAndClient person : visitorList) {
            clientsBySpecialist.add(
                    modelMapper.map(personService.findById(person.getVisitorList().getId()), PersonFullName.class)
            );

//            Person person = this.modelMapper.map(personDTO, Person.class);
//            PersonFullName personFullName = modelMapper.map(personService.findById(person.getVisitorList().getId()), PersonFullName.class);
        }
        return clientsBySpecialist;
    }

    public void appointSpecialist(Long visitorId, Long specialistId) {
        SpecialistsAndClient newAppointSpecialist = new SpecialistsAndClient();
        newAppointSpecialist.setVisitorList(personService.findById(visitorId).get());
        newAppointSpecialist.setSpecialistList(personService.findById(specialistId).get());
        specialistsAndClientRepository.save(newAppointSpecialist);
    }
}
