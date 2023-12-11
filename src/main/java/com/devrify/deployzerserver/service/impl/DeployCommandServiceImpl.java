package com.devrify.deployzerserver.service.impl;

import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.entity.vo.DeployParamVo;
import com.devrify.deployzerserver.entity.vo.DeployTemplateVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor

public class DeployCommandServiceImpl {

    private final DeployTemplateServiceImpl deployTemplateService;

    private final DeployParamServiceImpl deployParamService;

    public void saveOrUpdateTemplateAndParam(
            DeployTemplateVo deployTemplateVo,
            List<DeployParamVo> deployParamVos) throws DeployzerException {
        this.checkIfDeployTemplateValid(deployTemplateVo);
        // 获取参数 keys， 没有参数则保存然后返回
        List<String> paramKeys = this.deployTemplateService.getParamKey(deployTemplateVo.getTemplateContent());
        if (CollectionUtils.isEmpty(paramKeys)) {
            this.deployTemplateService.save(deployTemplateVo);
            return;
        }
        // 有参数则进行语法检查, 如果参数不在 param key 里面， 则报错
        this.checkIfDeployParamValid(deployParamVos);
        Set<String> set = deployParamVos.stream().map(DeployParamVo::getParamKey).collect(Collectors.toSet());
        for (String paramKey : paramKeys) {
            if (!set.contains(paramKey)) {
                throw new DeployzerException(paramKey + " 没有参数");
            }
        }
        this.deployTemplateService.saveOrUpdate(deployTemplateVo);
        this.deployParamService.saveOrUpdateBatch(deployParamVos);
    }

    public void updateTemplate(DeployTemplateVo deployTemplateVo) throws DeployzerException {

    }

    public void updateParam(List<DeployParamVo> deployParamVos) throws DeployzerException {

    }

    public void updateTemplateAndParam(DeployTemplateVo deployTemplateVo) throws DeployzerException {

    }

    private void checkIfDeployTemplateValid(DeployTemplateVo deployTemplateVo) throws DeployzerException {
        // 检查属性是否为空
        if (StringUtils.isAnyBlank(deployTemplateVo.getTemplateContent(), deployTemplateVo.getTemplateName())) {
            throw new DeployzerException("命令模板名称， 内容为空");
        }
        // 检查模板是否已存在
        if (deployTemplateService.checkIfTemplateExist(deployTemplateVo.getTemplateName())) {
            throw new DeployzerException("命令模板的名称已存在");
        }
    }

    private void checkIfDeployParamValid(List<DeployParamVo> deployParamVos) throws DeployzerException {
        // 检查属性是否为空
        if (CollectionUtils.isEmpty(deployParamVos)) {
            throw new DeployzerException("部署参数为空");
        }
        for (DeployParamVo deployParamVo : deployParamVos) {
            if (StringUtils.isAnyBlank(deployParamVo.getParamKey(), deployParamVo.getParamValue())) {
                throw new DeployzerException("部署参数的 key 或 value 为空");
            }
        }
    }
}
