package com.deviky.Integration_Service.integrations.platforms;

import com.deviky.Integration_Service.integrations.base.StatisticBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaceitCS2StatisticDto extends StatisticBase {
    String nickname;
    Integer elo;
    Integer level;
    String adr;
    String kd;
    String winRate;
    String matches;
}
