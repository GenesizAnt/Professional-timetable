package ru.genesizant.Professional.Timetable.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.model.BaseSchedule;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.repositories.BaseScheduleRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BaseScheduleService {

    private final BaseScheduleRepository baseScheduleRepository;

//    public createNewBaseSchedule()

    public Optional<BaseSchedule> getBaseScheduleSpecialist(Person spec) {
        return baseScheduleRepository.findBySpecialistBaseSchedule(spec);
    }

    public void saveBaseSchedule(BaseSchedule baseSchedule) {
        baseScheduleRepository.save(baseSchedule);
    }

    public boolean isScheduleExist(Person specialist) {
        Optional<BaseSchedule> schedule = baseScheduleRepository.findBySpecialistBaseSchedule(specialist);
        return schedule.isPresent();
    }
}
