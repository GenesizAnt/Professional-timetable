package ru.genesizant.Professional.Timetable.controllers.mng.specialist;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistPay;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.SpecialistPayService;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegram;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegramService;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/spec_profile")
public class SpecialistProfileController {

    private final JWTUtil jwtUtil;
    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final String PROFILE_SPEC_VIEW_REDIRECT = "redirect:/spec_profile/my_profile";

    private final UserTelegramService userTelegramService;
    private final PersonService personService;
    private final SpecialistPayService specialistPayService;

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
        Optional<UserTelegram> userTelegram = userTelegramService.findByPersonId((Long) request.getSession().getAttribute("id"));
        if (userTelegram.isPresent()) {
            userTelegram.get().setAgree(Boolean.TRUE);
            userTelegramService.save(userTelegram.get());
            log.info("Спец: " + request.getSession().getAttribute("id") + ". Нажал кнопку подтвердить аккаунт в ТГ боте");
        } else {
            return encodeError("Не найден аккаунт в ТГ-боте, перейдите в бот и зарегистрируйтесь https://t.me/TimeProfessionalBot");
        }
        return PROFILE_SPEC_VIEW_REDIRECT;
    }

    // Отметить подтверждение зарегистрированного аккаунта в ТГ боте - т.е. удалить его
    @GetMapping("/cancelTG")
    public String cancelTG(HttpServletRequest request) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        userTelegramService.deleteByPersonId((Long) request.getSession().getAttribute("id"));
        log.info("Спец: " + request.getSession().getAttribute("id") + ". Нажал кнопку отметить подтверждение зарегистрированного аккаунта в ТГ боте");
        return PROFILE_SPEC_VIEW_REDIRECT;
    }

    @PostMapping("/setlinkpay")
    public String processForm(@RequestParam("linkpay") String linkpay, HttpServletRequest request) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        specialistPayService.saveNewLinkPay(linkpay, (Long) request.getSession().getAttribute("id"));
        log.info("Спец: " + request.getSession().getAttribute("id") + ". Обновил ссылку для оплаты");
        return PROFILE_SPEC_VIEW_REDIRECT;
    }


    private void displayPage(Model model, HttpServletRequest request) {
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

    private String encodeError(String error) {
        String ERROR_VALIDATE_FORM = "redirect:/spec_profile/my_profile?error=";
        return ERROR_VALIDATE_FORM + URLEncoder.encode(error, StandardCharsets.UTF_8);
    }
}
