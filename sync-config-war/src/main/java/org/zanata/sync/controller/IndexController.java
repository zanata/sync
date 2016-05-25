package org.zanata.sync.controller;

import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import org.zanata.sync.api.JobResource;
import org.zanata.sync.api.WorkResource;
import org.zanata.sync.model.JobStatusType;
import org.zanata.sync.model.JobSummary;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.WorkSummary;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("indexController")
@ViewScoped
@Slf4j
public class IndexController implements Serializable {

    @Inject
    private WorkResource workResourceImpl;

    @Inject
    private JobResource jobResource;

    public List<WorkSummary> getAllWork() {
        Response response = workResourceImpl.getWork("", "summary");
        return (List<WorkSummary>)response.getEntity();
    }

    public List<JobSummary> getRunningJobs() {
        Response response =
                jobResource.getJob(null, null, JobStatusType.RUNNING);
        return (List<JobSummary>) response.getEntity();
    }

    /**
     * Cancel running job
     * @param id
     * @param type - SyncConfig.Type
     */
    public void cancelRunningJob(String id, String type) {
        jobResource.cancelRunningJob(id, JobType.valueOf(type));
    }
}
