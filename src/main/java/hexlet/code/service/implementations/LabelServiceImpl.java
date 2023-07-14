package hexlet.code.service.implementations;

import hexlet.code.dto.LabelDto;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.service.LabelService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    @Override
    public Label createLabel(LabelDto labelDto) {
        Label newLabel = new Label();
        newLabel.setName(labelDto.getName());
        return labelRepository.save(newLabel);
    }

    @Override
    public Label getLabelById(Long id) {
        return labelRepository.findById(id)
                .orElseThrow();
    }

    @Override
    public List<Label> getAllLabels() {
        return labelRepository.findAll();
    }

    @Override
    public Label updateLabel(LabelDto labelDto, Long id) {
        Label updateLabel = labelRepository.findById(id).get();
        updateLabel.setName(labelDto.getName());
        return labelRepository.save(updateLabel);
    }

    @Override
    public void deleteLabel(Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow();
        labelRepository.delete(label);
    }
}
