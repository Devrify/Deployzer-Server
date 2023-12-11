package com.devrify.deployzerserver.entity.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class GetCommandResponseDto {

    @JsonProperty("deploy_execution_id")
    private Long deployExecutionId;

    @JsonProperty("command")
    private String command;
}
