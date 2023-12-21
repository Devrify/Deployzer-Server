package com.devrify.deployzerserver.job;

import com.devrify.deployzerserver.common.enums.DeployzerClientStatusEnum;
import com.devrify.deployzerserver.entity.vo.DeployClientVo;
import com.devrify.deployzerserver.service.DeployClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeployzerClientTimerJob {

    private final DeployClientService deployClientService;

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void checkClientStatus() {
        // 获取等待中的 client
        List<DeployClientVo> waitingClients =
                deployClientService.getDeployClientByStatus(DeployzerClientStatusEnum.WAITING);
        if (CollectionUtils.isEmpty(waitingClients)) {
            return;
        }
        // 判断上次心跳时间是否超过 120 秒
        List<DeployClientVo> downClients = new ArrayList<>(waitingClients.size());
        for (DeployClientVo waitingClient : waitingClients) {
            LocalDateTime now = LocalDateTime.now();
            long diffInSecond = ChronoUnit.SECONDS.between(now, waitingClient.getLastUpdatedDate());
            if (Math.abs(diffInSecond) > 120) {
                downClients.add(waitingClient);
            }
        }
        downClients.forEach(o -> o.setClientStatus(DeployzerClientStatusEnum.DOWN.name()));
        this.deployClientService.updateBatchById(downClients);
    }
}
