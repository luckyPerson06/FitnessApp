package ru.univ.grain.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    private static final String LOG_METHOD_ENTRY = "Метод {} вызван с параметрами: {}";
    private static final String LOG_METHOD_EXIT_SUCCESS = "Метод {} выполнен успешно за {} мс";
    private static final String LOG_METHOD_EXIT_ERROR = "Метод {} завершился с ошибкой за {} мс: {}";


    @Pointcut("execution(* ru.univ.grain.services.*.*(..))")
    public void serviceMethods() { }

    @Pointcut("execution(* ru.univ.grain.controllers.*.*(..))")
    public void controllerMethods() { }

    @Pointcut("execution(* ru.univ.grain.repositories.*.*(..))")
    public void repositoryMethods() { }

    @Around("serviceMethods() || controllerMethods() || repositoryMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        final String methodName = joinPoint.getSignature().toShortString();
        final Object[] args = joinPoint.getArgs();
        final Instant start = Instant.now();

        if (args.length > 0) {
            log.debug(LOG_METHOD_ENTRY, methodName, Arrays.toString(args));
        } else {
            log.debug(LOG_METHOD_ENTRY, methodName, "без параметров");
        }

        try {
            final Object result = joinPoint.proceed();
            final long duration = Duration.between(start, Instant.now()).toMillis();
            log.info(LOG_METHOD_EXIT_SUCCESS, methodName, duration);
            return result;
        } catch (Exception e) {
            final long duration = Duration.between(start, Instant.now()).toMillis();
            log.error(LOG_METHOD_EXIT_ERROR, methodName, duration, e.getMessage(), e);
            throw e;
        }
    }
}
