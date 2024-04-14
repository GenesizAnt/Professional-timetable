package ru.genesizant.Professional.Timetable.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.repositories.SpecialistAppointmentsRepository;

import java.time.LocalDate;
import java.time.LocalTime;
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

    public void createNewAppointments(LocalDate toLocalDate, LocalTime toLocalTime, Person idSpecialist, Person idVisitor, Boolean isPrePay) {
        SpecialistAppointments specialistAppointments = new SpecialistAppointments();
        specialistAppointments.setVisitDate(toLocalDate);
        specialistAppointments.setAppointmentTime(toLocalTime);
        specialistAppointments.setSpecialist_appointments(idSpecialist);
        specialistAppointments.setVisitor_appointments(idVisitor);
        specialistAppointments.setPrepayment(isPrePay);
        specialistAppointmentsRepository.save(specialistAppointments);
    }

    public void agreementPrePay(Long idAgreement, Boolean isPrePay) {
        Optional<SpecialistAppointments> appointments = specialistAppointmentsRepository.findById(idAgreement);
        appointments.get().setPrepayment(isPrePay);
        specialistAppointmentsRepository.save(appointments.get());
    }
}
