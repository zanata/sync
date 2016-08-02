/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.sync.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.enterprise.context.Dependent;

import org.zanata.sync.model.SyncWorkConfig;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Dependent
public class SyncWorkConfigSerializerImpl implements SyncWorkConfigSerializer {

    @Override
    public SyncWorkConfig fromYaml(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
//            YAML.setBeanAccess(BeanAccess.FIELD);
//            SyncWorkConfig config = (SyncWorkConfig) YAML.load(
//                    inputStream);
            return null;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String toYaml(SyncWorkConfig syncWorkConfig) {
//        YAML.setBeanAccess(BeanAccess.FIELD);
//        return YAML.dump(syncWorkConfig);
        return null;
    }

    @Override
    public SyncWorkConfig fromYaml(String yaml) {
//        YAML.setBeanAccess(BeanAccess.FIELD);
//        return (SyncWorkConfig) YAML.load(yaml);
        return null;
    }
}
