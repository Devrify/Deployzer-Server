package com.devrify.deployzerserver.controller;

import com.devrify.deployzerserver.common.enums.DeployzerClientStatusEnum;
import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.entity.dto.*;
import com.devrify.deployzerserver.entity.vo.DeployClientVo;
import com.devrify.deployzerserver.entity.vo.DeployExecutionVo;
import com.devrify.deployzerserver.entity.vo.DeployParamVo;
import com.devrify.deployzerserver.entity.vo.DeployTemplateVo;
import com.devrify.deployzerserver.service.impl.DeployClientServiceImpl;
import com.devrify.deployzerserver.service.impl.DeployCommandServiceImpl;
import com.devrify.deployzerserver.service.impl.DeployExecutionServiceImpl;
import com.devrify.deployzerserver.service.impl.DeployTokenServiceImpl;
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

    private final DeployTokenServiceImpl deployTokenService;

    private final DeployClientServiceImpl deployClientService;

    private final DeployExecutionServiceImpl deployExecutionService;

    private final DeployCommandServiceImpl deployCommandService;

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

    @PostMapping("create-template-param")
    public ResultDto<String> createTemplateAndParam(@RequestBody CreateTemplateParamDto createTemplateParamDto) {
        log.info(createTemplateParamDto.toString());
        // 检查属性
        if (ObjectUtils.anyNull(createTemplateParamDto, createTemplateParamDto.getDeployTemplateVo())) {
            return ResultDto.fail("dto 或者命令模板为空");
        }
        try {
            this.deployCommandService.saveOrUpdateTemplateAndParam(
                    createTemplateParamDto.getDeployTemplateVo(),
                    createTemplateParamDto.getDeployParamVos()
            );
        } catch (DeployzerException e) {
            return ResultDto.fail(e.getMessage());
        }
        return ResultDto.success();
    }

    @PostMapping("/update-param")
    public ResultDto<String> updateParam(@RequestBody List<DeployParamVo> deployParamVos) {
        log.info(deployParamVos.toString());
        // 检查属性是否为空
        if (CollectionUtils.isEmpty(deployParamVos)) {
            return ResultDto.fail("param vos 为空");
        }
        try {
            this.deployCommandService.updateParam(deployParamVos);
        } catch (DeployzerException e) {
            return ResultDto.fail(e.getMessage());
        }
        return ResultDto.success();
    }

    @PostMapping("/update-template")
    public ResultDto<String> updateTemplate(@RequestBody DeployTemplateVo deployTemplateVo) {
        log.info(deployTemplateVo.toString());
        if (ObjectUtils.isEmpty(deployTemplateVo)) {
            return ResultDto.fail("template vo 为空");
        }
        try {
            this.deployCommandService.updateTemplate(deployTemplateVo);
        } catch (DeployzerException e) {
            return ResultDto.fail(e.getMessage());
        }
        return ResultDto.success();
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
}
