package com.deviky.Integration_Service.integrations.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class StatisticBase {
    private String platform;
}