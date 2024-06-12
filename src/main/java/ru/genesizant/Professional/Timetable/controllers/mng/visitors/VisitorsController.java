package ru.genesizant.Professional.Timetable.controllers.mng.visitors;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.AgreementAppointmentDTO;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.model.SpecialistsAndClient;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.SpecialistAppointmentsService;
import ru.genesizant.Professional.Timetable.services.SpecialistsAndClientService;
import ru.genesizant.Professional.Timetable.services.telegram.SendMessageService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static ru.genesizant.Professional.Timetable.enums.StatusPerson.VISITOR;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/visitors")
public class VisitorsController {

    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final SendMessageService sendMessageService;
    private final ObjectMapper objectMapper;
    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final String ENROLL_VIEW_REDIRECT = "redirect:/visitors/my_specialist_menu";

    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;
    private final SpecialistsAndClientService specialistsAndClientService;
    private final SpecialistAppointmentsService specialistAppointmentsService;

    @ModelAttribute
    public void getPayloadPage(Model model, HttpServletRequest request) {
        Optional<SpecialistsAndClient> assignedToSpecialist = specialistsAndClientService.findByVisitorListId((Long) request.getSession().getAttribute("id"));
        List<SpecialistAppointments> appointmentsList = List.of();
        List<String> nearestDates = List.of();
        if (assignedToSpecialist.isPresent()) {
            Map<LocalDate, Map<String, String>> schedule = datesAppointmentsService.getCalendarFreeScheduleById(assignedToSpecialist.get().getSpecialistList().getId());
            nearestDates = datesAppointmentsService.getFiveNearestDates(schedule, assignedToSpecialist.get().getVisitorList().getFullName());
            appointmentsList = specialistAppointmentsService.findAllAppointmentsBySpecialist(assignedToSpecialist.get().getSpecialistList().getId());
            model.addAttribute("nameClient", assignedToSpecialist.get().getVisitorList().getUsername());
            model.addAttribute("idSpecialist", assignedToSpecialist.get().getSpecialistList().getId());
        }
        List<AgreementAppointmentDTO> times = new ArrayList<>();
        List<AgreementAppointmentDTO> needAgree = new ArrayList<>();
        if (!appointmentsList.isEmpty()) {
            for (SpecialistAppointments appointments : appointmentsList) {
                AgreementAppointmentDTO appointmentDTO = new AgreementAppointmentDTO();
                appointmentDTO.setIdAppointment(appointments.getId());
                appointmentDTO.setDateAppointment(appointments.getVisitDate());
                appointmentDTO.setTimeAppointment(appointments.getAppointmentTime());
                if (!appointments.isPrepayment() && appointments.getVisitorAppointments().getId().equals(request.getSession().getAttribute("id")) && !appointments.isPrepaymentVisitor()) {
                    times.add(appointmentDTO);
                }
                if (appointments.isPrepaymentVisitor() && !appointments.isPrepayment()) {
                    needAgree.add(appointmentDTO);
                }
            }
        }
        model.addAttribute("visitDates", times);
        model.addAttribute("needAgree", needAgree);

        if (!nearestDates.isEmpty()) {
            for (int i = 0; i < nearestDates.size(); i++) {
                model.addAttribute("day" + (1 + i), nearestDates.get(i));
            }
        }
    }

    @ModelAttribute(name = "visitor")
    public Person getVisitor(HttpServletRequest request) {
//        if (jwtUtil.isValidJWTInRun(request)) {
            return personService.findById((Long) request.getSession().getAttribute("id")).orElseThrow();
//        } else {
//            log.error("Ошибка валидации JWT токена у пользователя - " + request.getSession().getAttribute("id"));
//            throw new JWTVerificationException("");
//        }
    }

    //ToDo в разделе Мой профиль - добавить окно с выбором специалистов за которыми закреплен клиент, чтобы клиент мог переключаться между ними (специалистами)

    //Отображение меню специалиста за которым закреплен клиент
    @GetMapping("/my_specialist_menu") //ToDo добавить в конфиг - доступ только для авторизированных пользователей
    public String getMySpecialistMenu(@ModelAttribute("visitor") Person visitor) {

//        if (jwtUtil.isValidJWTAndSession(request)) {
//            displayPage(model, request);
//            log.info("Клиент: " + visitor.getFullName() + ". Перешел на страницу меню по специалисту");
//        } else {
//            return ERROR_LOGIN;
//        }
        return "visitors/my_specialist_menu";
    }

    //Открывает отдельную страницу с отображением только календаря
    @GetMapping("/full_calendar") //ToDo Формально не отображает ВЕСЬ календарь только 20 дней
    public String getFullCalendar(@ModelAttribute("visitor") Person visitor, Model model, HttpServletRequest request) {
//        if (!jwtUtil.isValidJWTAndSession(request)) {
//            return ERROR_LOGIN;
//        }
        Optional<SpecialistsAndClient> assignedToSpecialist = specialistsAndClientService.findByVisitorListId(visitor.getId());
        Map<LocalDate, Map<String, String>> schedule;
        if (assignedToSpecialist.isPresent()) {
            schedule = datesAppointmentsService.getCalendarFreeScheduleById(assignedToSpecialist.get().getSpecialistList().getId());
            String fullName = assignedToSpecialist.get().getVisitorList().getFullName();
            List<String> allCalendar = new ArrayList<>();
            LocalDate now = LocalDate.now();
            List<LocalDate> nearestDates = schedule.keySet().stream()
                    .filter(date -> !date.isBefore(now)) // исключаем даты, предшествующие текущей дате
                    .sorted(Comparator.comparingLong(date -> ChronoUnit.DAYS.between(now, date))).toList();
            for (LocalDate nearestDate : nearestDates) {
                String[][] calendarForView = datesAppointmentsService.getCalendarForClient(fullName, nearestDate, schedule.get(nearestDate));
                try {
                    allCalendar.add(objectMapper.writeValueAsString(calendarForView));
                } catch (Exception e) {
                    log.error("Ошибка формирования JSON из календаря:" + Arrays.deepToString(calendarForView) + ". Текст сообщения - " + e.getMessage());
                }
            }
            model.addAttribute("idSpecialist", assignedToSpecialist.get().getSpecialistList().getId());
            model.addAttribute("nameClient", assignedToSpecialist.get().getVisitorList().getUsername());
            for (int i = 0; i < allCalendar.size(); i++) {
                model.addAttribute("day" + i, allCalendar.get(i));
            }
            log.info("Клиент: " + request.getSession().getAttribute("id") + ". Перешел на отдельную страницу с отображением только календаря - 20 дней");
        }
        return "visitors/full_calendar";
    }

    //Записаться через кнопку в таблице
    @PostMapping("/appointment_booking_table")
    public String setAppointmentBookingTable(@ModelAttribute("visitor") Person visitor, HttpServletRequest request,
                                             @RequestBody Map<String, String> applicationFromVisitor) {

//        if (!jwtUtil.isValidJWTAndSession(request)) {
//            return ERROR_LOGIN;
//        }

        String specialistId = applicationFromVisitor.get("specialistId");
        String meetingDate = applicationFromVisitor.get("meetingDate");
        String meetingTime = applicationFromVisitor.get("meetingTime");

        if (!specialistId.equals("") && !meetingDate.equals("") && !meetingTime.equals("")) {
            LocalDate date = LocalDate.parse(meetingDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            LocalTime time = LocalTime.parse(meetingTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime meeting = date.atTime(time);

            PersonFullName personFullNameRegistered =
                    modelMapper.map(personService.findById(visitor.getId()), PersonFullName.class);

            datesAppointmentsService.enrollVisitorNewAppointments(meeting, personFullNameRegistered, Long.valueOf(specialistId), VISITOR);
            sendMessageService.notifyEnrollNewAppointment(VISITOR, meeting, visitor.getId(), Long.valueOf(specialistId));
//            displayPage(model, request);
            log.info("Клиент: " + visitor.getFullName() + ". Записался через кнопку в таблице на: " + meeting);
        } else {
            return encodeError("Если Вы видите это сообщение, то произошла неизвестная ошибка");
        }
        return ENROLL_VIEW_REDIRECT;
    }

    //Записаться через кнопку
    //ToDo Посмотреть дублирующий код с предыдущ
    @PostMapping("/appointment_booking_form")
    public String setAppointmentBookingForm(@ModelAttribute("visitor") Person visitor, HttpServletRequest request,
                                            @RequestParam("selectedSpecialistId") String selectedSpecialistId,
                                            @RequestParam("meetingDataTime") LocalDateTime meeting) {

//        if (!jwtUtil.isValidJWTAndSession(request)) {
//            return ERROR_LOGIN;
//        }

        if (meeting != null) {
            PersonFullName personFullNameRegistered = modelMapper.map(personService.findById(visitor.getId()), PersonFullName.class);

            datesAppointmentsService.enrollVisitorNewAppointments(meeting, personFullNameRegistered, Long.valueOf(selectedSpecialistId), VISITOR);
            sendMessageService.notifyEnrollNewAppointment(VISITOR, meeting, visitor.getId(), Long.valueOf(selectedSpecialistId));
//            displayPage(model, request);
            log.info("Клиент: " + visitor.getFullName() + ". Записался через кнопку на странице на: " + meeting);
        } else {
            return encodeError("Для записи нужно выбрать ДАТУ и ВРЕМЯ приема");
        }

        return ENROLL_VIEW_REDIRECT;
    }

    // Отменить запись
    @PostMapping("/cancellingBookingVisitor")
    public String cancellingBookingVisitor(@ModelAttribute("visitor") Person visitor, HttpServletRequest request,
                                    @RequestParam("selectedSpecialistId") String selectedSpecialistId,
                                    @RequestParam("meetingCancel") LocalDateTime meetingCancel) {
//        if (!jwtUtil.isValidJWTAndSession(request)) {
//            return ERROR_LOGIN;
//        }
        if (meetingCancel != null) {
            datesAppointmentsService.cancellingBookingAppointments(meetingCancel, Long.valueOf(selectedSpecialistId));
            Optional<SpecialistAppointments> appointmentsCancel = specialistAppointmentsService.getAppointmentsSpecificDay(Long.valueOf(selectedSpecialistId), meetingCancel);
            appointmentsCancel.ifPresent(specialistAppointmentsService::removeAppointment);
            sendMessageService.notifyCancellation(VISITOR, meetingCancel, Long.valueOf(selectedSpecialistId));
//            displayPage(model, request);
            log.info("Клиент: " + visitor.getFullName() + ". Отменил запись на: " + meetingCancel);
        } else {
            return encodeError("Для отмены записи нужно выбрать КЛИЕНТА и ДАТУ отмены приема");
        }

        return ENROLL_VIEW_REDIRECT;
    }

//    @PostMapping("/cancelling_booking")
//    public String setCancellingBooking(Model model, HttpServletRequest request,
//                                       @RequestParam("selectedSpecialistId") String selectedSpecialistId,
//                                       @RequestParam("meetingCancel") LocalDateTime meetingCancel) {
//        if (!jwtUtil.isValidJWTAndSession(request)) {
//            return ERROR_LOGIN;
//        }
//
//
//        PersonFullName personFullNameRegistered =
//                modelMapper.map(personService.findById((Long) request.getSession().getAttribute("id")), PersonFullName.class);
//
//        if (datesAppointmentsService.isCheckAvailableCancellingBooking(meetingCancel, personFullNameRegistered.toString(), Long.valueOf(selectedSpecialistId))) {
//
//            datesAppointmentsService.cancellingBookingAppointments(meetingCancel, Long.valueOf(selectedSpecialistId));
//            model.addAttribute("selectedSpecialistId", selectedSpecialistId); //ToDo нормально ли добавлять атрибут каждый раз???
//
//        } else {
//            model.addAttribute("notAvailable", "НЕЛЬЗЯ ОТМЕНИТЬ ЧУЖУЮ ЗАПИСЬ!");
//        }
//
//
//        displayPage(model, request);
//
//        return ENROLL_VIEW_REDIRECT;
//
////        return "visitors/specialist_choose";
//    }

//    private void displayPage(Model model, HttpServletRequest request) {
//        Optional<SpecialistsAndClient> assignedToSpecialist = specialistsAndClientService.findByVisitorListId((Long) request.getSession().getAttribute("id"));
//        List<SpecialistAppointments> appointmentsList = List.of();
//        List<String> nearestDates = List.of();
//        if (assignedToSpecialist.isPresent()) {
//            Map<LocalDate, Map<String, String>> schedule = datesAppointmentsService.getCalendarFreeScheduleById(assignedToSpecialist.get().getSpecialistList().getId());
//            nearestDates = datesAppointmentsService.getFiveNearestDates(schedule, assignedToSpecialist.get().getVisitorList().getFullName());
//            appointmentsList = specialistAppointmentsService.findAllAppointmentsBySpecialist(assignedToSpecialist.get().getSpecialistList().getId());
//            model.addAttribute("nameClient", assignedToSpecialist.get().getVisitorList().getUsername());
//            model.addAttribute("idSpecialist", assignedToSpecialist.get().getSpecialistList().getId());
//        }
//        List<AgreementAppointmentDTO> times = new ArrayList<>();
//        List<AgreementAppointmentDTO> needAgree = new ArrayList<>();
//        if (!appointmentsList.isEmpty()) {
//            for (SpecialistAppointments appointments : appointmentsList) {
//                AgreementAppointmentDTO appointmentDTO = new AgreementAppointmentDTO();
//                appointmentDTO.setIdAppointment(appointments.getId());
//                appointmentDTO.setDateAppointment(appointments.getVisitDate());
//                appointmentDTO.setTimeAppointment(appointments.getAppointmentTime());
//                if (!appointments.isPrepayment() && appointments.getVisitorAppointments().getId().equals(request.getSession().getAttribute("id")) && !appointments.isPrepaymentVisitor()) {
//                    times.add(appointmentDTO);
//                }
//                if (appointments.isPrepaymentVisitor() && !appointments.isPrepayment()) {
//                    needAgree.add(appointmentDTO);
//                }
//            }
//        }
//        model.addAttribute("visitDates", times);
//        model.addAttribute("needAgree", needAgree);
//
//        if (!nearestDates.isEmpty()) {
//            for (int i = 0; i < nearestDates.size(); i++) {
//                model.addAttribute("day" + (1 + i), nearestDates.get(i));
////                model.addAttribute("day" + 2, nearestDates.get(i));
////                model.addAttribute("day3", nearestDates.get(i));
////                model.addAttribute("day4", nearestDates.get(i));
////                model.addAttribute("day5", nearestDates.get(i);
//            }
//        }
//    }

    private String encodeError(String error) {
        String ERROR_VALIDATE_FORM = "redirect:/visitors/my_specialist_menu?error=";
        return ERROR_VALIDATE_FORM + URLEncoder.encode(error, StandardCharsets.UTF_8);
    }
}


//отображение списка специалистов на выбор для клиента
//    @GetMapping("/start_menu_visitor") //ToDo добавить в конфиг - доступ только для авторизированных пользователей
//    public String getStartMenu(Model model, HttpServletRequest request) {
//
//        //ToDo добавить навигационный бар https://getbootstrap.com/docs/5.0/components/navbar/
//
//        if (jwtUtil.isValidJWTAndSession(request)) {
//
////            нужно добавить в html страницу отображение карточки, при этом количество карточек может меняться <div class="card" style="width: 18rem;">
////  <img src="..." class="card-img-top" alt="...">
////  <div class="card-body">
////    <h5 class="card-title">Card title</h5>
////    <p class="card-text">Some quick example text to build on the card title and make up the bulk of the card's content.</p>
////                    <a href="#" class="btn btn-primary">Go somewhere</a>
////  </div>
////</div> как сделать такую html страницу с разным количеством отображения карточек
//
//
//
//
////            String jwtToken = (String) request.getSession().getAttribute("jwtToken");
////            String email = jwtUtil.validateTokenAndRetrieveClaim(jwtToken);
////            Optional<Person> visitor = personRepository.findByEmail(email);
////            model.addAttribute("name", visitor.get().getUsername());
//
//            List<Person> specialists = personService.getPersonByRoleList("ROLE_ADMIN");
//            model.addAttribute("name", request.getSession().getAttribute("name"));
//            model.addAttribute("specialists", specialists);
//
//        } else {
//            model.addAttribute("error", "Упс! Пора перелогиниться!");
//            return "redirect:/auth/login?error"; //ToDo добавить считывание ошибки и правильного отображения сейчас отображается "Неправильные имя или пароль"
//        }
//
//        return "visitors/start_menu_visitor";
//
////        HttpSession session = request.getSession(false); // Получение текущей сессии, если сессия не существует, вернет null
////
////        if (session != null) {
////            String jwtToken = (String) session.getAttribute("jwtToken");
////            if (jwtToken != null) {
////                try {
////                    String email = jwtUtil.validateTokenAndRetrieveClaim(jwtToken);
////                    Optional<Person> visitor = personRepository.findByEmail(email);
////                    model.addAttribute("name", visitor.get().getUsername());
////
////                } catch (Exception e) {
////                    model.addAttribute("error", "Упс! Пора перелогиниться!");
////                    return "redirect:/auth/login?error"; //ToDo добавить считывание ошибки и правильного отображения сейчас отображается "Неправильные имя или пароль"
////                }
////            }
////        }
////        return "visitors/start_menu_visitor";
//    }
