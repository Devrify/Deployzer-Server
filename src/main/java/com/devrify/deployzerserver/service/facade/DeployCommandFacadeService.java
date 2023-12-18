package com.devrify.deployzerserver.service.facade;

import com.devrify.deployzerserver.common.OperationUtil;
import com.devrify.deployzerserver.common.enums.DeployzerStatusEnum;
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
        if (DeployzerStatusEnum.RUNNING.name().equals(databaseResult.getStatus())) {
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
            databaseParamValueVos.forEach(o -> o.setStatus(DeployzerStatusEnum.NOT_CONFIRM.name()));
            this.deployParamValueService.updateBatchById(databaseParamValueVos);
        }
    }

    public void saveParamSet(List<DeployParamValueVo> deployParamValueVos) throws DeployzerException {
        // 检查 param set name 和 template id 是否已存在
        List<DeployParamValueVo> databaseResult =
                this.deployParamValueService.getByTemplateIdParamSetName(deployParamValueVos.get(0));
        if (CollectionUtils.isNotEmpty(databaseResult)) {
            throw new DeployzerException("template id 下的 param set name 重复");
        }
        // 设置状态为启用, 保存
        for (DeployParamValueVo deployParamValueVo : deployParamValueVos) {
            String paramSetString = deployParamValueVo.getParamSetName() + deployParamValueVo.getDeployTemplateId();
            String paramSetUuid = UUID.nameUUIDFromBytes(paramSetString.getBytes()).toString();
            deployParamValueVo.setParamSetUuid(paramSetUuid);
            deployParamValueVo.setStatus(DeployzerStatusEnum.WAITING.name());
        }
        this.deployParamValueService.saveBatch(deployParamValueVos);
    }

    public void updateParamSet(List<DeployParamValueVo> deployParamValueVos) throws DeployzerException {
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
        if (DeployzerStatusEnum.RUNNING.name().equals(databaseResult.get(0).getStatus())) {
            throw new DeployzerException("param set 的状态为运行中");
        }
        // 设置状态为启用, 保存
        deployParamValueVos.forEach(o -> o.setStatus(DeployzerStatusEnum.WAITING.name()));
        this.deployParamValueService.updateBatchById(deployParamValueVos);
    }

    public String getCommandFromTemplateAndParamSet(Long templateId, String paramSetUuid) throws DeployzerException {
        DeployTemplateVo template = this.deployTemplateService.getById(templateId);
        if (ObjectUtils.isEmpty(template)) {
            throw new DeployzerException("找不到模板：" + templateId);
        }
        List<DeployParamValueVo> paramSet = this.deployParamValueService.getByParamSetUuid(paramSetUuid);
        if (ObjectUtils.isEmpty(paramSet)) {
            throw new DeployzerException("找不到参数 set");
        }
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
}
