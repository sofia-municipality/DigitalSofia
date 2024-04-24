"""This exposes BPM Service."""

from enum import IntEnum
from typing import Dict

from flask import current_app

from .base_bpm import BaseBPMService


class BPMEndpointType(IntEnum):
    """This enum provides the list of bpm endpoints type."""

    PROCESS_DEFINITION = 1
    TASK = 3
    HISTORY = 4
    PROCESS_DEFINITION_XML = 6
    MESSAGE_EVENT = 7
    PROCESS_INSTANCE = 8
    FORM_AUTH_DETAILS = 9
    PROCESS_VARIABLES = 10
    DEFAULT_PROCESS_INSTANCE = 11
    DEFAULT_TASK = 12


class BPMService(BaseBPMService):
    """This class manages all of the Camunda BPM Service."""

    @classmethod
    def get_all_process(cls, token):
        """Get all process."""
        url = cls._get_url_(BPMEndpointType.PROCESS_DEFINITION) + "?latestVersion=true"
        current_app.logger.debug(url)
        return cls.get_request(url, token)

    @classmethod
    def get_process_instance(cls, process_instance_id:str, token:str = None):
        url = (
            f"{cls._get_url_(BPMEndpointType.PROCESS_INSTANCE)}"
            f"{process_instance_id}/activity-instances"
        )

        return cls.get_request(url, token)

    @classmethod
    def get_process_details_by_key(cls, process_key, token):
        """Get process details."""
        current_app.logger.debug(
            "Getting process details. Process Key : %s", process_key
        )
        for process_definition in cls.get_all_process(token):
            if process_definition.get("key") == process_key:
                current_app.logger.debug(
                    "Found Process Definition. process_definition : %s",
                    process_definition,
                )
                return process_definition
        return None

    @classmethod
    def get_process_definition_xml(cls, process_key, token):
        """Get process details XML."""
        current_app.logger.debug(f"process detail xml process-key>> {process_key}")
        url = (
            cls._get_url_(BPMEndpointType.PROCESS_DEFINITION_XML) + process_key + "/xml"
        )
        current_app.logger.debug(f"process def url>> {url}")
        return cls.get_request(url, token)

    @classmethod
    def post_process_start(cls, process_key, payload, token, tenant_key):
        """Post process start."""
        url = (
            f"{cls._get_url_(BPMEndpointType.PROCESS_DEFINITION)}/"
            f"key/{process_key}/start"
        )
        return cls.post_request(url, token, payload=payload, tenant_key=tenant_key)

    @classmethod
    def post_process_start_tenant(
        cls, process_key: str, payload: Dict, token: str, tenant_key: str
    ):
        """Post process start based on tenant key."""
        url = (
            f"{cls._get_url_(BPMEndpointType.PROCESS_DEFINITION)}/"
            f"key/{process_key}/start?tenantId=" + tenant_key
        )
        return cls.post_request(url, token, payload=payload)

    @classmethod
    def get_auth_form_details(cls, token):
        """Get authorized form details."""
        url = cls._get_url_(BPMEndpointType.FORM_AUTH_DETAILS)
        return cls.get_request(url, token)

    @classmethod
    def get_process_instance_tasks(cls, process_instance_id, token):
        url = cls._get_url_(BPMEndpointType.TASK)
        url += f'?processInstanceId={process_instance_id}'
        return cls.get_request(url, token)

    @classmethod
    def get_all_tasks(cls, token):
        """Get all tasks."""
        url = cls._get_url_(BPMEndpointType.HISTORY)
        return cls.get_request(url, token)

    @classmethod
    def get_task(cls, task_id, token:str = None):
        """Get task."""
        url = cls._get_url_(BPMEndpointType.DEFAULT_TASK) + task_id
        return cls.get_request(url, token)

    @classmethod
    def get_task_variables(cls, task_id, token):
        """Get task variables."""
        url = cls._get_url_(BPMEndpointType.TASK) + task_id + "/variables"
        return cls.get_request(url, token)

    def get_process_instance_variable(cls, process_instance_id, token, variable_name):
        url = (
            cls._get_url_(BPMEndpointType.DEFAULT_PROCESS_INSTANCE)
            + process_instance_id
            + "/variables"
            + f"/{variable_name}"
        )

        return cls.get_request(url, token)

    @classmethod
    def claim_task(cls, task_id, data, token):
        """Claim a task."""
        url = cls._get_url_(BPMEndpointType.TASK) + task_id + "/claim"
        return cls.post_request(url, token, data)

    @classmethod
    def unclaim_task(cls, task_id, data, token):
        """Unclaim a task."""
        url = cls._get_url_(BPMEndpointType.TASK) + task_id + "/unclaim"
        return cls.post_request(url, token, data)

    @classmethod
    def complete_task(cls, task_id, data, token):
        """Complete a task."""
        url = cls._get_url_(BPMEndpointType.TASK) + task_id + "/complete"
        return cls.post_request(url, token, data)

    @classmethod
    def trigger_notification(cls, token):
        """Submit a form."""
        url = cls._get_url_(BPMEndpointType.PROCESS_DEFINITION) + "process/start"
        return cls.post_request(url, token)

    @classmethod
    def send_message(cls, data, token):
        """Correlate a Message."""
        url = cls._get_url_(BPMEndpointType.MESSAGE_EVENT)
        return cls.post_request(url, token, data)

    @classmethod
    def get_process_activity_instances(cls, process_instance_id, token):
        """Get task."""
        url = (
            cls._get_url_(BPMEndpointType.PROCESS_INSTANCE)
            + process_instance_id
            + "/activity-instances"
        )
        return cls.get_request(url, token)
    
    @classmethod
    def get_process_variables(cls, process_instance_id, token):
        url = (
            cls._get_url_(BPMEndpointType.PROCESS_INSTANCE)
            + process_instance_id
            + "/variables"
        )
        return cls.get_request(url, token)
    
    @classmethod
    def update_process_variables(cls, process_instance_id, token, modifications={}, deletions=[]):
        url = (
            cls._get_url_(BPMEndpointType.DEFAULT_PROCESS_INSTANCE)
            + process_instance_id
            + "/variables"
        )

        data = {
            "modifications": modifications,
            "deletions": deletions
        }

        return cls.post_request(url, token, payload=data)
    
    def create_process_variable(cls, process_instance_id, token, variable_name, payload):
        url = (
            cls._get_url_(BPMEndpointType.DEFAULT_PROCESS_INSTANCE)
            + process_instance_id
            + "/variables"
            + f"/{variable_name}"
        )

        return cls.put_request(url, token=token, payload=payload)

    @classmethod
    def post_identity_links(cls, process_instance_id, token, data):
        url = (
            cls._get_url_(BPMEndpointType.TASK)
            + process_instance_id
            + "/identity-links"
        )

        return cls.post_request(url, token, data)
    
    @classmethod
    def get_identity_links(cls, process_instance_id, token):
        url = (
            cls._get_url_(BPMEndpointType.TASK)
            + process_instance_id
            + "/identity-links"
        )

        return cls.get_request(url, token)
    
    @classmethod
    def delete_identity_links(cls, process_instance_id, token, data):
        url = (
            cls._get_url_(BPMEndpointType.TASK)
            + process_instance_id
            + "/identity-links"
            + "/delete"
        )

        return cls.post_request(url, token, data)

    @classmethod
    def delete_process_instance(cls, process_instance_id, token):
        url = (
            cls._get_url_(BPMEndpointType.DEFAULT_PROCESS_INSTANCE)
            + f"{process_instance_id}/"
        )

        return cls.delete_request(url, token)

    @classmethod
    def resolve_task(cls, task_id:str, data:dict = {}, token:str = None):
        url = (
            cls._get_url_(BPMEndpointType.DEFAULT_TASK)
            + task_id
            + "/resolve"
        )

        return cls.post_request(url, token=token, payload={"variables": data})

    @classmethod
    def _get_url_(cls, endpoint_type: BPMEndpointType):
        """Get Url."""
        bpm_api_base = current_app.config.get("BPM_API_URL")
        try:
            if endpoint_type == BPMEndpointType.PROCESS_DEFINITION:
                url = f"{bpm_api_base}/engine-rest-ext/v1/process-definition"
            elif endpoint_type == BPMEndpointType.FORM_AUTH_DETAILS:
                url = f"{bpm_api_base}/engine-rest-ext/v1/admin/form/authorization"
            elif endpoint_type == BPMEndpointType.HISTORY:
                url = f"{bpm_api_base}/engine-rest-ext/v1/task/"
            elif endpoint_type == BPMEndpointType.TASK:
                url = f"{bpm_api_base}/engine-rest-ext/v1/task/"
            elif endpoint_type == BPMEndpointType.PROCESS_DEFINITION_XML:
                url = f"{bpm_api_base}/engine-rest-ext/v1/process-definition/key/"
            elif endpoint_type == BPMEndpointType.MESSAGE_EVENT:
                url = f"{bpm_api_base}/engine-rest-ext/v1/message/"
            elif endpoint_type == BPMEndpointType.PROCESS_INSTANCE:
                url = f"{bpm_api_base}/engine-rest-ext/v1/process-instance/"
            elif endpoint_type == BPMEndpointType.DEFAULT_PROCESS_INSTANCE:
                url = f"{bpm_api_base}/engine-rest-ext/process-instance/"
            elif endpoint_type == BPMEndpointType.DEFAULT_TASK:
                url = f"{bpm_api_base}/engine-rest-ext/task/"
            return url

        except BaseException:  # pylint: disable=broad-except
            return {
                "type": "Environment missing",
                "message": "Missing environment variable BPM_API_URL",
            }
        

    @classmethod
    def get_apps_not_completed_for_child(cls, personIdentifier:str, tenantId:str, definitionId:str):
        """
            To find if there is at least one application for adderss change/registration for child is started but not completed:
            - Filter by EGN: personIdentifier_like_%-{egn}
            - Application status must not be 'Completed': applicationStatus_neq_Completed
            - Application must be on behalf of the child: behalf_eq_child
            - Filter by Tenant ID: tenantIdIn={tenantId}
            - Filter by processDefinitionId: Process_sofiade:66:ab532399-cf36-11ee-80ed-e20c45ea0eb8
        """

        if not personIdentifier:
            current_app.logger.error("personIdentifier is required for BPMService.get_apps_not_completed_for_child")
            return {
                "success": False,
                "error": "personIdentifier is required for BPMService.get_apps_not_completed_for_child"
            }


        if not tenantId:
            current_app.logger.error("tenantId is required for BPMService.get_apps_not_completed_for_child")
            return {
                "success": False,
                "error": "tenantId is required for BPMService.get_apps_not_completed_for_child"
            }            

        if not definitionId:
            current_app.logger.error("definitionId is required for BPMService.get_apps_not_completed_for_child")
            return {
                "success": False,
                "error": "definitionId is required for BPMService.get_apps_not_completed_for_child"
            }            

        # pnobg-9308241146
        egn = personIdentifier[6:]

        if len(egn) != 10:
            current_app.logger.error(f"Invalid EGN is provided: {egn}")
            return {
                "success": False,
                "error": f"Invalid EGN is provided: {egn}"
            }   

        url = cls._get_url_(BPMEndpointType.DEFAULT_PROCESS_INSTANCE) + f"?variables=personIdentifier_like_%-{egn},applicationStatus_neq_Completed,behalf_eq_child&&tenantIdIn={tenantId}&processDefinitionId={definitionId}"

        current_app.logger.debug(url)

        data = cls.get_request(url, token = None)

        current_app.logger.debug(data) 

        return{
            "success": True,
            "data": data
        }
