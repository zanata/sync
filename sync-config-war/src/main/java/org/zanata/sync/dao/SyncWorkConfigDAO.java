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

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.zanata.sync.model.SyncWorkConfig;
import com.google.common.collect.Lists;


/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Alternative
@RequestScoped
public class SyncWorkConfigDAO implements Repository<SyncWorkConfig, Long> {
    @Inject
    private Connection connection;

    @Inject
    private SyncWorkConfigSerializer serializer;

    @Override
    public Optional<SyncWorkConfig> load(Long id) {
       /* DSLContext dslContext = DSL.using(connection, SQLDialect.H2);

        Record record = dslContext.select().from(SYNC_WORK_CONFIG_TABLE).where(
                SYNC_WORK_CONFIG_TABLE.ID.equal(id)).fetchOne();
        if (record != null) {
            String yaml = record.getValue(SYNC_WORK_CONFIG_TABLE.YAML);
            return Optional.of(serializer.fromYaml(yaml));
        }*/
        return Optional.empty();
    }

    @Override
    public void persist(SyncWorkConfig config) {
        /*DSLContext dslContext = DSL.using(connection, SQLDialect.H2);

        if (config.getId() == null) {
            SyncWorkConfigTableRecord savedRecord =
                    dslContext.insertInto(SYNC_WORK_CONFIG_TABLE,
                            SYNC_WORK_CONFIG_TABLE.NAME,
                            SYNC_WORK_CONFIG_TABLE.DESCRIPTION,
                            SYNC_WORK_CONFIG_TABLE.CREATEDDATE,
                            SYNC_WORK_CONFIG_TABLE.YAML)
                            .values(config.getName(), config.getDescription(),
                                    new Timestamp(new Date().getTime()), "")
                            .returning(SYNC_WORK_CONFIG_TABLE.ID)
                            .fetchOne();
            // we have to get back the id first then marshall it to yaml
            config.setId(savedRecord.getId());
            dslContext.update(SYNC_WORK_CONFIG_TABLE)
                    .set(SYNC_WORK_CONFIG_TABLE.YAML, serializer.toYaml(config))
                    .where(SYNC_WORK_CONFIG_TABLE.ID.equal(config.getId()))
                    .execute();
        } else {
            dslContext.update(SYNC_WORK_CONFIG_TABLE)
                    .set(SYNC_WORK_CONFIG_TABLE.NAME, config.getName())
                    .set(SYNC_WORK_CONFIG_TABLE.DESCRIPTION, config.getDescription())
                    .set(SYNC_WORK_CONFIG_TABLE.YAML, serializer.toYaml(config))
                    .where(SYNC_WORK_CONFIG_TABLE.ID.equal(config.getId()))
                    .execute();
        }*/
    }

    @Override
    public boolean delete(Long id) {
       /* DSLContext dslContext = DSL.using(connection, SQLDialect.H2);

        int execute = dslContext.deleteFrom(SYNC_WORK_CONFIG_TABLE)
                .where(SYNC_WORK_CONFIG_TABLE.ID.equal(id))
                .execute();
        return execute == 1;*/
        return false;
    }

    @Override
    public List<SyncWorkConfig> getHistory(Long id) {
        //TODO implement
        throw new UnsupportedOperationException("Implement me!");
        //return null;
    }

    @Override
    public List<SyncWorkConfig> getAll() {
        /*DSLContext dslContext = DSL.using(connection, SQLDialect.H2);

        Result<Record> result =
                dslContext.select().from(SYNC_WORK_CONFIG_TABLE).fetch();

        List<SyncWorkConfig> syncWorkConfigs =
                result.stream().map(r -> serializer
                        .fromYaml(r.getValue(SYNC_WORK_CONFIG_TABLE.YAML)))
                        .collect(
                                Collectors.toList());
        return syncWorkConfigs;*/
        return Lists.newArrayList();
    }
}
