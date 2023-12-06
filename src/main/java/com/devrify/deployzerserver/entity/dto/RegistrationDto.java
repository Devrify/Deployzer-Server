package com.devrify.deployzerserver.entity.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class RegistrationDto {

    @JsonProperty("ip")
    private String ip;

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("name")
    private String name;
}
