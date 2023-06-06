package hatch.hatchserver2023.domain.stage.dto;

import lombok.Builder;
import lombok.Data;

// API Server -> AI Server
@Data
@Builder
public class AISimilarityRequestDto {

    Double[][] seq1;
    Double[][] seq2;
}
