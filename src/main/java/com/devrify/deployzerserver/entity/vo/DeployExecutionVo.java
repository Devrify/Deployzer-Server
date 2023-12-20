package com.devrify.deployzerserver.entity.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author houance
 * @since 2023-12-06 11:55:48
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@TableName("deploy_execution_t")
public class DeployExecutionVo extends BaseVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "deploy_execution_id", type = IdType.AUTO)
    private Long deployExecutionId;

    @TableField(value = "deploy_template_id", updateStrategy = FieldStrategy.NEVER)
    private Long deployTemplateId;

    @TableField(value = "param_set_uuid", updateStrategy = FieldStrategy.NEVER)
    private String paramSetUuid;

    @TableField(value = "deploy_client_id", updateStrategy = FieldStrategy.NEVER)
    private Long deployClientId;

    @TableField("command")
    private String command;

    @TableField("execution_output")
    private String executionOutput;

    @TableField("execution_error")
    private String executionError;

    @TableField("execution_status")
    private String executionStatus;

    @TableField("duration")
    private Float duration;
}
