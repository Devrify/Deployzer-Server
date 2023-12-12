package com.devrify.deployzerserver.service;

import com.devrify.deployzerserver.common.OperationUtil;
import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.entity.vo.DeployParamVo;
import com.devrify.deployzerserver.entity.vo.DeployTemplateVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeployCommandService {

    private final DeployTemplateService deployTemplateService;

    private final DeployParamService deployParamService;

    private final DeployParamSetService deployParamSetService;

    public void saveTemplateAndParam(
            DeployTemplateVo deployTemplateVo,
            List<DeployParamVo> deployParamVos) throws DeployzerException {
        // template 检查
        this.checkIfDeployTemplateValid(deployTemplateVo);
        // 检查模板是否已存在
        if (deployTemplateService.checkIfTemplateExist(deployTemplateVo.getTemplateName())) {
            throw new DeployzerException("命令模板的名称已存在");
        }
        this.baseCrudMethod(deployTemplateVo, deployParamVos);
    }

    public void updateTemplateAndParam(
            DeployTemplateVo deployTemplateVo,
            List<DeployParamVo> deployParamVos) throws DeployzerException {
        // template 检查
        this.checkIfDeployTemplateValid(deployTemplateVo);
        this.baseCrudMethod(deployTemplateVo, deployParamVos);
    }


    private void baseCrudMethod(
            DeployTemplateVo deployTemplateVo,
            List<DeployParamVo> deployParamVos) throws DeployzerException {
        // 获取参数 keys， 没有参数则保存然后返回
        List<String> paramKeys = this.deployTemplateService.getParamKey(deployTemplateVo.getTemplateContent());
        if (CollectionUtils.isEmpty(paramKeys)) {
            this.deployTemplateService.save(deployTemplateVo);
            return;
        }
        // 检查 template id 是否匹配
        this.checkIfTemplateIdMatch(deployTemplateVo, deployParamVos);
        // param 检查
        this.checkIfDeployParamValid(deployParamVos);
        // 有参数则进行语法检查, 如果参数不在 param key 里面， 则报错
        Set<String> set = deployParamVos.stream().map(DeployParamVo::getParamKey).collect(Collectors.toSet());
        for (String paramKey : paramKeys) {
            if (!set.contains(paramKey)) {
                throw new DeployzerException(paramKey + " 没有参数");
            }
        }
        this.deployTemplateService.saveOrUpdate(deployTemplateVo);
        this.deployParamService.saveOrUpdateTemplateIdAndParamKey(deployParamVos);
    }

    private void checkIfDeployTemplateValid(DeployTemplateVo deployTemplateVo) throws DeployzerException {
        // 检查属性是否为空
        if (StringUtils.isAnyBlank(deployTemplateVo.getTemplateContent(), deployTemplateVo.getTemplateName())) {
            throw new DeployzerException("命令模板名称， 内容为空");
        }
    }

    private void checkIfDeployParamValid(List<DeployParamVo> deployParamVos) throws DeployzerException {
        // 检查属性是否为空
        if (CollectionUtils.isEmpty(deployParamVos)) {
            throw new DeployzerException("部署参数为空");
        }
        for (DeployParamVo deployParamVo : deployParamVos) {
            if (StringUtils.isAnyBlank(deployParamVo.getParamKey())) {
                throw new DeployzerException("部署参数的 key 为空");
            }
        }
        String duplicateElement = OperationUtil.hasDuplicateElement(deployParamVos, DeployParamVo::getParamKey);
        if (ObjectUtils.isNotEmpty(duplicateElement)) {
            throw new DeployzerException("部署参数的 key 重复 ：" + duplicateElement);
        }
    }

    private void checkIfTemplateIdMatch(
            DeployTemplateVo deployTemplateVo,
            List<DeployParamVo> deployParamVos
    ) throws DeployzerException {
        if (CollectionUtils.isEmpty(deployParamVos)) {
            return;
        }
        Long deployTemplateId = deployTemplateVo.getDeployTemplateId();
        for (DeployParamVo deployParamVo : deployParamVos) {
            if (!deployTemplateId.equals(deployParamVo.getDeployTemplateId())) {
                throw new DeployzerException(
                        "部署模板 id 不匹配 " + deployTemplateId + "-" + deployParamVo.getDeployTemplateId());
            }
        }
    }
}
