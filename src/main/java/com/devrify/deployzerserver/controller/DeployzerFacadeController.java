package com.devrify.deployzerserver.controller;

import com.devrify.deployzerserver.common.enums.DeployzerClientStatusEnum;
import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.entity.dto.GetCommandResponseDto;
import com.devrify.deployzerserver.entity.dto.RegistrationDto;
import com.devrify.deployzerserver.entity.dto.ReportCommandResultDto;
import com.devrify.deployzerserver.entity.dto.ResultDto;
import com.devrify.deployzerserver.entity.vo.DeployClientVo;
import com.devrify.deployzerserver.entity.vo.DeployTemplateVo;
import com.devrify.deployzerserver.service.impl.DeployClientServiceImpl;
import com.devrify.deployzerserver.service.impl.DeployTemplateServiceImpl;
import com.devrify.deployzerserver.service.impl.DeployTokenServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
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

    private final DeployTemplateServiceImpl deployTemplateService;

    @PostMapping("/registration")
    public ResultDto<DeployClientVo> registration(
            @RequestHeader("Authorization") String token, @RequestBody RegistrationDto registrationDto) {
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

        return ResultDto.success(new GetCommandResponseDto("java --version"));
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
        // 检查 uuid 是否为空
        if (StringUtils.isBlank(reportCommandResultDto.getUuid())) {
            return ResultDto.fail("uuid 为空");
        }
        // todo:保存 log

        // 更新状态
        this.deployClientService.updateClientStatusByUuid(
                    reportCommandResultDto.getUuid(), DeployzerClientStatusEnum.WAITING);
        return ResultDto.success();
    }

    @PostMapping("/create-command-template")
    public ResultDto<String> createCommandTemplate(
            @RequestHeader("Authorization") String token,
            @RequestBody DeployTemplateVo deployTemplateVo) {
        log.info(deployTemplateVo.toString());
        // 检查 token
        try {
            this.checkIfTokenValid(token);
        } catch (DeployzerException e) {
            return ResultDto.fail(e.getMessage());
        }
        // 检查属性是否为空
        if (StringUtils.isAnyBlank(deployTemplateVo.getTemplateContent(), deployTemplateVo.getTemplateName())) {
            return ResultDto.fail("命令模板名称， 内容为空");
        }
        // 检查模板是否已存在
        if (deployTemplateService.checkIfTemplateExist(deployTemplateVo.getTemplateName())) {
            return ResultDto.fail("命令模板的名称已存在");
        }
        this.deployTemplateService.save(deployTemplateVo);
        return ResultDto.success();
    }

    @PostMapping("/update-command-template")
    public ResultDto<String> updateCommandTemplate(
            @RequestHeader("Authorization") String token,
            @RequestBody DeployTemplateVo deployTemplateVo) {
        log.info(deployTemplateVo.toString());
        // 检查 token
        try {
            this.checkIfTokenValid(token);
        } catch (DeployzerException e) {
            return ResultDto.fail(e.getMessage());
        }
        // 检查属性是否为空
        if (ObjectUtils.isEmpty(deployTemplateVo.getDeployTemplateId())) {
            return ResultDto.fail("命令模板的 id 为空");
        }
        if (StringUtils.isAnyBlank(deployTemplateVo.getTemplateContent(), deployTemplateVo.getTemplateName())) {
            return ResultDto.fail("命令模板名称， 内容为空");
        }
        // 检查模板是否已存在
        if (!deployTemplateService.checkIfTemplateExist(deployTemplateVo.getDeployTemplateId())) {
            return ResultDto.fail("命令模板不存在");
        }
        this.deployTemplateService.updateById(deployTemplateVo);
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
