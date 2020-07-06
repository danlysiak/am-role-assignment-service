package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

@RunWith(MockitoJUnitRunner.class)
class CreateRoleAssignmentControllerTest {

    private final Map<String, String> headers = new HashMap<>() {
        {
            put("serviceauthorisation", "Bearer dummyToken");
        }
    };

    @Mock
    private CreateRoleAssignmentOrchestrator createRoleAssignmentServiceMock =
        mock(CreateRoleAssignmentOrchestrator.class);

    @InjectMocks
    private CreateAssignmentController sut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createRoleAssignment() throws Exception { //TODO improve this
        AssignmentRequest request = TestDataBuilder.buildAssignmentRequest();
        ResponseEntity<Object> expectedResponse = TestDataBuilder.buildResponseEntity(request);
        when(createRoleAssignmentServiceMock.createRoleAssignment(request, headers)).thenReturn(expectedResponse);
        ResponseEntity<Object> response = sut.createRoleAssignment(request, headers);
        assertNotNull(response);
        assertEquals(expectedResponse.getStatusCode(),response.getStatusCode());
        assertEquals(expectedResponse.getBody(),response.getBody());
    }
}
