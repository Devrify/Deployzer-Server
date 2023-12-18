package com.devrify.deployzerserver.controller;

import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.entity.dto.ResultDto;
import com.devrify.deployzerserver.entity.vo.DeployParamValueVo;
import com.devrify.deployzerserver.entity.vo.DeployTemplateVo;
import com.devrify.deployzerserver.service.facade.DeployCommandFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/deployzer/command")
@Slf4j
@RequiredArgsConstructor
public class DeployzerCommandController {

    private final DeployCommandFacadeService deployCommandFacadeService;

    @PostMapping("create-template")
    public ResultDto<String> createTemplateAndParam(@RequestBody DeployTemplateVo deployTemplateVo) {
        log.info(deployTemplateVo.toString());
        return this.easyReturn(() -> {
            this.checkIfDeployTemplateValid(deployTemplateVo);
            deployTemplateVo.setDeployTemplateId(null);
            this.deployCommandFacadeService.saveTemplate(deployTemplateVo);
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
            this.deployCommandFacadeService.updateTemplate(deployTemplateVo);
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
            this.deployCommandFacadeService.saveParamSet(deployParamValueVos);
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
            this.deployCommandFacadeService.updateParamSet(deployParamValueVos);
        });
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
