package com.devrify.deployzerserver.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devrify.deployzerserver.common.enums.DeployzerClientStatusEnum;
import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.dao.DeployClientDao;
import com.devrify.deployzerserver.entity.dto.RegistrationDto;
import com.devrify.deployzerserver.entity.vo.DeployClientVo;
import org.apache.commons.lang3.ObjectUtils;
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
public class DeployClientService extends ServiceImpl<DeployClientDao, DeployClientVo> {

    public DeployClientVo registration(RegistrationDto registrationDto) {
        // 检查是否有注册
        DeployClientVo databaseResult = this.getDeployClientByUuid(registrationDto.getUuid());
        // 没有注册则注册
        if (ObjectUtils.isEmpty(databaseResult)) {
            DeployClientVo deployClientVo = new DeployClientVo();
            deployClientVo.setClientIp(registrationDto.getIp());
            deployClientVo.setClientUuid(registrationDto.getUuid());
            deployClientVo.setClientStatus(DeployzerClientStatusEnum.WAITING.name());
            // client name 判空然后设置默认值
            if (StringUtils.isBlank(registrationDto.getName())) {
                deployClientVo.setClientName("you forget your name");
            } else {
                deployClientVo.setClientName(registrationDto.getName());
            }
            this.save(deployClientVo);
            return deployClientVo;
        }
        // 已经注册则更新状态
        return this.updateClientStatusByUuid(databaseResult.getClientUuid(), DeployzerClientStatusEnum.WAITING);
    }

    public void updateClientStatusByClientId(
            Long clientId, DeployzerClientStatusEnum statusEnum) throws DeployzerException {
        DeployClientVo deployClientVo = this.getById(clientId);
        if (ObjectUtils.isEmpty(deployClientVo)) {
            throw new DeployzerException("client id 找不到记录: " + clientId);
        }
        deployClientVo.setClientStatus(statusEnum.name());
        this.updateById(deployClientVo);
    }

    public DeployClientVo updateClientStatusByUuid(
            String uuid, DeployzerClientStatusEnum statusEnum) {
        DeployClientVo deployClientVo = this.getDeployClientByUuid(uuid);
        deployClientVo.setClientStatus(statusEnum.name());
        this.updateById(deployClientVo);
        return deployClientVo;
    }

    private DeployClientVo getDeployClientByUuid(String uuid) {
        LambdaQueryWrapper<DeployClientVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeployClientVo::getClientUuid, uuid);
        return this.getOne(queryWrapper);
    }

    private List<DeployClientVo> getDeployClientByStatus(DeployzerClientStatusEnum statusEnum) {
        LambdaQueryWrapper<DeployClientVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeployClientVo::getClientStatus, statusEnum);
        return this.list(queryWrapper);
    }
}
