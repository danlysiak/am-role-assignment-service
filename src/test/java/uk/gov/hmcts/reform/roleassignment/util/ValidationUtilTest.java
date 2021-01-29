package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfigRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.NUMBER_PATTERN;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.NUMBER_TEXT_PATTERN;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.TEXT_HYPHEN_PATTERN;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.TEXT_PATTERN;

class ValidationUtilTest {

    @Test
    void shouldValidate() {
        Assertions.assertDoesNotThrow(() ->
            ValidationUtil.validateId(NUMBER_PATTERN, "1212121212121213")
        );

    }

    @Test
    void shouldThrow() {
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateId(NUMBER_PATTERN, "2323232323")
        );
    }

    @Test
    void validateTextField() {
        Assertions.assertDoesNotThrow(() ->
            ValidationUtil.validateId(TEXT_PATTERN, "CREATE")
        );
    }

    @Test
    void throw_validateTextField() {
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateId(NUMBER_PATTERN, "1234")
        );
    }

    @Test
    void validateNumberTextField() {
        Assertions.assertDoesNotThrow(() ->
            ValidationUtil.validateId(NUMBER_TEXT_PATTERN, "request1")
        );
    }

    @Test
    void throw_validateNumberTextField() {
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateId(NUMBER_TEXT_PATTERN, "requ-est1")
        );
    }

    @Test
    void shouldValidateHyphenTextField() {
        Assertions.assertDoesNotThrow(() ->
            ValidationUtil.validateId(TEXT_HYPHEN_PATTERN, "north-west")
        );
    }

    @Test
    void should_ValidateHyphenTextField() {
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateId(TEXT_HYPHEN_PATTERN, "north-west1")
        );
    }

    @Test
    void validateRoleRequest() {
        Assertions.assertDoesNotThrow(() ->
            ValidationUtil.validateRoleRequest(TestDataBuilder.buildRequest(Status.APPROVED, false))
        );
    }

    @Test
    void validateRequestedRoles() {
        Assertions.assertDoesNotThrow(() ->
            ValidationUtil.validateRequestedRoles(TestDataBuilder.buildRequestedRoleCollection(Status.LIVE))
        );
    }

    @Test
    void validateRequestedRolesForCase() throws IOException {
        Collection<RoleAssignment> roleAssignments = new ArrayList<>();
        RoleAssignment roleAssignment = TestDataBuilder.buildRoleAssignment(Status.LIVE);
        roleAssignment.setRoleType(RoleType.ORGANISATION);
        roleAssignments.add(roleAssignment);
        Assertions.assertDoesNotThrow(() -> ValidationUtil.validateRequestedRoles(roleAssignments)
        );
    }

    @Test
    void shouldThrowInvalidRequestException_ValidateLists() {
        List<String> list = new ArrayList<>();
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.isRequestedRolesEmpty(list)
        );
    }

    @Test
    void testBuildRole() throws IOException {
        List<RoleConfigRole> roles = TestDataBuilder.buildRolesFromFile();
        assertNotNull(roles);
        assertTrue(roles.size() > 1);
    }

    @Test
    void should_validateDateTime() {
        Assertions.assertDoesNotThrow(() ->
            ValidationUtil.validateDateTime(LocalDateTime.now().plusMinutes(1).toString())
        );
    }

    @Test
    void validateDateTime_ThrowLessThanLimit() {
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateDateTime("2050-09-01T00:")
        );
    }

    @Test
    void validateDateTime_ThrowParseException() {
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateDateTime("2050-090000000000000")
        );
    }

    @Test
    void shouldValidateAssignmentRequest_clf() throws IOException, ParseException {
        Assertions.assertDoesNotThrow(() ->
            ValidationUtil.validateAssignmentRequest(TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                                            false
            ))
        );
    }

    @Test
    void shouldValidateAssignmentRequest_clt() throws IOException, ParseException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                   true);
        Assertions.assertDoesNotThrow(() ->
            ValidationUtil.validateAssignmentRequest(assignmentRequest));
    }

    @Test
    void shouldValidateAssignmentRequest_invalid_assignerId() throws IOException, ParseException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                                     true);
        assignmentRequest.getRequest().setAssignerId("@@@@");
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateAssignmentRequest(assignmentRequest));
    }

    @Test
    void shouldValidateAssignmentRequest_clf_EmptyCollection() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                                     false);
        assignmentRequest.getRequestedRoles().clear();

        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateAssignmentRequest(assignmentRequest)
        );
    }

    @Test
    void shouldValidateAssignmentRequest_clf_InvalidRoleRequests() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                                     false);
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            requestedRole.setRoleName("commander");
        }

        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateAssignmentRequest(assignmentRequest)
        );
    }

    @Test
    void shouldValidateAssignmentRequest_clf_InvalidBeginTime() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                                     false);
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            requestedRole.setBeginTime(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1L));
            requestedRole.setRoleName("solicitor");
        }


        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateAssignmentRequest(assignmentRequest)
        );
    }

    @Test
    void shouldValidateAssignmentRequest_clf_InvalidEndTime() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                                     false);
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            requestedRole.setEndTime(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1L));
        }

        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateAssignmentRequest(assignmentRequest)
        );
    }

    @Test
    void shouldValidateAssignmentRequest_clt_InvalidEndTime() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                                     false);
        for (RoleAssignment requestedRole : assignmentRequest.getRequestedRoles()) {
            requestedRole.setEndTime(ZonedDateTime.now(ZoneOffset.UTC).minusDays(1L));
        }

        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateAssignmentRequest(assignmentRequest)
        );
    }

    @Test
    void shouldThrow_ValidateAssignmentRequest_clt_Empty() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                                     true);
        assignmentRequest.getRequestedRoles().iterator().next().setProcess("");
        assignmentRequest.getRequestedRoles().iterator().next().setReference("");
        assignmentRequest.getRequest().setProcess("");
        assignmentRequest.getRequest().setReference("");

        Assertions
            .assertThrows(BadRequestException.class, () -> ValidationUtil.validateAssignmentRequest(assignmentRequest));

    }

    @Test
    void shouldThrow_ValidateAssignmentRequest_clt_processEmpty() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                                     true);
        assignmentRequest.getRequestedRoles().iterator().next().setProcess("");
        assignmentRequest.getRequestedRoles().iterator().next().setReference("ref");
        assignmentRequest.getRequest().setProcess("");
        assignmentRequest.getRequest().setReference("ref");

        Assertions
            .assertThrows(BadRequestException.class, () -> ValidationUtil.validateAssignmentRequest(assignmentRequest));
    }

    @Test
    void shouldThrow_ValidateAssignmentRequest_clt_referenceEmpty() throws IOException {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                                     true);
        assignmentRequest.getRequestedRoles().iterator().next().setProcess("pro");
        assignmentRequest.getRequestedRoles().iterator().next().setReference("");
        assignmentRequest.getRequest().setProcess("pro");
        assignmentRequest.getRequest().setReference("");

        Assertions
            .assertThrows(BadRequestException.class, () -> ValidationUtil.validateAssignmentRequest(assignmentRequest));
    }

    @Test
    void shouldValidateCaseId() {
        ValidationUtil.validateCaseId("1234567890123456");
    }

    @Test
    void shouldThrow_ValidateCaseId() {
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateCaseId("1234567890123")
        );
    }

    @Test
    void shouldValidateDateOrder() throws ParseException {
        String beginTime = LocalDateTime.now().plusDays(1).toString();
        String endTime = LocalDateTime.now().plusDays(14).toString();
        Assertions.assertDoesNotThrow(() ->
            ValidationUtil.compareDateOrder(beginTime, endTime)
        );
    }

    @Test
    void shouldThrow_ValidateDateOrder_BeginTimeBeforeCurrent() {
        String beginTime = LocalDateTime.now().minusDays(1).toString();
        String endTime = LocalDateTime.now().minusDays(2).toString();
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.compareDateOrder(beginTime,endTime)
        );
    }

    @Test
    void shouldThrow_ValidateDateOrder_EndTimeBeforeCurrent() {
        String beginTime = LocalDateTime.now().plusDays(14).toString();
        String endTime = LocalDateTime.now().minusDays(1).toString();
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.compareDateOrder(beginTime,endTime)
        );
    }

    @Test
    void shouldThrow_ValidateDateOrder_EndTimeBeforeBegin() {
        String beginTime = LocalDateTime.now().plusDays(14).toString();
        String endTime = LocalDateTime.now().plusDays(10).toString();
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.compareDateOrder(beginTime,endTime)
        );
    }

    @Test
    void shouldThrowBad_ValidateEnumField() {
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.compareRoleType("SecretAgent")
        );
    }

    @Test
    void shouldValidateEnumField() {
        Assertions.assertDoesNotThrow(() -> {
            ValidationUtil.compareRoleType("Case");
            ValidationUtil.compareRoleType("Organisation");
        });
    }

    @Test
    void shouldThrowBadReq_invalidCaseId() {
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateCaseId("123456789012345A")
        );
    }

    @Test
    void shouldThrowBadReq_invalidCaseIdLength() {
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateCaseId("123456789012345")
        );
    }

    @Test
    void shouldThrowBadReq_sanitiseCorrelationId_input_not_null_invalid() {
        Assertions.assertThrows(BadRequestException.class, () ->
            ValidationUtil.validateCaseId("123456789012345")
        );
    }
}
