package ru.genesizant.Professional.Timetable.controllers.visitors;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.AgreementAppointmentDTO;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.model.SpecialistAppointments;
import ru.genesizant.Professional.Timetable.model.SpecialistsAndClient;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.SpecialistAppointmentsService;
import ru.genesizant.Professional.Timetable.services.SpecialistsAndClientService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static ru.genesizant.Professional.Timetable.enums.StatusPerson.VISITOR;

@Controller
@RequestMapping("/visitors")
public class VisitorsController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;
    private final ModelMapper modelMapper;
    private final SpecialistsAndClientService specialistsAndClientService;
    private final SpecialistAppointmentsService specialistAppointmentsService;
    private final ObjectMapper objectMapper;
    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final String ERROR_VALIDATE_FORM = "redirect:/visitors/my_specialist_menu?error=";
    private final String ENROLL_VIEW_REDIRECT = "redirect:/visitors/my_specialist_menu";

    @Autowired
    public VisitorsController(JWTUtil jwtUtil, PersonService personService, DatesAppointmentsService datesAppointmentsService, ModelMapper modelMapper, SpecialistsAndClientService specialistsAndClientService, SpecialistAppointmentsService specialistAppointmentsService, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.personService = personService;
        this.datesAppointmentsService = datesAppointmentsService;
        this.modelMapper = modelMapper;
        this.specialistsAndClientService = specialistsAndClientService;
        this.specialistAppointmentsService = specialistAppointmentsService;
        this.objectMapper = objectMapper;
    }

    //ToDo в разделе Мой профиль - добавить окно с выбором специалистов за которыми закреплен клиент, чтобы клиент мог переключаться между ними (специалистами)

    //отображение меню специалиста за которым закреплен клиент
    @GetMapping("/my_specialist_menu") //ToDo добавить в конфиг - доступ только для авторизированных пользователей
    public String getMySpecialistMenu(Model model, HttpServletRequest request) {

        if (jwtUtil.isValidJWTAndSession(request)) {
            displayPage(model, request);
        } else {
            return ERROR_LOGIN;
        }
        return "visitors/my_specialist_menu";
    }

    //Открывает отдельную страницу с отображением только календаря
    @GetMapping("/full_calendar") //ToDo Формально не отображает ВЕСЬ календарь только 20 дней
    public String getFullCalendar(Model model, HttpServletRequest request) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        Optional<SpecialistsAndClient> assignedToSpecialist = specialistsAndClientService.findByVisitorListId((Long) request.getSession().getAttribute("id"));
        Map<LocalDate, Map<String, String>> schedule = datesAppointmentsService.getCalendarFreeScheduleById(assignedToSpecialist.get().getSpecialistList().getId());
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
                e.printStackTrace();
            }
        }
        model.addAttribute("idSpecialist", assignedToSpecialist.get().getSpecialistList().getId());
        model.addAttribute("nameClient", assignedToSpecialist.get().getVisitorList().getUsername());
        for (int i = 0; i < allCalendar.size(); i++) {
            model.addAttribute("day" + i, allCalendar.get(i));
        }
        return "visitors/full_calendar";
    }

    @PostMapping("/appointment_booking_table")
    public String setAppointmentBookingTable(Model model, HttpServletRequest request,
                                             @RequestBody Map<String, String> applicationFromVisitor) {

        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        String specialistId = applicationFromVisitor.get("specialistId");
        String meetingDate = applicationFromVisitor.get("meetingDate");
        String meetingTime = applicationFromVisitor.get("meetingTime");

        if (!specialistId.equals("") && !meetingDate.equals("") && !meetingTime.equals("")) {
            LocalDate date = LocalDate.parse(meetingDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            LocalTime time = LocalTime.parse(meetingTime, DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime meeting = date.atTime(time);

            PersonFullName personFullNameRegistered =
                    modelMapper.map(personService.findById((Long) request.getSession().getAttribute("id")), PersonFullName.class);

            datesAppointmentsService.enrollVisitorNewAppointments(meeting, personFullNameRegistered, Long.valueOf(specialistId), VISITOR);
            displayPage(model, request);

        } else {
            return encodeError("Если Вы видите это сообщение, то произошла неизвестная ошибка");
        }
        return ENROLL_VIEW_REDIRECT;
    }
//
//    @GetMapping("/refresh")
//    public String getRefresh(Model model, HttpServletRequest request) {
//        if (!jwtUtil.isValidJWTAndSession(request)) {
//            return ERROR_LOGIN;
//        }
//        displayPage(model, request);
//        return "redirect:/visitors/my_specialist_menu";
//    }

    @PostMapping("/appointment_booking_form")
    public String setAppointmentBookingForm(Model model, HttpServletRequest request,
                                            @RequestParam("selectedSpecialistId") String selectedSpecialistId,
                                            @RequestParam("meetingDataTime") Optional<@NotNull LocalDateTime> meeting) {

        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }

        if (meeting.isPresent()) {
            PersonFullName personFullNameRegistered = modelMapper.map(personService.findById(
                    (Long) request.getSession().getAttribute("id")), PersonFullName.class);

            datesAppointmentsService.enrollVisitorNewAppointments(meeting.get(), personFullNameRegistered, Long.valueOf(selectedSpecialistId), VISITOR);
            displayPage(model, request);
        } else {
            return encodeError("Для записи нужно выбрать ДАТУ и ВРЕМЯ приема");
        }

        return ENROLL_VIEW_REDIRECT;
    }

    @PostMapping("/cancellingBookingVisitor")
    public String cancellingBookingVisitor(Model model, HttpServletRequest request,
                                    @RequestParam("selectedSpecialistId") String selectedSpecialistId,
                                    @RequestParam("meetingCancel") Optional<@NotNull LocalDateTime> meetingCancel) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        if (meetingCancel.isPresent()) {
            datesAppointmentsService.cancellingBookingAppointments(meetingCancel.get(), Long.valueOf(selectedSpecialistId));
            displayPage(model, request);
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

    private void displayPage(Model model, HttpServletRequest request) {
        Optional<SpecialistsAndClient> assignedToSpecialist = specialistsAndClientService.findByVisitorListId((Long) request.getSession().getAttribute("id"));
        Map<LocalDate, Map<String, String>> schedule = datesAppointmentsService.getCalendarFreeScheduleById(assignedToSpecialist.get().getSpecialistList().getId());
        List<String> nearestDates = datesAppointmentsService.getFiveNearestDates(schedule, assignedToSpecialist.get().getVisitorList().getFullName());

        List<SpecialistAppointments> appointmentsList = specialistAppointmentsService.findAllAppointments();
//        List<LocalDateTime> times = new ArrayList<>();
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


        model.addAttribute("nameClient", assignedToSpecialist.get().getVisitorList().getUsername());
        model.addAttribute("idSpecialist", assignedToSpecialist.get().getSpecialistList().getId());
        //ToDo решить почему не работает с пустым списком как это исправить!!!!!!!!!!!!!!!!!!!!!!!!!!!! - на этот метод поставить оптионал datesAppointmentsService.getFiveNearestDates
        if (!nearestDates.isEmpty()) {
            model.addAttribute("day1", nearestDates.get(0));
            model.addAttribute("day2", nearestDates.get(1));
            model.addAttribute("day3", nearestDates.get(2));
            model.addAttribute("day4", nearestDates.get(3));
            model.addAttribute("day5", nearestDates.get(4));
        }
    }

    private String encodeError(String error) {
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
