package com.devrify.deployzerserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devrify.deployzerserver.dao.DeployParamKeyDao;
import com.devrify.deployzerserver.entity.vo.DeployParamKeyVo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author houance
 * @since 2023-12-06 11:55:48
 */
@Service
public class DeployParamKeyService extends ServiceImpl<DeployParamKeyDao, DeployParamKeyVo> {

    public void saveOrUpdateTemplateIdAndParamKey(List<DeployParamKeyVo> deployParamKeyVos) {
        for (DeployParamKeyVo deployParamKeyVo : deployParamKeyVos) {
            DeployParamKeyVo databaseResult = this.getByTemplateIdAndParamKey(deployParamKeyVo);
            if (ObjectUtils.isEmpty(databaseResult)) {
                this.save(deployParamKeyVo);
            } else {
                deployParamKeyVo.setDeployParamId(databaseResult.getDeployParamId());
                this.updateById(deployParamKeyVo);
            }
        }
    }

    public List<DeployParamKeyVo> getByTemplateId(Long templateId) {
        LambdaQueryWrapper<DeployParamKeyVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeployParamKeyVo::getDeployTemplateId, templateId);
        return this.list(queryWrapper);
    }

    private DeployParamKeyVo getByTemplateIdAndParamKey(DeployParamKeyVo deployParamKeyVo) {
        LambdaQueryWrapper<DeployParamKeyVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeployParamKeyVo::getDeployTemplateId, deployParamKeyVo.getDeployTemplateId());
        queryWrapper.eq(DeployParamKeyVo::getParamKey, deployParamKeyVo.getParamKey());
        return this.getOne(queryWrapper);
    }
}
