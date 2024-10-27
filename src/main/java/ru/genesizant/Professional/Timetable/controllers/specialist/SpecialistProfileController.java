package ru.genesizant.Professional.Timetable.controllers.specialist;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.model.BaseSchedule;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistPay;
import ru.genesizant.Professional.Timetable.services.BaseScheduleService;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.SpecialistPayService;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegram;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegramService;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/spec_profile")
public class SpecialistProfileController {

    private final String PROFILE_SPEC_VIEW_REDIRECT = "redirect:/spec_profile/my_profile";

    private final UserTelegramService userTelegramService;
    private final PersonService personService;
    private final SpecialistPayService specialistPayService;
    private final BaseScheduleService baseScheduleService;

    @ModelAttribute
    public void getPayloadPage(Model model, HttpServletRequest request) {
        Optional<UserTelegram> userTelegram = userTelegramService.findByPersonId((Long) request.getSession().getAttribute("id"));
        Optional<Person> person = personService.findById((Long) request.getSession().getAttribute("id"));
        Optional<SpecialistPay> specialistPay = specialistPayService.findBySpecialistPay((Long) request.getSession().getAttribute("id"));
        if (userTelegram.isEmpty()) {
            model.addAttribute("notacc", "");
        } else {
            if (userTelegram.get().isAgree()) {
                model.addAttribute("agree", "подтвержден");
            }
            if (!userTelegram.get().isAgree()) {
                model.addAttribute("notagree", "не подтвержден");
                model.addAttribute("username", userTelegram.get().getPersonusername());
            }
        }
        specialistPay.ifPresent(pay -> model.addAttribute("link", pay.getLinkPay()));
        model.addAttribute("name", request.getSession().getAttribute("name"));
        String currentUrl = request.getRequestURL().toString();
        String baseUrl = extractBaseUrl(currentUrl);
        person.ifPresent(value -> model.addAttribute("baseUrl", baseUrl + "auth/registration?phone=" + value.getPhoneNumber() + "&role=client"));

        Optional<BaseSchedule> scheduleSpecialist = baseScheduleService.getBaseScheduleSpecialist(person.get());
        scheduleSpecialist.ifPresent(baseSchedule -> model.addAttribute("schedule", baseSchedule));
    }
    @ModelAttribute(name = "specialist")
    public Person getSpecialist(HttpServletRequest request) {
        return personService.findById((Long) request.getSession().getAttribute("id")).orElseThrow();
    }

    // Отображение страницы профиля специалиста
    @GetMapping("/my_profile")
    public String listDebtorsS(@ModelAttribute("specialist") Person specialist) {
        return "specialist/specialistprofile";
    }

    // Подтвердить аккаунт в ТГ боте
    @GetMapping("/agreeTG")
    public String agreeTG(@ModelAttribute("specialist") Person specialist) {
        Optional<UserTelegram> userTelegram = userTelegramService.findByPersonId(specialist.getId());
        if (userTelegram.isPresent()) {
            userTelegram.get().setAgree(Boolean.TRUE);
            userTelegramService.save(userTelegram.get());
            log.info("Спец: " + specialist.getFullName() + ". Нажал кнопку подтвердить аккаунт в ТГ боте");
        } else {
            return encodeError("Не найден аккаунт в ТГ-боте, перейдите в бот и зарегистрируйтесь https://t.me/TimeProfessionalBot");
        }
        return PROFILE_SPEC_VIEW_REDIRECT;
    }

    // Отметить подтверждение зарегистрированного аккаунта в ТГ боте - т.е. удалить его
    @GetMapping("/cancelTG")
    public String cancelTG(@ModelAttribute("specialist") Person specialist) {
        userTelegramService.deleteByPersonId(specialist.getId());
        log.info("Спец: " + specialist.getFullName() + ". Нажал кнопку отметить подтверждение зарегистрированного аккаунта в ТГ боте");
        return PROFILE_SPEC_VIEW_REDIRECT;
    }

    @PostMapping("/setlinkpay")
    public String processForm(@RequestParam("linkpay") String linkpay,@ModelAttribute("specialist") Person specialist) {
        specialistPayService.saveNewLinkPay(linkpay, specialist.getId());
        log.info("Спец: " + specialist.getFullName() + ". Обновил ссылку для оплаты");
        return PROFILE_SPEC_VIEW_REDIRECT;
    }

    @PostMapping("/set-base-schedule")
    public String setBaseSchedule(
            @RequestParam("inputStartDate") String inputStartDate,
            @RequestParam("inputEndDate") String inputEndDate,
            @RequestParam("inputTime") String inputTime,
            @RequestParam("countDays") Integer countDays,
            @RequestParam(value = "monday", required = false) Boolean monday,
            @RequestParam(value = "tuesday", required = false) Boolean tuesday,
            @RequestParam(value = "wednesday", required = false) Boolean wednesday,
            @RequestParam(value = "thursday", required = false) Boolean thursday,
            @RequestParam(value = "friday", required = false) Boolean friday,
            @RequestParam(value = "saturday", required = false) Boolean saturday,
            @RequestParam(value = "sunday", required = false) Boolean sunday,
            @ModelAttribute("specialist") Person specialist) {
        LocalTime startTime = LocalTime.parse(inputStartDate);
        LocalTime endTime = LocalTime.parse(inputEndDate);
        LocalTime minInterval = LocalTime.parse(inputTime);

        Optional<BaseSchedule> baseScheduleSpecialist = baseScheduleService.getBaseScheduleSpecialist(specialist);
        BaseSchedule baseSchedule = baseScheduleSpecialist.orElseGet(BaseSchedule::new);
        updateSchedule(countDays, monday, tuesday, wednesday, thursday, friday, saturday, sunday, specialist, startTime, endTime, minInterval, baseSchedule);

        log.info("Спец: " + specialist.getFullName() + ". Обновил шаблон расписания");
        return PROFILE_SPEC_VIEW_REDIRECT;
    }

    @PostMapping("/editTemplate")
    public void editTemplateSchedule(@ModelAttribute("specialist") Person specialist,
                                     @RequestParam("editParam") String editParam,
                                     @RequestParam("scheduleId") String scheduleId,
                                     @RequestParam("editParameter") String editParameter) {
        System.out.println();
    }

    private void updateSchedule(Integer countDays, Boolean monday, Boolean tuesday, Boolean wednesday, Boolean thursday, Boolean friday, Boolean saturday, Boolean sunday, Person specialist, LocalTime startTime, LocalTime endTime, LocalTime minInterval, BaseSchedule baseSchedule) {
        baseSchedule.setStartTime(startTime);
        baseSchedule.setEndTime(endTime);
        baseSchedule.setMinInterval(minInterval);
        baseSchedule.setCountDays(countDays);
        baseSchedule.setSpecialistBaseSchedule(specialist);

        // Логика для работы с boolean (если чекбокс не выбран, он не передастся в запросе)
        baseSchedule.setMonday(monday != null && monday);
        baseSchedule.setTuesday(tuesday != null && tuesday);
        baseSchedule.setWednesday(wednesday != null && wednesday);
        baseSchedule.setThursday(thursday != null && thursday);
        baseSchedule.setFriday(friday != null && friday);
        baseSchedule.setSaturday(saturday != null && saturday);
        baseSchedule.setSunday(sunday != null && sunday);

        // Сохранение расписания через сервис
        baseScheduleService.saveBaseSchedule(baseSchedule);
    }

    private String extractBaseUrl(String urlString) {
        String baseUrl;
        try {
            URI uri = new URI(urlString);
            int port = uri.getPort();
            String domain = uri.getScheme() + "://" + uri.getHost();
            if (port != -1) {
                domain += ":" + port;
            }
            baseUrl = domain + "/";
        } catch (URISyntaxException e) {
            e.printStackTrace();
            baseUrl = "";
        }
        return baseUrl;
    }

    private String encodeError(String error) {
        String ERROR_VALIDATE_FORM = "redirect:/spec_profile/my_profile?error=";
        return ERROR_VALIDATE_FORM + URLEncoder.encode(error, StandardCharsets.UTF_8);
    }
}
