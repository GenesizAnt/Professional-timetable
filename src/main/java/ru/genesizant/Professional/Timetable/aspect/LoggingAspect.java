package ru.genesizant.Professional.Timetable.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Component;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;

import javax.sql.rowset.Joinable;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class LoggingAspect {

    private final JWTUtil jwtUtil;

//    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.ModelAttribute)")
    @Pointcut("within(ru.genesizant.Professional.Timetable.controllers.mng..*)")
    public void anyControllerLogging() {

    }

//    @Before("anyControllerLogging()")
//    public void beforeAnyController(JoinPoint joinPoint) {
//        log.info("======BEFORE======");
////        log.info("{}", joinPoint.getArgs());
//        Object[] args = joinPoint.getArgs();
//        for (Object arg : args) {
//            System.out.println(arg);
//        }
//        log.info("======AFTER======");
//
//    }

    @Around("anyControllerLogging()")
    public Object beforeAnyController(ProceedingJoinPoint point) throws Throwable {
        log.info("======BEFORE======");
        log.info("getKind {}", point.getKind());
        log.info("getSignature {}", point.getSignature());
        log.info("getTarget {}", point.getTarget());
        log.info("getSourceLocation {}", point.getSourceLocation());
        log.info("getStaticPart {}", point.getStaticPart());
        log.info("getThis {}", point.getThis());
        log.info("getClass {}", point.getClass());
        Object[] args = point.getArgs();
        for (Object arg : args) {
            if (arg instanceof Map<?,?>) {
                for (Map.Entry<?,?> obj : ((Map<?, ?>) arg).entrySet()) {
                    System.out.println("Ключ: " + obj.getKey() + " : " + obj.getValue());
                }
            }
            if (arg instanceof SecurityContextHolderAwareRequestWrapper) { //jwtToken
                String jwtToken = (String) ((SecurityContextHolderAwareRequestWrapper) arg).getSession().getAttribute("jwtToken");
            }
        }
        log.info("======AFTER======");
        var result = point.proceed(args);
        return result;
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
