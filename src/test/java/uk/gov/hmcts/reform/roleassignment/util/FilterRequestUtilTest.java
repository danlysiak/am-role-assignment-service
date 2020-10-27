package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
class FilterRequestUtilTest {

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    HttpServletResponse httpServletResponse;

    @Mock
    FilterChain filterChain;

    @Mock
    CorrelationInterceptorUtil correlationInterceptorUtil;

    @InjectMocks
    private FilterRequestUtil sut = new FilterRequestUtil();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void doFilterInternal() throws ServletException, IOException {
        Mockito.when(correlationInterceptorUtil.preHandle(any(HttpServletRequest.class)))
            .thenReturn("a5cff648-84b6-404d-83d6-f86b526cc59b");
        sut.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        verify(correlationInterceptorUtil, times(1))
            .preHandle(any(HttpServletRequest.class));
    }

    @Test
    void doFilterInternal_throwsBadRequest() {
        Mockito.when(correlationInterceptorUtil.preHandle(any(HttpServletRequest.class)))
            .thenReturn("a5cff648-84b6-404d-83d6");
        Assertions.assertThrows(BadRequestException.class, () ->
            sut.doFilterInternal(httpServletRequest, httpServletResponse, filterChain)
        );
        verify(correlationInterceptorUtil, times(1))
            .preHandle(any(HttpServletRequest.class));
    }
}