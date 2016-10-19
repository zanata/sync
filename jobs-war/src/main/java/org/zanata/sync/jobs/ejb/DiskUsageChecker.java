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
package org.zanata.sync.jobs.ejb;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.enterprise.event.Observes;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.sync.jobs.utils.ProcessUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class DiskUsageChecker {
    private static final Logger log =
            LoggerFactory.getLogger(DiskUsageChecker.class);
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("\\s+");

    @Resource(lookup = "java:jboss/mail/Default")
    private Session session;

    @Resource
    TimerService timerService;

    @PostConstruct
    public void setUp() {
        if (ProcessUtils.isExecutableOnPath("quota")) {
            timerService.createTimer(0, TimeUnit.MINUTES.toMillis(1),
                    "Created new disk usage checker timer");
        } else {
            log.warn("quota is not on the system PATH. Will not check disk usage");
        }
    }

    public void start() {
        // so that this EJB can be instantiated and the programmatic scheduler can start
    }

    @Timeout
    public void onTimeout(Timer timer) {
        checkDiskUsage();
    }

    public void checkDiskUsage() {
        // linux quota with no wrap option
        List<String> output = ProcessUtils
                .runNativeCommand(Paths.get(System.getProperty("user.home")),
                        TimeUnit.MINUTES.toMillis(1),
                        "quota", "-w");

        /*
Disk quotas for user 579eb79d5110e2628b0000cc (uid 3356):
     Filesystem  blocks   quota   limit   grace   files   quota   limit   grace
     /dev/xvde1       0       0 1048576               1       0   80000
/dev/mapper/luks-openshift  304624       0 1048576            1344       0   80000
         */

        // only the last line is relevant
        String lastLine =
                output.size() > 1 ? output.get(output.size() - 1) : "";
        List<String> values = Splitter.on(SEPARATOR_PATTERN).omitEmptyStrings()
                .splitToList(lastLine);
        log.info("==== DISK ===== \n{}",
                Joiner.on(System.lineSeparator()).join(output));

        if (values.size() != 7) {
            log.warn("can not interpret quota output: {}", output);
        } else {
            try {
                long diskUsage = Long.parseLong(values.get(1));
                long diskLimit = Long.parseLong(values.get(3));
                long files = Long.parseLong(values.get(4));
                long filesLimit = Long.parseLong(values.get(6));

                int usagePercent = percent(diskUsage, diskLimit);
                int filesPercent = percent(files, filesLimit);
                if (usagePercent > 80 || filesPercent > 80) {
                    log.warn(
                            "disk usage percent: {}%. files number percent: {}%",
                            usagePercent, filesPercent);
                    sendMail(diskUsage, diskLimit, files, filesLimit);
                }
            } catch (Exception e) {
                log.error("unable to parse output: {}", lastLine, e);
            }
        }
    }

    private void sendMail(long diskUsage,
            long diskLimit,
            long files,
            long filesLimit) {

        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.addFrom(new Address[]{
                    new InternetAddress("zanata-sync@zanata.org",
                            "Zanata Sync System") });
            mimeMessage.addRecipients(Message.RecipientType.TO,
                    "pahuang@redhat.com");
            mimeMessage
                    .setSubject("Zanata Sync jobs war disk usage is too high");
            mimeMessage.setText(String.format(
                    "Disk Usage: %d, Disk limit: %d; File Usage: %d, File Limit: %d",
                    diskUsage, diskLimit, files, filesLimit));

            Transport.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error(
                    "disk usage alert: Disk usage: {}, Disk limit: {}, Files: {}, Files limit: {}",
                    diskUsage, diskLimit, files, filesLimit);
            log.error("failed to send email", e);
//            throw Throwables.propagate(e);
        }
    }

    private static int percent(long usage, long limit) {
        return (int) (usage * 100.0 / limit);
    }

}
