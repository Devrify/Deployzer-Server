package com.devrify.deployzerserver.service.facade;

import com.devrify.deployzerserver.common.enums.DeployzerClientStatusEnum;
import com.devrify.deployzerserver.common.enums.DeployzerExecutionStatusEnum;
import com.devrify.deployzerserver.common.enums.DeployzerParamSetStatusEnum;
import com.devrify.deployzerserver.common.enums.DeployzerTemplateStatusEnum;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

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

    private final ConcurrentHashMap<String, Integer> paramSetRunningCountMap =
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
        // 获取命令和 param set
        String command;
        if (StringUtils.isBlank(deployExecutionVo.getParamSetUuid())) {
            command = this.deployCommandFacadeService.getCommandFromTemplate(deployExecutionVo.getDeployTemplateId());
        } else {
            command = this.deployCommandFacadeService.getCommandFromTemplateAndParamSet(
                    deployExecutionVo.getDeployTemplateId(),
                    deployExecutionVo.getParamSetUuid()
            );
        }
        // 设置完整的命令和 execution 的状态
        deployExecutionVo.setCommand(command);
        deployExecutionVo.setExecutionStatus(DeployzerExecutionStatusEnum.WAITING.name());
        deployExecutionService.save(deployExecutionVo);
        // 放命令到 map 中
        ConcurrentLinkedQueue<DeployExecutionVo> commandQueue =
                clientCommandQueueMap.getOrDefault(deployExecutionVo.getDeployClientId(), new ConcurrentLinkedQueue<>());
        commandQueue.offer(deployExecutionVo);
        this.clientCommandQueueMap.put(deployExecutionVo.getDeployClientId(), commandQueue);
        // 状态 map count ++
        templateRunningCountMap.put(
                deployExecutionVo.getDeployTemplateId(),
                templateRunningCountMap.getOrDefault(deployExecutionVo.getDeployTemplateId(), 0) + 1
        );
        if (StringUtils.isNotBlank(deployExecutionVo.getParamSetUuid())) {
            paramSetRunningCountMap.put(
                    deployExecutionVo.getParamSetUuid(),
                    paramSetRunningCountMap.getOrDefault(deployExecutionVo.getParamSetUuid(), 0) + 1
            );
        }
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

    public DeployExecutionVo saveExecutionResult(
            ReportCommandResultDto reportCommandResultDto)
            throws DeployzerException {
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
        // 更新 client 状态
        this.deployClientService.updateClientStatusByClientId(
                databaseResult.getDeployClientId(), DeployzerClientStatusEnum.WAITING);
        // 更新状态
        Long templateId = databaseResult.getDeployTemplateId();
        this.updateStatus(
                templateRunningCountMap,
                templateId,
                () -> this.deployCommandFacadeService.updateTemplateStatus(
                        templateId, DeployzerTemplateStatusEnum.WAITING)
        );
        String paramSetUuid = databaseResult.getParamSetUuid();
        if (StringUtils.isNotEmpty(paramSetUuid)) {
            this.updateStatus(
                    paramSetRunningCountMap,
                    paramSetUuid,
                    () -> this.deployCommandFacadeService.updateParamSetStatus(
                            paramSetUuid, DeployzerParamSetStatusEnum.WAITING)
            );
        }
        return databaseResult;
    }

    private <T> void updateStatus (
            Map<T, Integer> map,
            T key,
            Function databaseUpdateFunction) throws DeployzerException {
        Integer count = map.get(key);
        if (ObjectUtils.isEmpty(count)) {
            databaseUpdateFunction.run();
            return;
        }
        if (count -1 <= 0) {
            map.put(key, 0);
            databaseUpdateFunction.run();
        } else {
            map.put(key, count - 1);
        }
    }

    @FunctionalInterface
    private interface Function {
        void run() throws DeployzerException;
    }
}
