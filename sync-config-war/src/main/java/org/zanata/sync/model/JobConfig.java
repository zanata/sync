package org.zanata.sync.model;

import java.io.Serializable;
import java.util.Objects;

import org.zanata.sync.common.model.SyncOption;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@ToString
@NoArgsConstructor
public class JobConfig implements Serializable {

    private JobType type;
    /**
     * see http://en.wikipedia.org/wiki/Cron#CRON_expression
     */
    private String cron;
    private SyncOption option;

    public JobConfig(JobType type, String cron, SyncOption option) {
        this.type = type;
        this.cron = cron;
        this.option = option;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobConfig jobConfig = (JobConfig) o;
        return type == jobConfig.type &&
                Objects.equals(cron, jobConfig.cron) &&
                option == jobConfig.option;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, cron, option);
    }
}
