/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.trait;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.trait.ImmutableTraitUsage;
import com.khartec.waltz.model.trait.TraitUsage;
import com.khartec.waltz.model.trait.TraitUsageKind;
import com.khartec.waltz.schema.tables.records.TraitUsageRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.TraitUsage.TRAIT_USAGE;

@Repository
public class TraitUsageDao {

    private final DSLContext dsl;

    private RecordMapper<? super Record, TraitUsage> traitUsageMapper = r -> {
        TraitUsageRecord record = r.into(TRAIT_USAGE);
        return ImmutableTraitUsage.builder()
                .entityReference(ImmutableEntityReference.builder()
                        .id(record.getEntityId())
                        .kind(EntityKind.valueOf(record.getEntityKind()))
                        .build())
                .relationship(TraitUsageKind.valueOf(record.getRelationship()))
                .traitId(record.getTraitId())
                .build();
    };

    @Autowired
    public TraitUsageDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<TraitUsage> findAll() {
        return queryForList(DSL.trueCondition());
    }


    public List<TraitUsage> findByEntityKind(EntityKind kind) {
        return queryForList(
                TRAIT_USAGE.ENTITY_KIND.eq(kind.name()));
    }


    public List<TraitUsage> findByTraitId(long id) {
        return queryForList(
                TRAIT_USAGE.TRAIT_ID.eq(id));
    }


    public List<TraitUsage> findByEntityReference(EntityReference reference) {
        return queryForList(
                TRAIT_USAGE.ENTITY_ID.eq(reference.id())
                        .and(TRAIT_USAGE.ENTITY_KIND.eq(reference.kind().name()))
        );
    }


    public boolean addTraitUsage(EntityReference entityReference, Long traitId, TraitUsageKind traitUsageKind) {
        return dsl.insertInto(TRAIT_USAGE)
                .set(TRAIT_USAGE.ENTITY_KIND, entityReference.kind().name())
                .set(TRAIT_USAGE.ENTITY_ID, entityReference.id())
                .set(TRAIT_USAGE.TRAIT_ID, traitId)
                .set(TRAIT_USAGE.RELATIONSHIP, traitUsageKind.name())
                .execute() == 1;
    }


    public boolean removeTraitUsage(EntityReference entityReference, long traitId) {
        return dsl.deleteFrom(TRAIT_USAGE)
                .where(TRAIT_USAGE.ENTITY_KIND.eq(entityReference.kind().name()))
                .and(TRAIT_USAGE.ENTITY_ID.eq(entityReference.id()))
                .and(TRAIT_USAGE.TRAIT_ID.eq(traitId))
                .execute() == 1;
    }

    // -- HELPERS --

    private List<TraitUsage> queryForList(Condition condition) {
        return dsl.select(TRAIT_USAGE.fields())
                .from(TRAIT_USAGE)
                .where(condition)
                .fetch(traitUsageMapper);
    }

}
