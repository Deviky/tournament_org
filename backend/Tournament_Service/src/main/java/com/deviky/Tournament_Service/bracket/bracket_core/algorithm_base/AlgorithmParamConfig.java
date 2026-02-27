package com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AlgorithmParamConfig {

    String label();               // Название поля для фронта
    boolean required() default false;
    String description() default "";
    String defaultValue() default "";

    long min() default Long.MIN_VALUE;      // для чисел
    long max() default Long.MAX_VALUE;

    int minLength() default 0;              // для строк
    int maxLength() default Integer.MAX_VALUE;

    String[] allowedValues() default {};    // enum / select
}
