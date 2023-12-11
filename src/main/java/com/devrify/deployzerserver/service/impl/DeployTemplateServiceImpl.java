package com.devrify.deployzerserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devrify.deployzerserver.dao.DeployTemplateDao;
import com.devrify.deployzerserver.entity.vo.DeployTemplateVo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author houance
 * @since 2023-12-06 11:55:48
 */
@Service
public class DeployTemplateServiceImpl extends ServiceImpl<DeployTemplateDao, DeployTemplateVo> {

    private static final Pattern PARAM_PATTERN = Pattern.compile("\\$\\{([^}]*)}");

    public List<String> getParamKey(String templateContent) {
        ArrayList<String> result = new ArrayList<>(10);
        Matcher matcher = PARAM_PATTERN.matcher(templateContent);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

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
