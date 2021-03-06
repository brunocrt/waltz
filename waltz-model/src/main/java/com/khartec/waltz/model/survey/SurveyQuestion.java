package com.khartec.waltz.model.survey;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdProvider;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyQuestion.class)
@JsonDeserialize(as = ImmutableSurveyQuestion.class)
public abstract class SurveyQuestion implements IdProvider {

    public abstract Long surveyTemplateId();
    public abstract String questionText();
    public abstract Optional<String> helpText();
    public abstract SurveyQuestionFieldType fieldType();
    public abstract Optional<String> sectionName();
    public abstract Integer position();
    public abstract Boolean isMandatory();
    public abstract Boolean allowComment();
}
