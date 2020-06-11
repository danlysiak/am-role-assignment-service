package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

@Service
public class ParseRequestService {

    public static final String ATTRIBUTES = "attributes";

    public boolean parseRequest(AssignmentRequest assignmentRequest) {
        ValidationUtil.validateNumberTextField(assignmentRequest.request.correlationId);
        ValidationUtil.validateNumberTextField(assignmentRequest.request.clientId);
        ValidationUtil.validateNumberTextField(assignmentRequest.request.authenticatedUserId);
        ValidationUtil.validateNumberTextField(assignmentRequest.request.requestorId);
        ValidationUtil.validateTextField(assignmentRequest.request.requestType.toString());

        ValidationUtil.validateLists(assignmentRequest.requestedRoles);

        for (RequestedRole requestedRole: assignmentRequest.requestedRoles) {
            ValidationUtil.validateUuidField(requestedRole.getId());
            ValidationUtil.validateUuidField(requestedRole.getActorId());

            ValidationUtil.validateTextField(requestedRole.getActorIdType().toString());
            ValidationUtil.validateTextField(requestedRole.getRoleType().toString());
            ValidationUtil.validateTextField(requestedRole.getRoleName());
            ValidationUtil.validateTextField(requestedRole.getClassification().toString());
            ValidationUtil.validateTextField(requestedRole.getGrantType().toString());

            ValidationUtil.validateTextField(requestedRole.getAttributes().get(ATTRIBUTES).get("jurisdiction").asText());
            ValidationUtil.validateTextHyphenField(requestedRole.getAttributes().get(ATTRIBUTES).get("region").asText());
            ValidationUtil.validateTextField(requestedRole.getAttributes().get(ATTRIBUTES).get("contractType").asText());
        }
        return Boolean.TRUE;
    }
}
