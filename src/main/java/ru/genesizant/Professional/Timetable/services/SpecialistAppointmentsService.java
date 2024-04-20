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

    //ToDo возможно сделать через оптионал????
    public List<SpecialistAppointments> findAllAppointments() {
        List<SpecialistAppointments> all = specialistAppointmentsRepository.findAll();
        if (all.isEmpty()) {
            return List.of();
        }
        return all;
    }

    //ToDo возможно сделать через оптионал????
    public List<SpecialistAppointments> findAppointmentsByVisitor(Long idVisitor, Long idSpecialist) {
        List<SpecialistAppointments> appointments = specialistAppointmentsRepository.findByVisitorAppointmentsIdAndSpecialistAppointmentsIdOrderById(idVisitor, idSpecialist);
        if (appointments.isEmpty()) {
            return List.of();
        }
        return appointments;
//        return specialistAppointmentsRepository.findByVisitorAppointmentsIdAndSpecialistAppointmentsIdOrderById(idVisitor, idSpecialist);
    }

    public void createNewAppointments(LocalDate toLocalDate, LocalTime toLocalTime, Person idSpecialist, Person idVisitor, Boolean isPrePaySpecialist, Boolean isPrePayVisitor) {
        SpecialistAppointments specialistAppointments = new SpecialistAppointments();
        specialistAppointments.setVisitDate(toLocalDate);
        specialistAppointments.setAppointmentTime(toLocalTime);
        specialistAppointments.setSpecialistAppointments(idSpecialist);
        specialistAppointments.setVisitorAppointments(idVisitor);
        specialistAppointments.setPrepayment(isPrePaySpecialist);
        specialistAppointments.setPrepaymentVisitor(isPrePayVisitor);
        specialistAppointmentsRepository.save(specialistAppointments);
    }

    public void agreementPrePay(Long idAgreement, Boolean isPrePay) {
        Optional<SpecialistAppointments> appointments = specialistAppointmentsRepository.findById(idAgreement);
        appointments.get().setPrepayment(isPrePay);
        specialistAppointmentsRepository.save(appointments.get());
    }

    public void agreementVisitorPrePay(Long idAgreement, Boolean isPrePay) {
        Optional<SpecialistAppointments> appointments = specialistAppointmentsRepository.findById(idAgreement);
        appointments.get().setPrepaymentVisitor(isPrePay);
        specialistAppointmentsRepository.save(appointments.get());
    }
}
