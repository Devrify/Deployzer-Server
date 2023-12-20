package com.devrify.deployzerserver.controller;

import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.entity.dto.ResultDto;
import com.devrify.deployzerserver.entity.vo.DeployExecutionVo;
import com.devrify.deployzerserver.entity.vo.DeployTemplateVo;
import com.devrify.deployzerserver.service.facade.DeployExecutionFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deployzer/execution")
@Slf4j
@RequiredArgsConstructor
public class DeployzerExecutionService extends BaseController {

    private final DeployExecutionFacadeService deployExecutionFacadeService;

    @PostMapping("create-execution")
    public ResultDto<String> createTemplateAndParam(@RequestBody DeployExecutionVo deployExecutionVo) {
        log.info(deployExecutionVo.toString());
        return this.easyReturn(() -> {
            if (ObjectUtils.isEmpty(deployExecutionVo)) {
                throw new DeployzerException("入参为空");
            }
            if (ObjectUtils.anyNull(
                    deployExecutionVo.getDeployTemplateId(),
                    deployExecutionVo.getDeployClientId()
            )) {
                throw new DeployzerException("template id 或 client id 为空");
            }
            this.deployExecutionFacadeService.createExecution(deployExecutionVo);
        });
    }
}
