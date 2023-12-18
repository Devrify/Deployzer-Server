package com.devrify.deployzerserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devrify.deployzerserver.dao.DeployParamValueDao;
import com.devrify.deployzerserver.entity.vo.DeployParamValueVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author houance
 * @since 2023-12-12 11:26:16
 */
@Service
public class DeployParamValueService extends ServiceImpl<DeployParamValueDao, DeployParamValueVo> {

    public List<DeployParamValueVo> getByTemplateId(Long templateId) {
        LambdaQueryWrapper<DeployParamValueVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeployParamValueVo::getDeployTemplateId, templateId);
        return this.list(queryWrapper);
    }

    public List<DeployParamValueVo> getByTemplateIdParamSetName(DeployParamValueVo deployParamValueVo) {
        LambdaQueryWrapper<DeployParamValueVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeployParamValueVo::getDeployTemplateId, deployParamValueVo.getDeployTemplateId());
        queryWrapper.eq(DeployParamValueVo::getParamSetName, deployParamValueVo.getParamSetName());
        return this.list(queryWrapper);
    }

    public List<DeployParamValueVo> getByParamSetUuid(String paramSetUuid) {
        LambdaQueryWrapper<DeployParamValueVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeployParamValueVo::getParamSetUuid, paramSetUuid);
        return this.list(queryWrapper);
    }
}
