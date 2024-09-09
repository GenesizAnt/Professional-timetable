package ru.genesizant.Professional.Timetable.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.model.BaseSchedule;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.repositories.BaseScheduleRepository;

@Service
@RequiredArgsConstructor
public class BaseScheduleService {

    private final BaseScheduleRepository baseScheduleRepository;

//    public createNewBaseSchedule()

    public BaseSchedule getBaseScheduleSpecialist(Person spec) {
        return baseScheduleRepository.findBySpecialistBaseSchedule(spec);
    }
}
