package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;

@Slf4j
@Service
public class CreateRoleAssignmentOrchestrator {

    private ParseRequestService parseRequestService;
    private PrepareResponseService prepareResponseService;
    private CreateRoleAssignmentService createRoleAssignmentService;
    Request request;
    RequestEntity requestEntity;

    public CreateRoleAssignmentOrchestrator(ParseRequestService parseRequestService,
                                            PrepareResponseService prepareResponseService,
                                            CreateRoleAssignmentService createRoleAssignmentService) {
        this.parseRequestService = parseRequestService;
        this.prepareResponseService = prepareResponseService;
        this.createRoleAssignmentService = createRoleAssignmentService;
    }

    public ResponseEntity<Object> createRoleAssignment(AssignmentRequest roleAssignmentRequest) throws ParseException {

        AssignmentRequest existingAssignmentRequest = null;

        //1. call parse request service
        AssignmentRequest parsedAssignmentRequest = parseRequestService
            .parseRequest(roleAssignmentRequest, RequestType.CREATE);
        //2. Call persistence service to store only the request
        requestEntity = createRoleAssignmentService.persistInitialRequest(parsedAssignmentRequest.getRequest());
        requestEntity.setHistoryEntities(new HashSet<>());
        request = parsedAssignmentRequest.getRequest();
        request.setId(requestEntity.getId());
        createRoleAssignmentService.setRequestEntity(requestEntity);
        createRoleAssignmentService.setIncomingRequest(request);

        //Check replace existing true/false
        if (request.isReplaceExisting()) {

            //retrieve existing assignments and prepared temp request
            existingAssignmentRequest = createRoleAssignmentService
                .retrieveExistingAssignments(parsedAssignmentRequest);

            // compare identical existing and incoming requested roles based on some attributes
            try {
                if (createRoleAssignmentService.hasAssignmentsUpdated(
                    existingAssignmentRequest,
                    parsedAssignmentRequest
                )) {

                    //update the existingAssignmentRequest with Only need to be removed record
                    if (!createRoleAssignmentService.needToDeleteRoleAssignments.isEmpty()) {
                        createRoleAssignmentService.updateExistingAssignmentWithNewDeleteRoleAssignments(
                            existingAssignmentRequest);
                    }

                    //update the parsedAssignmentRequest with Only new record
                    if (!createRoleAssignmentService.needToCreateRoleAssignments.isEmpty()) {
                        createRoleAssignmentService.updateParseRequestWithNewCreateRoleAssignments(
                            existingAssignmentRequest,
                            parsedAssignmentRequest
                        );


                    } else {
                        parsedAssignmentRequest.setRequestedRoles(Collections.emptyList());

                    }

                    //Checking all assignments has DELETE_APPROVED status to create new entries of assignment records
                    createRoleAssignmentService.checkAllDeleteApproved(
                        existingAssignmentRequest,
                        parsedAssignmentRequest
                    );

                } else {
                    createRoleAssignmentService.duplicateRequest(existingAssignmentRequest, parsedAssignmentRequest);

                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("context", e);
            }

        } else {
            //Save requested role in history table with CREATED and Approved Status
            createRoleAssignmentService.createNewAssignmentRecords(parsedAssignmentRequest);
            createRoleAssignmentService.checkAllApproved(parsedAssignmentRequest);

        }


        //8. Call the persistence to copy assignment records to RoleAssignmentLive table
        if (!createRoleAssignmentService.needToCreateRoleAssignments.isEmpty()
            && createRoleAssignmentService.needToRetainRoleAssignments.size() > 0) {
            parsedAssignmentRequest.getRequestedRoles().addAll(createRoleAssignmentService.needToRetainRoleAssignments);
        } else if (createRoleAssignmentService.needToRetainRoleAssignments.size() > 0) {
            parsedAssignmentRequest.setRequestedRoles(createRoleAssignmentService.needToRetainRoleAssignments);
        }


        ResponseEntity<Object> result = prepareResponseService.prepareCreateRoleResponse(parsedAssignmentRequest);

        parseRequestService.removeCorrelationLog();
        return result;
    }


}




