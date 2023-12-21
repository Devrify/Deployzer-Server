package com.devrify.deployzerserver.controller;

import com.devrify.deployzerserver.common.enums.DeployzerClientStatusEnum;
import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.entity.dto.GetCommandResponseDto;
import com.devrify.deployzerserver.entity.dto.RegistrationDto;
import com.devrify.deployzerserver.entity.dto.ReportCommandResultDto;
import com.devrify.deployzerserver.entity.dto.ResultDto;
import com.devrify.deployzerserver.entity.vo.DeployClientVo;
import com.devrify.deployzerserver.entity.vo.DeployExecutionVo;
import com.devrify.deployzerserver.service.DeployClientService;
import com.devrify.deployzerserver.service.DeployTokenService;
import com.devrify.deployzerserver.service.facade.DeployExecutionFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deployzer/client")
@Slf4j
@RequiredArgsConstructor
public class DeployzerClientController extends BaseController {

    private final DeployClientService deployClientService;

    private final DeployTokenService deployTokenService;

    private final DeployExecutionFacadeService deployExecutionFacadeService;

    @PostMapping("/registration")
    public ResultDto<DeployClientVo> registration(
            @RequestHeader("Authorization") String token, @RequestBody RegistrationDto registrationDto) {
        log.info(registrationDto.toString());
        return this.easyReturn(() -> {
            // 检查 token
            this.checkIfTokenValid(token);
            // 检查属性
            if (StringUtils.isAnyBlank(registrationDto.getUuid(), registrationDto.getIp())) {
                throw new DeployzerException("uuid, ip 为空");
            }
            // 注册并返回
            return this.deployClientService.registration(registrationDto);
        }, new DeployClientVo());
    }

    @PostMapping("/get-command")
    public ResultDto<GetCommandResponseDto> getCommand(
            @RequestHeader("Authorization") String token,
            @RequestBody RegistrationDto registrationDto) {
        log.info(registrationDto.toString());
        return this.easyReturn(() -> {
            // 检查 token
            this.checkIfTokenValid(token);
            // 检查 uuid 是否为空
            if (StringUtils.isBlank(registrationDto.getUuid())) {
                throw new DeployzerException("uuid 为空");
            }
            // 获取命令
            DeployExecutionVo deployExecutionVo =
                    this.deployExecutionFacadeService.getCommand(registrationDto.getUuid());
            // 返回结果
            if (ObjectUtils.isEmpty(deployExecutionVo)) {
                return new GetCommandResponseDto();
            }
            return new GetCommandResponseDto(
                    deployExecutionVo.getDeployExecutionId(),
                    deployExecutionVo.getCommand());
        }, new GetCommandResponseDto());
    }

    @PostMapping("/report-command-result")
    public ResultDto<String> reportResult(
            @RequestHeader("Authorization") String token,
            @RequestBody ReportCommandResultDto reportCommandResultDto) {
        log.info(reportCommandResultDto.toString());
        return this.easyReturn(() -> {
            // 检查 token
            this.checkIfTokenValid(token);
            // 检查属性
            if (ObjectUtils.anyNull(
                    reportCommandResultDto,
                    reportCommandResultDto.getDeployExecutionId(),
                    reportCommandResultDto.getDuration())) {
                throw new DeployzerException("入参 或 execution id 或 duration 为空");
            }
            if (StringUtils.isAllBlank(
                    reportCommandResultDto.getStdout(),
                    reportCommandResultDto.getStderr()
            )) {
                throw new DeployzerException("stdout 和 stderr 均为空");
            }
            // 保存 log
            this.deployExecutionFacadeService.saveExecutionResult(reportCommandResultDto);
        });
    }

    private void checkIfTokenValid(String token) throws DeployzerException {
        if (StringUtils.isBlank(token)) {
            throw new DeployzerException("token 为空");
        }
        this.deployTokenService.checkIfTokenValid(token);
    }
}
