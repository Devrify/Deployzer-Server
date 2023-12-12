package com.devrify.deployzerserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devrify.deployzerserver.common.enums.DeployzerTokenStatusEnum;
import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.dao.DeployTokenDao;
import com.devrify.deployzerserver.entity.vo.DeployTokenVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
public class DeployTokenService extends ServiceImpl<DeployTokenDao, DeployTokenVo> {

    public boolean checkIfTokenValid(String token) throws DeployzerException {
        if (StringUtils.isBlank(token)) {
            throw new DeployzerException("token 为空");
        }

        List<DeployTokenVo> allValidToken = this.getAllValidToken();
        if (CollectionUtils.isEmpty(allValidToken)) {
            return false;
        }
        for (DeployTokenVo deployTokenVo : allValidToken) {
            if (token.equals(deployTokenVo.getToken())) {
                return true;
            }
        }
        return false;
    }

    private List<DeployTokenVo> getAllValidToken() {
        LambdaQueryWrapper<DeployTokenVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeployTokenVo::getTokenStatus, DeployzerTokenStatusEnum.VALID);
        return this.list(queryWrapper);
    }
}
