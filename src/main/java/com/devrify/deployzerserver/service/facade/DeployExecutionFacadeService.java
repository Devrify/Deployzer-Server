package com.devrify.deployzerserver.service.facade;

import com.devrify.deployzerserver.common.enums.DeployzerClientStatusEnum;
import com.devrify.deployzerserver.common.enums.DeployzerExecutionStatusEnum;
import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.entity.dto.ReportCommandResultDto;
import com.devrify.deployzerserver.entity.vo.DeployClientVo;
import com.devrify.deployzerserver.entity.vo.DeployExecutionVo;
import com.devrify.deployzerserver.service.DeployClientService;
import com.devrify.deployzerserver.service.DeployExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeployExecutionFacadeService {

    private final DeployExecutionService deployExecutionService;

    private final DeployClientService deployClientService;

    private final DeployCommandFacadeService deployCommandFacadeService;

    private final ConcurrentHashMap<Long, ConcurrentLinkedQueue<DeployExecutionVo>> clientCommandQueueMap =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, Integer> templateRunningCountMap =
            new ConcurrentHashMap<>();

    public void createExecution(DeployExecutionVo deployExecutionVo) throws DeployzerException {
        // 检查 client 是否可用
        DeployClientVo clientVo = this.deployClientService.getById(deployExecutionVo.getDeployClientId());
        if (ObjectUtils.isEmpty(clientVo)) {
            throw new DeployzerException("找不到 client:" + clientVo);
        }
        if (DeployzerClientStatusEnum.DOWN.name().equals(clientVo.getClientStatus())) {
            throw new DeployzerException("client 不可用");
        }
        // 设置命令，状态并保存
        deployExecutionVo.setExecutionStatus(DeployzerExecutionStatusEnum.WAITING.name());
        String command;
        if (StringUtils.isBlank(deployExecutionVo.getParamSetUuid())) {
            command = this.deployCommandFacadeService.getCommandFromTemplate(deployExecutionVo.getDeployTemplateId());
        } else {
            command = this.deployCommandFacadeService.getCommandFromTemplateAndParamSet(
                    deployExecutionVo.getDeployTemplateId(),
                    deployExecutionVo.getParamSetUuid()
            );
        }
        deployExecutionVo.setCommand(command);
        deployExecutionService.save(deployExecutionVo);
        // 放命令到 map 中
        ConcurrentLinkedQueue<DeployExecutionVo> commandQueue =
                clientCommandQueueMap.getOrDefault(deployExecutionVo.getDeployClientId(), new ConcurrentLinkedQueue<>());
        commandQueue.offer(deployExecutionVo);
        this.clientCommandQueueMap.put(deployExecutionVo.getDeployClientId(), commandQueue);
        // 放状态到 map 中
        templateRunningCountMap.put(
                deployExecutionVo.getDeployTemplateId(),
                templateRunningCountMap.getOrDefault(deployExecutionVo.getDeployTemplateId(), 0) + 1);
    }

    public DeployExecutionVo getCommand(String clientUuid) throws DeployzerException {
        // 获取 client id
        DeployClientVo deployClientVo = this.deployClientService.getDeployClientByUuid(clientUuid);
        if (ObjectUtils.isEmpty(deployClientVo)) {
            throw new DeployzerException("找不到 client：" + clientUuid);
        }
        // 获取执行 vo
        ConcurrentLinkedQueue<DeployExecutionVo> deployExecutionVos =
                this.clientCommandQueueMap.get(deployClientVo.getDeployClientId());
        if (ObjectUtils.isEmpty(deployExecutionVos)) {
            return null;
        }
        // 获取数据库对应记录， execution 和 client 状态置为运行， 并返回结果
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
        return databaseResult;
    }

    public DeployExecutionVo saveExecutionResult(ReportCommandResultDto reportCommandResultDto) throws DeployzerException {
        // 获取数据库记录
        DeployExecutionVo databaseResult =
                this.deployExecutionService.getById(reportCommandResultDto.getDeployExecutionId());
        if (ObjectUtils.isEmpty(databaseResult)) {
            throw new DeployzerException("找不到 execution 记录:" + reportCommandResultDto.getDeployExecutionId());
        }
        // 赋值并保存 log
        databaseResult.setDuration(reportCommandResultDto.getDuration());
        databaseResult.setExecutionOutput(reportCommandResultDto.getStdout());
        if (StringUtils.isBlank(reportCommandResultDto.getStderr())) {
            databaseResult.setExecutionStatus(DeployzerExecutionStatusEnum.SUCCESS.name());
        } else {
            databaseResult.setExecutionError(reportCommandResultDto.getStderr());
            databaseResult.setExecutionStatus(DeployzerExecutionStatusEnum.FAIL.name());
        }
        this.deployExecutionService.updateById(databaseResult);
        // todo: 思考状态更新的链路
        // running status -1. 如果为 0 则 template 状态应该改为 waiting
        Long templateId = databaseResult.getDeployTemplateId();
        Integer count = templateRunningCountMap.get(templateId);
        if (ObjectUtils.isEmpty(count)) {
            return databaseResult;
        }
        count--;
        templateRunningCountMap.put(templateId, count > 0 ? count : 0);
        if (count == 0) {
        }
        return databaseResult;
    }
}
