/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.sync.service;

import javax.websocket.Session;

import org.quartz.SchedulerException;
import org.quartz.UnableToInterruptJobException;
import org.zanata.sync.dto.JobRunStatus;
import org.zanata.sync.exception.JobNotFoundException;
import org.zanata.sync.model.JobType;
import org.zanata.sync.model.SyncWorkConfig;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public interface SchedulerService {

    void scheduleWork(SyncWorkConfig syncWorkConfig)
            throws SchedulerException;

    void rescheduleWork(SyncWorkConfig syncWorkConfig)
            throws SchedulerException;

    void cancelRunningJob(Long id, JobType type)
            throws UnableToInterruptJobException, JobNotFoundException;

    void deleteJob(Long id, JobType type)
            throws SchedulerException;

    void disableJob(Long id, JobType type) throws SchedulerException;

    void enableJob(Long id, JobType type) throws SchedulerException;

    /**
     * trigger a job
     *
     * @param id
     *         config id
     * @param type
     *         job type
     * @return true if the job is triggered of false if the job is already
     * running.
     * @throws JobNotFoundException
     * @throws SchedulerException
     */
    boolean triggerJob(Long id, JobType type)
            throws JobNotFoundException, SchedulerException;

    void addWebSocketSession(Session session);

    void removeWebSocketSession(Session session);

    /**
     * Publish websocket event
     * @param status
     */
    void publishEvent(JobRunStatus status);
}
