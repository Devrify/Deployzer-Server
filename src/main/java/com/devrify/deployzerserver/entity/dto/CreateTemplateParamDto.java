package com.devrify.deployzerserver.entity.dto;

import com.devrify.deployzerserver.entity.vo.DeployParamKeyVo;
import com.devrify.deployzerserver.entity.vo.DeployTemplateVo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateTemplateParamDto {

    @JsonProperty("deploy_template")
    private DeployTemplateVo deployTemplateVo;

    @JsonProperty("deploy_params")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DeployParamKeyVo> deployParamKeyVos;
}
