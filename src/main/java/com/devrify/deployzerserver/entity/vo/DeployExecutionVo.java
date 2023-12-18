package com.devrify.deployzerserver.entity.vo;

import com.baomidou.mybatisplus.annotation.*;
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

    @TableField("deploy_template_id")
    private Long deployTemplateId;

    @TableField("deploy_param_id")
    private Long deployParamId;

    @TableField("deploy_client_id")
    private Long deployClientId;

    @TableField("command")
    private String command;

    @TableField("execution_output")
    private String executionOutput;

    @TableField("execution_status")
    private String executionStatus;

    @TableField(value = "param_set_uuid", updateStrategy = FieldStrategy.NEVER)
    private String paramSetUuid;
}
