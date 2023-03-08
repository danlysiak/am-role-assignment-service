@F-015
Feature: F-015 : Create Case Role Assignments for Privatelaw

  Background:
    Given an appropriate test context as detailed in the test data source


    @S-223
     Scenario: must successfully create allocated-magistrate case role
       Given a user with [an active IDAM profile with full permissions],
       And a user [Befta3 - who is the actor for requested role],
       And a successful call [to create org role assignments for actors & requester] as in [S-223_Org_Role_Creation],
       When a request is prepared with appropriate values,
       And the request [contains ReplaceExisting is false and reference set to caseId],
       And the request [contains allocated-magistrate role assignment],
       And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
       Then a positive response is received,
       And the response has all other details as expected,
       And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
       And a successful call [to delete role assignments just created above] as in [S-223_DeleteDataForRoleAssignmentsForOrgRoles].

    @S-224
     Scenario: must successfully create hearing-judge case role
       Given a user with [an active IDAM profile with full permissions],
       And a user [Befta3 - who is the actor for requested role],
       And a successful call [to create org role assignments for actors & requester] as in [S-224_Org_Role_Creation],
       When a request is prepared with appropriate values,
       And the request [contains ReplaceExisting is false and reference set to caseId],
       And the request [contains hearing-judge case role assignment],
       And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
       Then a positive response is received,
       And the response has all other details as expected,
       And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
       And a successful call [to delete role assignments just created above] as in [S-224_DeleteDataForRoleAssignmentsForOrgRoles].



