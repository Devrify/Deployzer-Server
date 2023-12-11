package com.devrify.deployzerserver.entity.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class ReportCommandResultDto {

    @JsonProperty("deploy_execution_id")
    private Long deployExecutionId;

    @JsonProperty("stdout")
    private String stdout;

    @JsonProperty("stderr")
    private String stderr;

    @JsonProperty("duration")
    private Float duration;
}
