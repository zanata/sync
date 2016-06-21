package org.zanata.sync.model;

import java.io.Serializable;

import org.zanata.sync.dto.JobRunStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * TODO move to dto package
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JobSummary implements Serializable {
    private String jobKey;
    private Long workId;
    private String name;
    private String description;
    private JobType type;
    private JobRunStatus lastJobStatus;
}
