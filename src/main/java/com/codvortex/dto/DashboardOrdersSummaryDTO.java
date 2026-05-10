package com.codvortex.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DashboardOrdersSummaryDTO {
    private Integer total;
    private Integer confirmed;
    private Integer pending;
    private Integer canceled;
    private Integer delivered;
    private Integer shipping;
    private Integer returned;
    private Integer postponed;
    private Integer reprogrammed;
}
