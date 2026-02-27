package com.deviky.Tournament_Service.bracket.bracket_algorithms.SINGLE_ELIMINATION;

import com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base.AlgorithmParamConfig;
import com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base.AlgorithmParams;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SingleEliminationParams extends AlgorithmParams {

    @AlgorithmParamConfig(
            label = "Случайный порядок команд",
            description = "Если включено, команды будут случайно распределены по сетке. "
                    + "Если выключено, порядок будет соответствовать переданному списку.",
            defaultValue = "true"
    )
    private Boolean shuffle = true;
}
