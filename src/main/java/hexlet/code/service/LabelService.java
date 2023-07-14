package hexlet.code.service;

import hexlet.code.dto.LabelDto;
import hexlet.code.model.Label;

import java.util.List;

public interface LabelService {
    Label getLabelById(Long id);
    List<Label> getAllLabels();
    Label createLabel(LabelDto labelDto);
    Label updateLabel(LabelDto labelDto, Long id);
    void deleteLabel(Long id);
}
