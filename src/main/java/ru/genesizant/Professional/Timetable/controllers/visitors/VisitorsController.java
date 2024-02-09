package ru.genesizant.Professional.Timetable.controllers.visitors;

import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistsAndClient;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.SpecialistsAndClientService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/visitors")
public class VisitorsController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;
    private final ModelMapper modelMapper;
    private final SpecialistsAndClientService specialistsAndClientService;

    @Autowired
    public VisitorsController(JWTUtil jwtUtil, PersonService personService, DatesAppointmentsService datesAppointmentsService, ModelMapper modelMapper, SpecialistsAndClientService specialistsAndClientService) {
        this.jwtUtil = jwtUtil;
        this.personService = personService;
        this.datesAppointmentsService = datesAppointmentsService;
        this.modelMapper = modelMapper;
        this.specialistsAndClientService = specialistsAndClientService;
    }

    //ToDo в разделе Мой профиль - добавить окно с выбором специалистов за которыми закреплен клиент, чтобы клиент мог переключаться между ними (специалистами)

    //отображение меню специалиста за которым закреплен клиент
    @GetMapping("/my_specialist_menu") //ToDo добавить в конфиг - доступ только для авторизированных пользователей
    public String getMySpecialistMenu(Model model, HttpServletRequest request) {

        Optional<Person> person = personService.findById((Long) request.getSession().getAttribute("id"));
        model.addAttribute("name", person.get().getUsername());

        //ToDo в displayPage ДОБАВИТЬ СПЕЦИАЛИСТА ИЗ БАЗЫ И КАЖДЫЙ РАЗ ЕГО ТЯНУТЬ
//        displayPage(model, selectedSpecialistId, personFullNameRegistered, request);

        return "visitors/my_specialist_menu";
    }

    @PostMapping("/appointment_booking")
    public String setAppointmentBooking(Model model, HttpServletRequest request,
                                        @RequestParam("selectedSpecialistId") String selectedSpecialistId,
                                        @RequestParam("meetingDataTime") LocalDateTime meeting) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            PersonFullName personFullNameRegistered =
                    modelMapper.map(personService.findById((Long) request.getSession().getAttribute("id")), PersonFullName.class);


            datesAppointmentsService.enrollVisitorNewAppointments(meeting, personFullNameRegistered, Long.valueOf(selectedSpecialistId));

            model.addAttribute("selectedSpecialistId", selectedSpecialistId);
            displayPage(model, selectedSpecialistId, personFullNameRegistered, request);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }

        return "visitors/specialist_choose";
    }


    //отображение меню конкретного специалиста для клиента (запись, даты и пр.)
    @GetMapping("/specialist_choose/{id}") //ToDo добавить в конфиг - доступ только для авторизированных пользователей
    public String getSpecialistMenu(Model model, HttpServletRequest request, @PathVariable String id) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            PersonFullName personFullNameRegistered =
                    modelMapper.map(personService.findById((Long) request.getSession().getAttribute("id")), PersonFullName.class);

            model.addAttribute("selectedSpecialistId", id);
            displayPage(model, id, personFullNameRegistered, request);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error"; //ToDo добавить считывание ошибки и правильного отображения сейчас отображается "Неправильные имя или пароль"
        }

        return "visitors/specialist_choose";
    }

    @PostMapping("/cancelling_booking")
    public String setCancellingBooking(Model model, HttpServletRequest request,
                                        @RequestParam("selectedSpecialistId") String selectedSpecialistId,
                                        @RequestParam("meetingCancel") LocalDateTime meetingCancel) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            PersonFullName personFullNameRegistered =
                    modelMapper.map(personService.findById((Long) request.getSession().getAttribute("id")), PersonFullName.class);

            if (datesAppointmentsService.isCheckAvailableCancellingBooking(meetingCancel, personFullNameRegistered.toString(), Long.valueOf(selectedSpecialistId))) {

                datesAppointmentsService.cancellingBookingAppointments(meetingCancel, Long.valueOf(selectedSpecialistId));
                model.addAttribute("selectedSpecialistId", selectedSpecialistId); //ToDo нормально ли добавлять атрибут каждый раз???

            } else {
                model.addAttribute("notAvailable", "НЕЛЬЗЯ ОТМЕНИТЬ ЧУЖУЮ ЗАПИСЬ!");
            }


            displayPage(model, selectedSpecialistId, personFullNameRegistered, request);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }

        return "visitors/specialist_choose";
    }

    @GetMapping("/assigned_specialist/{id}")
    public String appointSpecialist(Model model, HttpServletRequest request, @PathVariable String id) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            PersonFullName personFullNameRegistered =
                    modelMapper.map(personService.findById((Long) request.getSession().getAttribute("id")), PersonFullName.class);

            specialistsAndClientService.appointSpecialist(personFullNameRegistered.getId(), Long.valueOf(id));

            model.addAttribute("selectedSpecialistId", id);
            displayPage(model, id, personFullNameRegistered, request);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error"; //ToDo добавить считывание ошибки и правильного отображения сейчас отображается "Неправильные имя или пароль"
        }

        return "visitors/specialist_choose";
    }

    //ToDo добавить отображение отметки "Закреплен ли сейчас посетитель за специалистом или нет"

    private void displayPage(Model model, @PathVariable String id, PersonFullName personFullNameRegistered, HttpServletRequest request) {
        Optional<Person> specialist = personService.findById(Long.valueOf(id));
        Optional<Person> visitor = personService.findById((Long) request.getSession().getAttribute("id"));
        Map<LocalDate, Map<String, String>> sortedFreeSchedule = datesAppointmentsService.getCalendarFreeScheduleForVisitor(Long.parseLong(id), personFullNameRegistered.toString());

        Optional<SpecialistsAndClient> assignedToSpecialist = specialistsAndClientService.assignedToSpecialist(specialist.get(), visitor.get());
        if (assignedToSpecialist.isPresent()) {
            model.addAttribute("assignedToSpecialist", assignedToSpecialist);
        }
        model.addAttribute("specialist", specialist.get().getUsername());
        model.addAttribute("dates", sortedFreeSchedule);
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
