package com.devrify.deployzerserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devrify.deployzerserver.dao.DeployParamDao;
import com.devrify.deployzerserver.entity.vo.DeployParamVo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
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
public class DeployParamService extends ServiceImpl<DeployParamDao, DeployParamVo> {

    public void saveOrUpdateTemplateIdAndParamKey(List<DeployParamVo> deployParamVos) {
        for (DeployParamVo deployParamVo : deployParamVos) {
            DeployParamVo databaseResult = this.getByTemplateIdAndParamKey(deployParamVo);
            if (ObjectUtils.isEmpty(databaseResult)) {
                this.save(deployParamVo);
            } else {
                deployParamVo.setDeployParamId(databaseResult.getDeployParamId());
                this.updateById(deployParamVo);
            }
        }
    }

    private DeployParamVo getByTemplateIdAndParamKey(DeployParamVo deployParamVo) {
        LambdaQueryWrapper<DeployParamVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeployParamVo::getDeployTemplateId, deployParamVo.getDeployTemplateId());
        queryWrapper.eq(DeployParamVo::getParamKey, deployParamVo.getParamKey());
        return this.getOne(queryWrapper);
    }
}
