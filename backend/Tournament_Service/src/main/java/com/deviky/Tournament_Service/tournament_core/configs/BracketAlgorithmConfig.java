package com.deviky.Tournament_Service.tournament_core.configs;

import com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base.BracketAlgorithm;
import com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base.BracketAlgorithmFactory;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
public class BracketAlgorithmConfig {

    @Bean
    public BracketAlgorithmFactory bracketAlgorithmFactory() {
        Map<String, BracketAlgorithm> map = new HashMap<>();

        // Используем Reflections для поиска всех классов, наследующихся от BaseAlgorithm
        Reflections reflections = new Reflections("com.deviky.Tournament_Service.bracket.bracket_algorithms"); // пакет с алгоритмами
        Set<Class<? extends BracketAlgorithm>> classes = reflections.getSubTypesOf(BracketAlgorithm.class);

        for (Class<? extends BracketAlgorithm> clazz : classes) {
            try {
                BracketAlgorithm algo = clazz.getDeclaredConstructor().newInstance();
                map.put(algo.getType(), algo);
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate algorithm: " + clazz, e);
            }
        }

        return new BracketAlgorithmFactory(map);
    }
}
