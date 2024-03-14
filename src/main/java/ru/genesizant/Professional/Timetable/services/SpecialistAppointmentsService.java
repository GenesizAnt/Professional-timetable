package ru.genesizant.Professional.Timetable.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.repositories.SpecialistAppointmentsRepository;

@Service
public class SpecialistAppointmentsService {
    SpecialistAppointmentsRepository specialistAppointmentsRepository;

    @Autowired
    public SpecialistAppointmentsService(SpecialistAppointmentsRepository specialistAppointmentsRepository) {
        this.specialistAppointmentsRepository = specialistAppointmentsRepository;
    }
}
