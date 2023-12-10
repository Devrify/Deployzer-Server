package com.devrify.deployzerserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devrify.deployzerserver.entity.vo.DeployTemplateVo;
import com.devrify.deployzerserver.dao.DeployTemplateDao;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author houance
 * @since 2023-12-06 11:55:48
 */
@Service
public class DeployTemplateServiceImpl extends ServiceImpl<DeployTemplateDao, DeployTemplateVo> {
    
    public boolean checkIfTemplateExist(String name) {
        return ObjectUtils.isNotEmpty(this.getDeployTemplateByName(name));
    }

    public boolean checkIfTemplateExist(Long templateId) {
        return ObjectUtils.isNotEmpty(this.getById(templateId));
    }
    
    private DeployTemplateVo getDeployTemplateByName(String name) {
        LambdaQueryWrapper<DeployTemplateVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeployTemplateVo::getTemplateName, name);
        return this.getOne(queryWrapper);
    }
}
