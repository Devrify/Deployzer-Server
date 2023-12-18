package com.devrify.deployzerserver.service.facade;

import com.devrify.deployzerserver.common.enums.DeployzerClientStatusEnum;
import com.devrify.deployzerserver.common.enums.DeployzerExecutionStatusEnum;
import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.entity.vo.DeployClientVo;
import com.devrify.deployzerserver.entity.vo.DeployExecutionVo;
import com.devrify.deployzerserver.service.DeployClientService;
import com.devrify.deployzerserver.service.DeployExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeployExecutionFacadeService {

    private final DeployExecutionService deployExecutionService;

    private final DeployClientService deployClientService;

    private ConcurrentHashMap<Long, ConcurrentLinkedQueue<DeployExecutionVo>> map;

    public void createExecution(DeployExecutionVo deployExecutionVo) {
        // 设置状态并保存
        deployExecutionVo.setExecutionStatus(DeployzerExecutionStatusEnum.WAITING.name());
        deployExecutionService.save(deployExecutionVo);
        // 放命令到 map 中
        ConcurrentLinkedQueue<DeployExecutionVo> commandQueue =
                map.getOrDefault(deployExecutionVo.getDeployClientId(), new ConcurrentLinkedQueue<>());
        commandQueue.offer(deployExecutionVo);
        this.map.put(deployExecutionVo.getDeployClientId(), commandQueue);
    }

    public String getCommand(String clientUuid) throws DeployzerException {
        // 获取 client id
        DeployClientVo deployClientVo = this.deployClientService.getDeployClientByUuid(clientUuid);
        if (ObjectUtils.isEmpty(deployClientVo)) {
            throw new DeployzerException("找不到 client：" + clientUuid);
        }
        // 获取执行 vo
        ConcurrentLinkedQueue<DeployExecutionVo> deployExecutionVos = this.map.get(deployClientVo.getDeployClientId());
        if (ObjectUtils.isEmpty(deployExecutionVos)) {
            return null;
        }
        // 获取数据库对应记录， 状态置为运行， 并返回结果
        DeployExecutionVo deployExecutionVo = deployExecutionVos.poll();
        if (ObjectUtils.isEmpty(deployExecutionVo)) {
            return null;
        }
        DeployExecutionVo databaseResult =
                this.deployExecutionService.getById(deployExecutionVo.getDeployExecutionId());
        databaseResult.setExecutionStatus(DeployzerExecutionStatusEnum.RUNNING.name());
        this.deployExecutionService.updateById(databaseResult);
        // 根据 client uuid 更新
        this.deployClientService.updateClientStatusByUuid(clientUuid, DeployzerClientStatusEnum.RUNNING);
        return databaseResult.getCommand();
    }
}
