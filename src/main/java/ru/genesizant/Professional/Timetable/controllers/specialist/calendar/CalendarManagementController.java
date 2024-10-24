package ru.genesizant.Professional.Timetable.controllers.specialist.calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.model.BaseSchedule;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.VacantSeat;
import ru.genesizant.Professional.Timetable.services.BaseScheduleService;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.VacantSeatService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/calendar")
public class CalendarManagementController {

    private final String CALENDAR_VIEW_REDIRECT = "redirect:/calendar/admission_calendar_view";

    private final PersonService personService;
    private final VacantSeatService vacantSeatService;
    private final BaseScheduleService baseScheduleService;

    @ModelAttribute
    public void getPayloadPage(@ModelAttribute("specialist") Person specialist, Model model, HttpServletRequest request) {
        model.addAttribute("name", request.getSession().getAttribute("name"));
        // Обработка параметров пагинации
        int page = request.getSession().getAttribute("page") != null ? (int) request.getSession().getAttribute("page") : 0;
        int size = request.getSession().getAttribute("size") != null ? (int) request.getSession().getAttribute("size") : 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateVacant").and(Sort.by("timeVacant")));

        Page<VacantSeat> vacantSeatsPage = vacantSeatService.getVacantSeatsPage(specialist, pageable);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        vacantSeatsPage.getContent().forEach(vacantSeat -> {
            vacantSeat.setFormattedDate(vacantSeat.getDateVacant().format(formatter));
        });

        int totalPages = vacantSeatsPage.getTotalPages();
        int currentPage = vacantSeatsPage.getNumber();
        int visiblePages = 5; // Количество отображаемых страниц

        int startPage = Math.max(0, currentPage - visiblePages / 2);
        int endPage = Math.min(totalPages - 1, startPage + visiblePages - 1);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("vacantSeats", vacantSeatsPage.getContent());
        model.addAttribute("page", vacantSeatsPage);

        Optional<BaseSchedule> scheduleSpecialist = baseScheduleService.getBaseScheduleSpecialist(specialist);
        scheduleSpecialist.ifPresent(baseSchedule -> model.addAttribute("schedule", baseSchedule));
    }

    @ModelAttribute(name = "specialist")
    public Person getSpecialist(HttpServletRequest request) {
        return personService.findById((Long) request.getSession().getAttribute("id")).orElseThrow();
    }

    //Отображение страницы управление календарем (создание расписания - доступного/не доступного времени)
    @GetMapping("/admission_calendar_view")
    public String addAdmissionCalendarView(@ModelAttribute("specialist") Person specialist) {
        return "specialist/admission_calendar_view";
    }

    // Кнопка для автоматического заполнения календаря на будущий период
    @PostMapping("/admission_calendar_update")
    public String addAdmissionCalendarUpdate(@ModelAttribute("specialist") Person specialist,
                                             @RequestParam("startDate") String startDate,
                                             @RequestParam("endDate") String endDate,
                                             @RequestParam(value = "startTime", required = false) String startTime,
                                             @RequestParam(value = "endTime", required = false) String endTime,
                                             @RequestParam("minInterval") String minInterval,
                                             @RequestParam(value = "weekDays", required = false) List<String> weekDays) {
        if (isValidFormAddCalendarAdmission(startDate, endDate, startTime, endTime, minInterval)) {
            if (weekDays == null || weekDays.isEmpty()) {
                weekDays = List.of(""); // Устанавливаем значение по умолчанию
            }
            if (vacantSeatService.isDateWithinRangeOfAppointments(startDate, endDate, specialist)) {
                return encodeError("Нельзя добавить календарь в уже существующих датах");
            } else {
                vacantSeatService.addFreeDateSchedule(specialist, startDate, endDate, startTime, endTime, minInterval, weekDays);
                log.info("Спец: " + specialist.getFullName() + ". Нажал кнопку создания расписания");
            }
        } else {
            return encodeError("Для создания календаря нужно внести все данные по форме");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    @PostMapping("/removeVacantSlot")
    public String handleTableClick(@RequestBody Map<String, String> applicationFromSpecialist) {
        vacantSeatService.removeVacantSlot(applicationFromSpecialist.get("id"));
        return CALENDAR_VIEW_REDIRECT;
    }

    @PostMapping("/add_sample_available_time")
    public String addSampleAvailableTime(@ModelAttribute("specialist") Person specialist,
                                         @RequestParam(name = "startDays") String startDays) {
        Optional<BaseSchedule> baseScheduleSpecialist = baseScheduleService.getBaseScheduleSpecialist(specialist);
        if (baseScheduleSpecialist.isPresent()) {
            vacantSeatService.addFreeDateSchedule(specialist, startDays, getEndDay(startDays, baseScheduleSpecialist),
                    baseScheduleSpecialist.get().getStartTime().toString(),
                    baseScheduleSpecialist.get().getEndTime().toString(), baseScheduleSpecialist.get().getMinInterval().toString(),
                    baseScheduleSpecialist.get().getWeekDays());
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Удалить полный День из доступных для выбора дат
    @PostMapping("/dateFormDelete")
    public String selectedDateFormDelete(@ModelAttribute("specialist") Person specialist,
                                         @RequestParam("selectedDate") LocalDate selectedDate) {
        if (selectedDate != null) {
            vacantSeatService.deleteVisitDate(specialist, selectedDate);
            log.info("Спец: " + specialist.getFullName() + ". Удалил полный день " + selectedDate);
        } else {
            return encodeError("Для удаления нужно выбрать дату");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    //Добавить в календарь конкретное время, дату и статус доступное для приема
    @PostMapping("/addTimeAvailability")
    public String addTimeAvailability(@ModelAttribute("specialist") Person specialist,
                                      @RequestParam("timeAvailability") String timeAvailability,
                                      @RequestParam("dateAddTime") LocalDate date) {
        if (isValidFormAddTimeAvailability(date, timeAvailability)) {
            vacantSeatService.addTimeAvailability(specialist,
                    date,
                    LocalTime.of(Integer.parseInt(timeAvailability.split(":")[0]),
                            Integer.parseInt(timeAvailability.split(":")[1])));
            log.info("Спец: " + specialist.getFullName() + ". Добавил в календарь конкретное время, дату и статус доступное для приема");
        } else {
            return encodeError("Нужно выбрать дату, время и статус времени приема");
        }
        return CALENDAR_VIEW_REDIRECT;
    }

    @GetMapping("/vacantSeats")
    public String getVacantSeats(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        if (page < 0) {
            page = 0; // Устанавливаем на страницу 0, если индекс меньше 0
        }
        request.getSession().setAttribute("page", page);
        request.getSession().setAttribute("size", size);
        Page<VacantSeat> all = vacantSeatService.findAll(PageRequest.of(page, size));
        model.addAttribute("vacantSeats", all.getContent());
        model.addAttribute("page", all);
        return CALENDAR_VIEW_REDIRECT;
    }

    private String getEndDay(String sDays, Optional<BaseSchedule> baseScheduleSpecialist) {
        LocalDate startDays = LocalDate.parse(sDays);
        return startDays.plusDays(baseScheduleSpecialist.get().getCountDays()).toString();
    }

    private String encodeError(String error) {
        String ERROR_VALIDATE_FORM = "redirect:/calendar/admission_calendar_view?error=";
        return ERROR_VALIDATE_FORM + URLEncoder.encode(error, StandardCharsets.UTF_8);
    }

    private boolean isValidFormAddCalendarAdmission(String startDate, String endDate, String startTime, String endTime, String minInterval) {
        return !startDate.isEmpty() && !endDate.isEmpty() && !minInterval.isEmpty();
    }
    private boolean isValidFormAddTimeAvailability(LocalDate date, String timeAvailability) {
        return date != null && !timeAvailability.isEmpty();
    }
}
