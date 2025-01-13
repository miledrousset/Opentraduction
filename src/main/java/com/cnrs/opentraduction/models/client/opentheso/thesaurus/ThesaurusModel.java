package com.cnrs.opentraduction.models.client.opentheso.thesaurus;

import com.cnrs.opentraduction.models.client.opentheso.concept.LabelModel;
import lombok.Data;
import java.util.List;


@Data
public class ThesaurusModel {

    private String idTheso;
    private List<LabelModel> labels;

}
