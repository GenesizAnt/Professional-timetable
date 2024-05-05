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
    //ToDo возможно переименовать????
    //ToDo не оптимальный запрос! Каждый раз тянутся ВСЕ приемы, а нужно проверять еще не состоявшиеся
    // Все приемы которые есть у пары Специалист+Клиент
    public List<SpecialistAppointments> findAppointmentsByVisitor(Long idVisitor, Long idSpecialist) {
        List<SpecialistAppointments> appointments = specialistAppointmentsRepository.findByVisitorAppointmentsIdAndSpecialistAppointmentsIdOrderById(idVisitor, idSpecialist);
        if (appointments.isEmpty()) {
            return List.of();
        }
        return appointments;
//        return specialistAppointmentsRepository.findByVisitorAppointmentsIdAndSpecialistAppointmentsIdOrderById(idVisitor, idSpecialist);
    }

    //Создать встречу
    public void createNewAppointments(LocalDate toLocalDate, LocalTime toLocalTime, Person idSpecialist, Person idVisitor, Boolean isPrePaySpecialist, Boolean isPrePayVisitor) {
        SpecialistAppointments specialistAppointments = new SpecialistAppointments();
        specialistAppointments.setVisitDate(toLocalDate);
        specialistAppointments.setAppointmentTime(toLocalTime);
        specialistAppointments.setSpecialistAppointments(idSpecialist);
        specialistAppointments.setVisitorAppointments(idVisitor);
        specialistAppointments.setPrepayment(isPrePaySpecialist);
        specialistAppointments.setPrepaymentVisitor(isPrePayVisitor);
        if (toLocalDate.equals(LocalDate.now())) {
            specialistAppointments.setNotify24hours(true);
            specialistAppointments.setNotify3hours(false);
        } else {
            specialistAppointments.setNotify24hours(false);
            specialistAppointments.setNotify3hours(false);
        }
        specialistAppointmentsRepository.save(specialistAppointments);
    }

    // Подтверждение или Отмена оплаты по конкретному клиенту - кнопка специалиста
    public void agreementPrePay(Long idAgreement, Boolean isPrePay) {
        Optional<SpecialistAppointments> appointments = specialistAppointmentsRepository.findById(idAgreement);
        appointments.get().setPrepayment(isPrePay);
        specialistAppointmentsRepository.save(appointments.get());
    }

    // Подтверждение или Отмена оплаты - кнопка клиента
    public void agreementVisitorPrePay(Long idAgreement, Boolean isPrePay) {
        Optional<SpecialistAppointments> appointments = specialistAppointmentsRepository.findById(idAgreement);
        appointments.get().setPrepaymentVisitor(isPrePay);
        specialistAppointmentsRepository.save(appointments.get());
    }

    // получить список приемов по конкретному клиенту после определенной даты
    public List<SpecialistAppointments> findVisitorAppointmentsAfterDate(Long visitorId, LocalDate currentDate) {
        return specialistAppointmentsRepository.findVisitorAppointmentsAfterDate(visitorId, currentDate);
    }

    // получить список приемов по конкретному клиенту после определенной даты с учетом были ли уведомления ранее
    public List<SpecialistAppointments> findVisitorAppointmentsAfterDateWithNotifications(Long visitorId,
                                                                                          LocalDate currentDate,
                                                                                          boolean notify24hours,
                                                                                          boolean notify3hours) {
        return specialistAppointmentsRepository.findVisitorAppointmentsAfterDateWithNotifications(
                visitorId,
                currentDate,
                notify24hours,
                notify3hours);
    }

    public SpecialistAppointments findById(Long id) {
        return specialistAppointmentsRepository.findById(id).orElse(null);
    }

    public void save(SpecialistAppointments specialistAppointments) {
        specialistAppointmentsRepository.save(specialistAppointments);
    }

    public boolean isNeedNotify(LocalDate visitDate) {
        return specialistAppointmentsRepository.
                existsByVisitDateGreaterThanEqualAndNotify24hoursIsFalseAndNotify3hoursIsFalse(visitDate);
    }
}
