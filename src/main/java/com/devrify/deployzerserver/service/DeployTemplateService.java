package com.devrify.deployzerserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devrify.deployzerserver.common.util.OperationUtil;
import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.dao.DeployTemplateDao;
import com.devrify.deployzerserver.entity.vo.DeployTemplateVo;
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
public class DeployTemplateService extends ServiceImpl<DeployTemplateDao, DeployTemplateVo> {

    private static final Pattern PARAM_PATTERN = Pattern.compile("\\$\\{([^}]*)}");

    private static final String PARAM_PLACE_HOLDER = "${%s}";

    public String setParamValue(String key, String value, String template) {
        return template.replace(PARAM_PLACE_HOLDER.formatted(key), value);
    }

    public List<String> getParamKey(String templateContent) throws DeployzerException {
        List<String> result = new ArrayList<>(10);
        Matcher matcher = PARAM_PATTERN.matcher(templateContent);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        if (OperationUtil.hasDuplicateElement(result)) {
            throw new DeployzerException("存在重复的 key");
        }
        return result;
    }

    public DeployTemplateVo getDeployTemplateByName(String name) {
        LambdaQueryWrapper<DeployTemplateVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeployTemplateVo::getTemplateName, name);
        return this.getOne(queryWrapper);
    }
}
