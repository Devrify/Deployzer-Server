package com.devrify.deployzerserver.controller;

import com.devrify.deployzerserver.common.enums.DeployzerClientStatusEnum;
import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.entity.dto.*;
import com.devrify.deployzerserver.entity.vo.DeployClientVo;
import com.devrify.deployzerserver.entity.vo.DeployExecutionVo;
import com.devrify.deployzerserver.entity.vo.DeployParamValueVo;
import com.devrify.deployzerserver.entity.vo.DeployTemplateVo;
import com.devrify.deployzerserver.service.DeployClientService;
import com.devrify.deployzerserver.service.DeployCommandService;
import com.devrify.deployzerserver.service.DeployExecutionService;
import com.devrify.deployzerserver.service.DeployTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author houance
 * @since 2023-12-06 11:55:48
 */
@RestController
@RequestMapping("/deployzer")
@Slf4j
@RequiredArgsConstructor
public class DeployzerFacadeController {

    private final DeployTokenService deployTokenService;

    private final DeployClientService deployClientService;

    private final DeployExecutionService deployExecutionService;

    private final DeployCommandService deployCommandService;

    @PostMapping("/registration")
    public ResultDto<DeployClientVo> registration(
            @RequestHeader("Authorization") String token, @RequestBody RegistrationDto registrationDto) {
        log.info(registrationDto.toString());
        // 检查 token
        try {
            this.checkIfTokenValid(token);
        } catch (DeployzerException e) {
            return ResultDto.fail(e.getMessage(), new DeployClientVo());
        }
        // 检查属性
        if (StringUtils.isAnyBlank(registrationDto.getUuid(), registrationDto.getIp())) {
            return ResultDto.fail("uuid, ip 为空", new DeployClientVo());
        }
        // 注册并返回
        DeployClientVo databaseResult = this.deployClientService.registration(registrationDto);
        return ResultDto.success(databaseResult);
    }

    @PostMapping("/get-command")
    public ResultDto<GetCommandResponseDto> getCommand(
            @RequestHeader("Authorization") String token,
            @RequestBody RegistrationDto registrationDto) {
        log.info(registrationDto.toString());
        // 检查 token
        try {
            this.checkIfTokenValid(token);
        } catch (DeployzerException e) {
            return ResultDto.fail(e.getMessage(), new GetCommandResponseDto());
        }
        // 检查 uuid 是否为空
        if (StringUtils.isBlank(registrationDto.getUuid())) {
            return ResultDto.fail("uuid 为空", new GetCommandResponseDto());
        }
        // todo: 获取命令

        return ResultDto.success(new GetCommandResponseDto(1L, "java --version"));
    }

    @PostMapping("/report-command-result")
    public ResultDto<String> reportResult(
            @RequestHeader("Authorization") String token,
            @RequestBody ReportCommandResultDto reportCommandResultDto) {
        log.info(reportCommandResultDto.toString());
        // 检查 token
        try {
            this.checkIfTokenValid(token);
        } catch (DeployzerException e) {
            return ResultDto.fail(e.getMessage());
        }
        // 检查 execution id 是否有效
        if (ObjectUtils.isEmpty(reportCommandResultDto.getDeployExecutionId())) {
            return ResultDto.fail("execution id 为空");
        }
        DeployExecutionVo databaseResult =
                this.deployExecutionService.getById(reportCommandResultDto.getDeployExecutionId());
        if (ObjectUtils.isEmpty(databaseResult)) {
            return ResultDto.fail("execution id 找不到记录:" + reportCommandResultDto.getDeployExecutionId());
        }
        // todo:保存 log

        // 更新状态
        try {
            this.deployClientService.updateClientStatusByClientId(
                    databaseResult.getDeployClientId(), DeployzerClientStatusEnum.WAITING);
        } catch (DeployzerException e) {
            return ResultDto.fail(e.getMessage());
        }
        return ResultDto.success();
    }

    @PostMapping("create-template")
    public ResultDto<String> createTemplateAndParam(@RequestBody DeployTemplateVo deployTemplateVo) {
        log.info(deployTemplateVo.toString());
        return this.easyReturn(() -> {
            this.checkIfDeployTemplateValid(deployTemplateVo);
            deployTemplateVo.setDeployTemplateId(null);
            this.deployCommandService.saveTemplate(deployTemplateVo);
        });
    }

    @PostMapping("update-template")
    public ResultDto<String> updateTemplateAndParam(@RequestBody DeployTemplateVo deployTemplateVo) {
        log.info(deployTemplateVo.toString());
        return this.easyReturn(() -> {
            this.checkIfDeployTemplateValid(deployTemplateVo);
            if (ObjectUtils.isEmpty(deployTemplateVo.getDeployTemplateId())) {
                throw new DeployzerException("template id 为空");
            }
            this.deployCommandService.updateTemplate(deployTemplateVo);
        });
    }

    @PostMapping("create-param-set")
    public ResultDto<String> createParamSet(@RequestBody List<DeployParamValueVo> deployParamValueVos) {
        log.info(deployParamValueVos.toString());
        return this.easyReturn(() -> {
            if (CollectionUtils.isEmpty(deployParamValueVos)) {
                throw new DeployzerException("输入为空");
            }
            for (DeployParamValueVo deployTemplateVo : deployParamValueVos) {
                if (StringUtils.isAnyBlank(
                        deployTemplateVo.getDeployParamKey(),
                        deployTemplateVo.getDeployParamValue(),
                        deployTemplateVo.getParamSetName()
                )) {
                    throw new DeployzerException("key, value, param set name 存在空值");
                }
                deployTemplateVo.setDeployParamValueId(null);
            }
            this.deployCommandService.saveParamSet(deployParamValueVos);
        });
    }

    @PostMapping("update-param-set")
    public ResultDto<String> updateParamSet(@RequestBody List<DeployParamValueVo> deployParamValueVos) {
        log.info(deployParamValueVos.toString());
        return this.easyReturn(() -> {
            if (CollectionUtils.isEmpty(deployParamValueVos)) {
                throw new DeployzerException("输入为空");
            }
            for (DeployParamValueVo deployTemplateVo : deployParamValueVos) {
                if (StringUtils.isAnyBlank(
                        deployTemplateVo.getDeployParamKey(),
                        deployTemplateVo.getDeployParamValue(),
                        deployTemplateVo.getParamSetName()
                )) {
                    throw new DeployzerException("key, value, param set name 存在空值");
                }
                if (ObjectUtils.isEmpty(deployTemplateVo.getDeployParamValueId())) {
                    throw new DeployzerException("deploy param value id 为空");
                }
            }
            this.deployCommandService.updateParamSet(deployParamValueVos);
        });
    }

    private void checkIfTokenValid(String token) throws DeployzerException {
        if (StringUtils.isBlank(token)) {
            throw new DeployzerException("token 为空");
        }
        try {
            this.deployTokenService.checkIfTokenValid(token);
        } catch (DeployzerException e) {
            throw new DeployzerException(e.getMessage());
        }
    }

    private void checkIfDeployTemplateValid(DeployTemplateVo deployTemplateVo) throws DeployzerException {
        log.info(deployTemplateVo.toString());
        // 检查属性
        if (ObjectUtils.isEmpty(deployTemplateVo)) {
            throw new DeployzerException("模板 VO 为空");
        }
        if (StringUtils.isAnyBlank(deployTemplateVo.getTemplateName(), deployTemplateVo.getTemplateContent())) {
            throw new DeployzerException("模板的内容，名称为空");
        }
    }

    private ResultDto<String> easyReturn(CheckedFunction function) {
        try {
            function.accept();
        } catch (DeployzerException e) {
            return ResultDto.fail(e.getMessage());
        }
        return ResultDto.success();
    }

    @FunctionalInterface
    public interface CheckedFunction {
        void accept() throws DeployzerException;
    }
}
