package org.zanata.sync.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class JobProgress implements Serializable {

    private double completePercent;
    private String description;
    private JobStatusType status;
}
