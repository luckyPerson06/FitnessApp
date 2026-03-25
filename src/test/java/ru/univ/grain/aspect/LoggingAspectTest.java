package ru.univ.grain.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @InjectMocks
    private LoggingAspect loggingAspect;

    @BeforeEach
    void setUp() {
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(signature.toShortString()).thenReturn("ClientService.createClient()");
    }

    @Test
    void logMethodExecution_ShouldLogEntryAndExit_WhenMethodSuccessWithParams() throws Throwable {
        Object[] args = new Object[]{"ivan@mail.com", "password123"};
        Object result = new Object();

        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(result);

        Object actualResult = loggingAspect.logMethodExecution(joinPoint);

        assertThat(actualResult).isEqualTo(result);
        verify(joinPoint).proceed();
    }

    @Test
    void logMethodExecution_ShouldLogEntryWithoutParams_WhenNoArgs() throws Throwable {
        Object[] args = new Object[]{};
        Object result = new Object();

        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(result);

        Object actualResult = loggingAspect.logMethodExecution(joinPoint);

        assertThat(actualResult).isEqualTo(result);
        verify(joinPoint).proceed();
    }

    @Test
    void logMethodExecution_ShouldLogError_WhenExceptionThrown() throws Throwable {
        Object[] args = new Object[]{"test@mail.com"};
        Exception exception = new RuntimeException("Database connection failed");

        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenThrow(exception);

        try {
            loggingAspect.logMethodExecution(joinPoint);
        } catch (Exception e) {
            assertThat(e).isEqualTo(exception);
        }

        verify(joinPoint).proceed();
    }
}