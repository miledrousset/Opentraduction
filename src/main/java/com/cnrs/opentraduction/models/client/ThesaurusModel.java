package com.cnrs.opentraduction.models.client;

import lombok.Data;
import java.util.List;


@Data
public class ThesaurusModel {

    private String idTheso;
    private List<LabelModel> labels;

}
