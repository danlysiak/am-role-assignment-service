package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentRepository;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

class UserCountControllerTest {

    @Mock
    private TelemetryClient telemetryClientMock;

    @Mock
    private RoleAssignmentRepository roleAssignmentRepositoryMock;

    @InjectMocks
    @Spy
    private final UserCountController sut = new UserCountController();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldGetUserCountResponse() throws SQLException, JsonProcessingException {

        RoleAssignmentRepository.JurisdictionRoleCategoryAndCount userCount1 =
            new RoleAssignmentRepository.JurisdictionRoleCategoryAndCount() {

                @Override
                public String getJurisdiction() {
                    return "some jurisdiction";
                }

                @Override
                public String getRoleCategory() {
                    return "some role category";
                }

                @Override
                public BigInteger getCount() {
                    return BigInteger.TEN;
                }
            };

        RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount userCount2 =
            new RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount() {

                @Override
                public String getJurisdiction() {
                    return "some jurisdiction";
                }

                @Override
                public String getRoleCategory() {
                    return "some role category";
                }

                @Override
                public String getRoleName() {
                    return "some role name";
                }

                @Override
                public BigInteger getCount() {
                    return BigInteger.TWO;
                }
            };

        doReturn(List.of(userCount1)).when(roleAssignmentRepositoryMock).getOrgUserCountByJurisdiction();
        doReturn(List.of(userCount2)).when(roleAssignmentRepositoryMock).getOrgUserCountByJurisdictionAndRoleName();

        ResponseEntity<Map<String, Object>> response = sut.getOrgUserCount();
        final List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount> responseOrgUserCountByJurisdiction =
            (List<RoleAssignmentRepository.JurisdictionRoleCategoryAndCount>)
                response.getBody().get("OrgUserCountByJurisdiction");
        final List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount>
            responseOrgUserCountByJurisdictionAndRoleName =
            (List<RoleAssignmentRepository.JurisdictionRoleCategoryNameAndCount>)
                response.getBody().get("OrgUserCountByJurisdictionAndRoleName");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigInteger.TEN, responseOrgUserCountByJurisdiction.get(0).getCount());
        assertEquals("some role category", responseOrgUserCountByJurisdiction.get(0).getRoleCategory());
        assertEquals(BigInteger.TWO, responseOrgUserCountByJurisdictionAndRoleName.get(0).getCount());
    }

}
