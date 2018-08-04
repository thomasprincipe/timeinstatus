package com.tapdevelopment.jira.web.contextproviders;


import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeInStatusContextProvider extends AbstractJiraContextProvider
{

    public Map<String, Object> getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper)
    {
        WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
        Map<String, Object> contextMap = new HashMap<>();
		Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");

        JiraWorkflow workflow = workflowManager.getWorkflow((currentIssue));
        List<Status> statusList = workflow.getLinkedStatusObjects();

        ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager();
        List<ChangeItemBean> changeBeanList = changeHistoryManager.getChangeItemsForField(currentIssue, "status");

        Map<String, Long> statusMap = processChangeHistory( currentIssue, changeBeanList );
        Map<String, String> statusMapString = convertToTimeString( statusMap );

        contextMap.put( "statuses", statusMapString );
        return contextMap;
    }

    private Map<String, Long> processChangeHistory( Issue issue, List<ChangeItemBean> beanList )
    {
        // Map<Status Name, Time>
        Map<String, Long> statusMap = new HashMap<>();
        Long changeTime = issue.getCreated().getTime();
        if( !beanList.isEmpty() ) {
            for (ChangeItemBean bean : beanList) {
                changeTime = processChangeBean( bean, statusMap, changeTime );
            }
        }

        // Current status is not part of change history
        Status status = issue.getStatus();
        String statusStr = status.getName();
        Long currentTime = System.currentTimeMillis();
        Long addTime = currentTime - changeTime;
        if( ! statusMap.containsKey(statusStr) ) {
            statusMap.put( statusStr, addTime );
        }
        else {
            Long existingTime = statusMap.get(statusStr);
            statusMap.put(statusStr, existingTime + addTime);
        }

        return statusMap;
    }

    private Long processChangeBean( ChangeItemBean bean, Map<String, Long>  map, Long lastChangeTime )
    {
        String fromString = bean.getFromString();
        Long changeTime = bean.getCreated().getTime();
        // If this is a new status
        Long addTime = changeTime - lastChangeTime;
        if( ! map.containsKey(fromString) ) {
            map.put( fromString, addTime );
        }
        else {
            Long existingTime = map.get(fromString);
            map.put(fromString, existingTime + addTime);
        }
        return changeTime;
    }

    private Map<String,String> convertToTimeString( Map<String, Long>  map )
    {
        Map<String, String> statusMap = new HashMap<>();
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            Long totalTime = entry.getValue();
            String stringTime = convertTimeToString( totalTime );
            statusMap.put(entry.getKey(),stringTime);
        }
        return statusMap;
    }

    private String convertTimeToString(Long time){
        Long origTime = time;
        time = time / 1000;
        String timeString;
        Long days = time / (24*60*60);
        time = time - (days*24*60*60);
        Long hours = time / (60*60);
        time = time - (hours*60*60);
        Long minutes = time / 60;
        time = time - (minutes*60);
        String daysStr = days.toString();
        String hourStr = hours.toString();
        String minStr = minutes.toString();
        String secStr = time.toString();
        return daysStr + "D " + hourStr + "H" + " " + minStr + "M " + secStr + "s     " + origTime ;


    }
}




