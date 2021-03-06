package com.khartec.waltz.data.survey;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.survey.ImmutableSurveyTemplate;
import com.khartec.waltz.model.survey.SurveyTemplate;
import com.khartec.waltz.model.survey.SurveyTemplateStatus;
import com.khartec.waltz.schema.tables.records.SurveyTemplateRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.SurveyTemplate.SURVEY_TEMPLATE;

@Repository
public class SurveyTemplateDao {

    public static final RecordMapper<Record, SurveyTemplate> TO_DOMAIN_MAPPER = r -> {
        SurveyTemplateRecord record = r.into(SURVEY_TEMPLATE);

        return ImmutableSurveyTemplate.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .targetEntityKind(EntityKind.valueOf(record.getTargetEntityKind()))
                .ownerId(record.getOwnerId())
                .createdAt(record.getCreatedAt().toLocalDateTime())
                .status(SurveyTemplateStatus.valueOf(record.getStatus()))
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public SurveyTemplateDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public SurveyTemplate getById(long id) {
        return dsl.select()
                .from(SURVEY_TEMPLATE)
                .where(SURVEY_TEMPLATE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<SurveyTemplate> findAllActive() {
        return dsl.select()
                .from(SURVEY_TEMPLATE)
                .where(SURVEY_TEMPLATE.STATUS.eq(SurveyTemplateStatus.ACTIVE.name()))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
