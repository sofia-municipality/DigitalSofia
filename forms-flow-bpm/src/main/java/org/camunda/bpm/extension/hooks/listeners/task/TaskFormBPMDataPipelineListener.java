package org.camunda.bpm.extension.hooks.listeners.task;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.extension.hooks.listeners.BaseListener;
import org.camunda.bpm.extension.hooks.services.FormSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.io.IOException;
import java.util.Map;

import static org.camunda.bpm.extension.commons.utils.VariableConstants.FORM_URL;
import static org.camunda.bpm.extension.commons.utils.VariableConstants.TASK_FORM_URL;

/**
 * TaskFormBPMDataPipelineListener.
 * This class copies all the task form document data into CAM variables.
 */
@Named("TaskFormBPMDataPipelineListener")
public class TaskFormBPMDataPipelineListener extends BaseListener implements TaskListener, ExecutionListener {

    @Autowired
    private FormSubmissionService formSubmissionService;

    @Override
    public void notify(DelegateExecution execution) {
        try {
            syncFormVariables(execution);
        } catch (IOException e) {
            handleException(execution, ExceptionSource.EXECUTION, e);
        }
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            syncFormVariables(delegateTask.getExecution());
        } catch (IOException e) {
            handleException(delegateTask.getExecution(), ExceptionSource.TASK, e);
        }
    }

    private void syncFormVariables(DelegateExecution execution) throws IOException {
        Map<String,Object> dataMap = formSubmissionService.retrieveFormValues(String.valueOf(execution.getVariablesLocal().get(TASK_FORM_URL)));
        for (Map.Entry<String, Object> entry: dataMap.entrySet()) {
            execution.setVariable(entry.getKey(), entry.getValue());
        }
    }
}
