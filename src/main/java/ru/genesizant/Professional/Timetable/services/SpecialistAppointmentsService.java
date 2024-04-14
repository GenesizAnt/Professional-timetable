package ru.genesizant.Professional.Timetable.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.repositories.SpecialistAppointmentsRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SpecialistAppointmentsService {
    SpecialistAppointmentsRepository specialistAppointmentsRepository;

    @Autowired
    public SpecialistAppointmentsService(SpecialistAppointmentsRepository specialistAppointmentsRepository) {
        this.specialistAppointmentsRepository = specialistAppointmentsRepository;
    }

    public List<SpecialistAppointments> findAllAppointments() {
        return specialistAppointmentsRepository.findAll();
    }

    public void agreementPrePay(Long idAgreement, Boolean isPrePay) {
        Optional<SpecialistAppointments> appointments = specialistAppointmentsRepository.findById(idAgreement);
        appointments.get().setPrepayment(isPrePay);
        specialistAppointmentsRepository.save(appointments.get());
    }
}
