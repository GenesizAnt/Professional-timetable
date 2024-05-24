package ru.genesizant.Professional.Timetable.controllers.specialist;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegram;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegramService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/spec_profile")
public class SpecialistProfileController {

    private final JWTUtil jwtUtil;
    private final UserTelegramService userTelegramService;
    private final PersonService personService;
    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final String PROFILE_SPEC_VIEW_REDIRECT = "redirect:/spec_profile/my_profile";

    public SpecialistProfileController(JWTUtil jwtUtil, UserTelegramService userTelegramService, PersonService personService) {
        this.jwtUtil = jwtUtil;
        this.userTelegramService = userTelegramService;
        this.personService = personService;
    }

    // Отображение страницы профиля специалиста
    @GetMapping("/my_profile")
    public String listDebtors(Model model, HttpServletRequest request) {
        if (jwtUtil.isValidJWTAndSession(request)) {
            displayPage(model, request);
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Перешел на страницу профиля специалиста");
        } else {
            return ERROR_LOGIN;
        }
        return "specialist/specialistprofile";
    }

    // Подтвердить аккаунт в ТГ боте
    @GetMapping("/agreeTG")
    public String agreeTG(HttpServletRequest request) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        UserTelegram userTelegram = userTelegramService.findByPersonId((Long) request.getSession().getAttribute("id"));
        userTelegram.setAgree(Boolean.TRUE);
        userTelegramService.save(userTelegram);
        log.info("Спец: " + request.getSession().getAttribute("id") + ". Нажал кнопку подтвердить аккаунт в ТГ боте");
        return PROFILE_SPEC_VIEW_REDIRECT;
    }

    // Отметить подтверждение зарегистрированного аккаунта в ТГ боте - т.е. удалить его
    @GetMapping("/cancelTG")
    public String cancelTG(Model model, HttpServletRequest request) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        userTelegramService.deleteByPersonId((Long) request.getSession().getAttribute("id"));
        log.info("Спец: " + request.getSession().getAttribute("id") + ". Нажал кнопку отметить подтверждение зарегистрированного аккаунта в ТГ боте");
        return PROFILE_SPEC_VIEW_REDIRECT;
    }


    private void displayPage(Model model, HttpServletRequest request) {
        UserTelegram userTelegram = userTelegramService.findByPersonId((Long) request.getSession().getAttribute("id"));
        Optional<Person> person = personService.findById((Long) request.getSession().getAttribute("id"));
        if (userTelegram == null) {
            model.addAttribute("notacc", "");
        } else {
            if (userTelegram.isAgree()) {
                model.addAttribute("agree", "подтвержден");
            }
            if (!userTelegram.isAgree()) {
                model.addAttribute("notagree", "не подтвержден");
                model.addAttribute("username", userTelegram.getPersonusername());
            }
        }
        model.addAttribute("name", request.getSession().getAttribute("name"));
        String currentUrl = request.getRequestURL().toString();
        String baseUrl = extractBaseUrl(currentUrl);
        model.addAttribute("baseUrl", baseUrl + "auth/registration?phone=" + person.get().getPhoneNumber() + "&role=client");
//        model.addAttribute("specialistPhone", "?regNumber=" + person.get().getPhoneNumber());
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
}
