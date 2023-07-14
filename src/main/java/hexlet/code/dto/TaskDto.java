package hexlet.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    @NotBlank
    private String name;
    private String description;
    private Long authorId;
    private Long executorId;
    @NotNull
    private Long taskStatusId;
    private Set<Long> labelIds;
}
