package ru.genesizant.Professional.Timetable.aspect;

import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.model.Person;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class JwtValidAspect {

    private final JWTUtil jwtUtil;
    @Pointcut("execution(public String listDebtorsS(ru.genesizant.Professional.Timetable.model.Person)) ||" +
            "execution(public String addAdmissionCalendarView(ru.genesizant.Professional.Timetable.model.Person)) ||" +
            "execution(public String addAdmissionCalendarUpdate(ru.genesizant.Professional.Timetable.model.Person)) ||" +
            "execution(public String getStartMenu(ru.genesizant.Professional.Timetable.model.Person)) ||" +
            "execution(public String listDebtorsV(ru.genesizant.Professional.Timetable.model.Person)) ||" +
            "execution(public String visitorProfile(ru.genesizant.Professional.Timetable.model.Person)) ||" +
            "execution(public String getMySpecialistMenu(ru.genesizant.Professional.Timetable.model.Person))")
    public void jwtValidPointcut() {}

    @Around("jwtValidPointcut()")
    public Object jwtControl(ProceedingJoinPoint joinPoint) throws Throwable {
        Person person = (Person) joinPoint.getArgs()[0];
        if (!jwtUtil.isValidJWTInRun(person.getJwtToken())) {
            log.error("Ошибка валидации JWT токена у пользователя - " + person.getFullName() + " id: " + person.getId());
            throw new JWTVerificationException("Закончилось безопасное время использования приложения, нужно обновить доступ, перезапустите приложение");
        }
        log.info("Пользователь: " + person.getFullName() + ". Перешел на страницу " + joinPoint.getSignature().getName());
        return joinPoint.proceed();
    }


//    Установить для конкретного класса
//    @Pointcut("within(ru.genesizant.Professional.Timetable.controllers.TestController)")

    //    для всех определнных бинов
//    @Pointcut("bean(*Service)")

    //    Установить для всех классов в пакете
//    @Pointcut("within(ru.genesizant.Professional.Timetable.controllers.*)")

    //к методам, которые возвращают Стринг, называются ХХХ, и принимают аргумент Стринг
//    @Pointcut("execution(String getGreed(String)")

    //все методы которые принимают определенную переменную, напрм класс DatesAppointmentsService
//    @Pointcut("args(ru.genesizant.Professional.Timetable.services.DatesAppointmentsService)")

    // также можно получить доступ к аргументу
//    @Pointcut("args(req)")
//    public void any(DatesAppointmentsService req) {
//
//    }
//    @Before("any(req)")
//    public void beforeAnyController(DatesAppointmentsService req) {
//        log.info("======BEFORE====== {}", req);
//    }
}
