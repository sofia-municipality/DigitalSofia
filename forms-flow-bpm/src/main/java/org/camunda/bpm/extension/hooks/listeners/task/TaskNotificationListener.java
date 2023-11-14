package org.camunda.bpm.extension.hooks.listeners.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.*;
import org.camunda.bpm.extension.hooks.listeners.BaseListener;
import org.camunda.bpm.extension.hooks.services.NotificationServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@Slf4j
@RequiredArgsConstructor
@Named("NotificationListener")
@Scope("prototype")
public class TaskNotificationListener extends BaseListener implements ExecutionListener, TaskListener {
    private final String TASK_ID = "taskId";
    private final String TASK_URL = "taskUrl";
    private final String WEB_URL = "webUrl";

    @Value("${camunda.bpm.webapp.base-app-url}")
    private String camundaAppBaseUrl;

    private final NotificationServiceImpl notificationService;
    private Expression formTemplatePath;
    private Expression recipientEmail;
    private Expression attachmentUrl;

    @Override
    public void notify(DelegateExecution execution) {
        String formTemplatePath = "";
        String recipientEmail = "";
        String attachmentUrl = "";
        Map<String, Object> variables = new HashMap<>();
        try {
            formTemplatePath = (String) this.getFormTemplatePath().getValue(execution);
            recipientEmail = (String) this.getRecipientEmail().getValue(execution);
            if (this.getAttachmentUrl() != null) {
                attachmentUrl = (String) this.getAttachmentUrl().getValue(execution);
            }
            variables = execution.getVariables();
            variables.put(WEB_URL, camundaAppBaseUrl);
            Map<String, Object> localVariables = execution.getVariablesLocal();
            variables.putAll(localVariables);
        } catch (Exception e) {
            handleException(execution, ExceptionSource.TASK, e);
        }
        try{
            notificationService.sendMailNotification("/" + formTemplatePath, recipientEmail, attachmentUrl, variables);
        } catch (Exception exception) {
            handleException(execution, ExceptionSource.EXECUTION, exception);
        }
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        String formTemplatePath = "";
        String recipientEmail = "";
        String attachmentUrl = "";
        Map<String, Object> variables = new HashMap<>();
        try {
            formTemplatePath = (String) this.getFormTemplatePath().getValue(delegateTask.getExecution());
            recipientEmail = (String) this.getRecipientEmail().getValue(delegateTask.getExecution());
            if (this.getAttachmentUrl() != null) {
                attachmentUrl = (String) this.getAttachmentUrl().getValue(delegateTask.getExecution());
            }
            variables = delegateTask.getExecution().getVariables();
            String taskId = delegateTask.getId();
            String taskUrl = camundaAppBaseUrl + "/user-task/" + taskId;
            variables.put(TASK_ID, taskId);
            variables.put(TASK_URL, taskUrl);
            variables.put(WEB_URL, camundaAppBaseUrl);
            Map<String, Object> localVariables = delegateTask.getExecution().getVariablesLocal();
            variables.putAll(localVariables);
        } catch (Exception e) {
            handleException(delegateTask.getExecution(), ExceptionSource.TASK, e);
        }
        try{
            notificationService.sendMailNotification("/" + formTemplatePath, recipientEmail, attachmentUrl, variables);
        } catch (Exception exception) {
            handleException(delegateTask.getExecution(), ExceptionSource.EXECUTION, exception);
        }
    }
}
