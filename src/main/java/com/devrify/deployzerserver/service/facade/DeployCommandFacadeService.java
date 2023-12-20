package com.devrify.deployzerserver.service.facade;

import com.devrify.deployzerserver.common.util.OperationUtil;
import com.devrify.deployzerserver.common.enums.DeployzerParamSetStatusEnum;
import com.devrify.deployzerserver.common.enums.DeployzerTemplateStatusEnum;
import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.entity.vo.DeployParamKeyVo;
import com.devrify.deployzerserver.entity.vo.DeployParamValueVo;
import com.devrify.deployzerserver.entity.vo.DeployTemplateVo;
import com.devrify.deployzerserver.service.DeployParamKeyService;
import com.devrify.deployzerserver.service.DeployParamValueService;
import com.devrify.deployzerserver.service.DeployTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeployCommandFacadeService {

    private final DeployTemplateService deployTemplateService;

    private final DeployParamKeyService deployParamKeyService;

    private final DeployParamValueService deployParamValueService;

    public void saveTemplate(DeployTemplateVo deployTemplateVo) throws DeployzerException {
        // 检查是否已存在
        DeployTemplateVo databaseResult =
                this.deployTemplateService.getDeployTemplateByName(deployTemplateVo.getTemplateName());
        if (ObjectUtils.isNotEmpty(databaseResult)) {
            throw new DeployzerException("命令模板的名称已存在");
        }
        // 保存 template service， 获取 template id
        this.deployTemplateService.save(deployTemplateVo);
        // 获取参数 keys， 没有参数则返回
        List<String> paramKeys = this.deployTemplateService.getParamKey(deployTemplateVo.getTemplateContent());
        if (CollectionUtils.isEmpty(paramKeys)) {
            return;
        }
        // 保存 key
        List<DeployParamKeyVo> deployParamKeyVos = new ArrayList<>(paramKeys.size());
        for (String paramKey : paramKeys) {
            DeployParamKeyVo deployParamKeyVo = new DeployParamKeyVo();
            deployParamKeyVo.setParamKey(paramKey);
            deployParamKeyVo.setDeployTemplateId(deployTemplateVo.getDeployTemplateId());
            deployParamKeyVos.add(deployParamKeyVo);
        }
        this.deployParamKeyService.saveBatch(deployParamKeyVos);
    }

    public void updateTemplate(DeployTemplateVo deployTemplateVo) throws DeployzerException {
        // 检查 template 是否存在
        DeployTemplateVo databaseResult =
                this.deployTemplateService.getById(deployTemplateVo.getDeployTemplateId());
        if (ObjectUtils.isEmpty(databaseResult)) {
            throw new DeployzerException("找不到 template :" + deployTemplateVo);
        }
        // 检查 template 是否为运行中
        if (DeployzerTemplateStatusEnum.RUNNING.name().equals(databaseResult.getStatus())) {
            throw new DeployzerException("模板的状态为运行中");
        }
        // 更新 template
        this.deployTemplateService.updateById(deployTemplateVo);
        // 获取 template 之前的 key, 全部删除, 添加新的 param key
        List<DeployParamKeyVo> databaseParamKeyVos =
                this.deployParamKeyService.getByTemplateId(deployTemplateVo.getDeployTemplateId());
        this.deployParamKeyService.removeBatchByIds(databaseParamKeyVos);
        // 获取 template 之前的 param set
        List<DeployParamValueVo> databaseParamValueVos =
                this.deployParamValueService.getByTemplateId(deployTemplateVo.getDeployTemplateId());
        // 获取现在的 param key, 如果为空则删除 param set, 不为空的添加新的 template 和 param key
        List<String> paramKeys = this.deployTemplateService.getParamKey(deployTemplateVo.getTemplateContent());
        if (CollectionUtils.isEmpty(paramKeys)) {
            this.deployParamValueService.removeBatchByIds(databaseParamValueVos);
            return;
        }
        // 添加新的 param key
        List<DeployParamKeyVo> deployParamKeyVos = new ArrayList<>(paramKeys.size());
        for (String paramKey : paramKeys) {
            DeployParamKeyVo deployParamKeyVo = new DeployParamKeyVo();
            deployParamKeyVo.setDeployTemplateId(deployParamKeyVo.getDeployTemplateId());
            deployParamKeyVo.setParamKey(paramKey);
            deployParamKeyVos.add(deployParamKeyVo);
        }
        this.deployParamKeyService.saveBatch(deployParamKeyVos);
        // 除非新的 param key 和原来的 param key 一致， 否则 param set 的状态都置为待确认
        List<String> oldParamKeys = deployParamKeyVos.stream().map(DeployParamKeyVo::getParamKey).toList();
        if (OperationUtil.checkIfListsDiff(oldParamKeys, paramKeys)) {
            databaseParamValueVos.forEach(o -> o.setParamSetStatus(DeployzerParamSetStatusEnum.INVALID.name()));
            this.deployParamValueService.updateBatchById(databaseParamValueVos);
        }
    }

    public void saveParamSet(List<DeployParamValueVo> deployParamValueVos) throws DeployzerException {
        // 检查 param set name
        DeployParamValueVo firstParamKeyVo = deployParamValueVos.get(0);
        List<DeployParamValueVo> databaseResult =
                this.deployParamValueService.getByTemplateIdParamSetName(firstParamKeyVo);
        if (CollectionUtils.isNotEmpty(databaseResult)) {
            throw new DeployzerException("template id 下的 param set name 重复:" + firstParamKeyVo);
        }
        // 检查模板和 param set 是否一致
        this.checkIfTemplateMatchParamSet(deployParamValueVos);
        // 设置状态为启用, 保存
        for (DeployParamValueVo deployParamValueVo : deployParamValueVos) {
            String paramSetString = deployParamValueVo.getParamSetName() + deployParamValueVo.getDeployTemplateId();
            String paramSetUuid = UUID.nameUUIDFromBytes(paramSetString.getBytes()).toString();
            deployParamValueVo.setParamSetUuid(paramSetUuid);
            deployParamValueVo.setParamSetStatus(DeployzerParamSetStatusEnum.WAITING.name());
        }
        this.deployParamValueService.saveBatch(deployParamValueVos);
    }

    public void updateParamSet(List<DeployParamValueVo> deployParamValueVos) throws DeployzerException {
        // 检查模板和 param set 是否一致
        this.checkIfTemplateMatchParamSet(deployParamValueVos);
        // 检查 param value 是否存在
        List<Long> ids = deployParamValueVos.stream().map(DeployParamValueVo::getDeployParamValueId).toList();
        List<DeployParamValueVo> databaseResult =
                this.deployParamValueService.listByIds(ids);
        if (CollectionUtils.isEmpty(databaseResult)) {
            throw new DeployzerException("根据 value id 找不到记录");
        }
        if (ids.size() != databaseResult.size()) {
            throw new DeployzerException("部分 value id 不存在");
        }
        // 检查 param set 状态是否可用
        List<String> statusList = databaseResult.stream().map(DeployParamValueVo::getParamSetStatus).toList();
        if (OperationUtil.anyNotEqual(statusList, DeployzerParamSetStatusEnum.WAITING.name())) {
            throw new DeployzerException("参数 set 的状态不可用");
        }
        this.deployParamValueService.updateBatchById(deployParamValueVos);
    }

    public String getCommandFromTemplateAndParamSet(Long templateId, String paramSetUuid) throws DeployzerException {
        // 检查参数
        DeployTemplateVo template = this.deployTemplateService.getById(templateId);
        if (ObjectUtils.isEmpty(template)) {
            throw new DeployzerException("找不到模板：" + templateId);
        }
        List<DeployParamValueVo> paramSet = this.deployParamValueService.getByParamSetUuid(paramSetUuid);
        if (ObjectUtils.isEmpty(paramSet)) {
            throw new DeployzerException("找不到参数 set");
        }
        // 检查 param set 状态是否可用
        List<String> statusList = paramSet.stream().map(DeployParamValueVo::getParamSetStatus).toList();
        if (OperationUtil.anyEqual(statusList, DeployzerParamSetStatusEnum.INVALID.name())) {
            throw new DeployzerException("参数 set 的状态不可用");
        }
        // 更新状态为运行中
        this.updateTemplateAndParamSetStatus(templateId, paramSetUuid, DeployzerTemplateStatusEnum.RUNNING);
        // 将 place holder 转换为 实际的 value
        String result = template.getTemplateContent();
        for (DeployParamValueVo deployParamValueVo : paramSet) {
            result = this.deployTemplateService.setParamValue(
                    deployParamValueVo.getDeployParamKey(),
                    deployParamValueVo.getDeployParamValue(),
                    result);
        }
        return result;
    }

    public String getCommandFromTemplate(Long templateId) throws DeployzerException {
        // 检查参数
        DeployTemplateVo template = this.deployTemplateService.getById(templateId);
        if (ObjectUtils.isEmpty(template)) {
            throw new DeployzerException("找不到模板：" + templateId);
        }
        // 更新状态为运行中
        template.setStatus(DeployzerTemplateStatusEnum.RUNNING.name());
        this.deployTemplateService.updateById(template);
        return template.getTemplateContent();
    }

    public void updateTemplateAndParamSetStatus(
            Long templateId,
            String paramSetUuid,
            DeployzerTemplateStatusEnum deployzerTemplateStatusEnum) throws DeployzerException {
        // 检查参数
        if (ObjectUtils.anyNull(templateId, deployzerTemplateStatusEnum)) {
            throw new DeployzerException("template id 或 template 状态为空");
        }
        if (StringUtils.isBlank(paramSetUuid)) {
            throw new DeployzerException("param set uuid 为空");
        }
        // 检查数据库
        DeployTemplateVo templateVo = this.deployTemplateService.getById(templateId);
        List<DeployParamValueVo> paramValueVos = this.deployParamValueService.getByParamSetUuid(paramSetUuid);
        if (ObjectUtils.isEmpty(templateVo) || CollectionUtils.isEmpty(paramValueVos)) {
            throw new DeployzerException("template 或者 param set uuid 找不到记录");
        }
        // 更新状态
        templateVo.setStatus(deployzerTemplateStatusEnum.name());
        paramValueVos.forEach(o -> o.setParamSetStatus(deployzerTemplateStatusEnum.name()));
        this.deployTemplateService.updateById(templateVo);
        this.deployParamValueService.updateBatchById(paramValueVos);
    }

    private void checkIfTemplateMatchParamSet(List<DeployParamValueVo> deployParamValueVos) throws DeployzerException {
        // 检查 template id
        DeployParamValueVo firstParamKeyVo = deployParamValueVos.get(0);
        DeployTemplateVo template = this.deployTemplateService.getById(firstParamKeyVo.getDeployTemplateId());
        if (ObjectUtils.isEmpty(template)) {
            throw new DeployzerException("template 找不到" + template);
        }
        // 检查 param key 是否一致
        List<String> paramKeys = deployParamValueVos.stream().map(DeployParamValueVo::getDeployParamKey).toList();
        List<String> templateParamKeys =
                this.deployTemplateService.getParamKey(template.getTemplateContent());
        if (OperationUtil.checkIfListsDiff(paramKeys, templateParamKeys)) {
            throw new DeployzerException("参数和模板对不上");
        }
    }
}
