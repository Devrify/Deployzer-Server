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
 * @since 2023-12-12 11:26:16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@TableName("deploy_param_set_t")
public class DeployParamValueVo extends BaseVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "deploy_param_set_id", type = IdType.AUTO)
    private Long deployParamValueId;

    @TableField(value = "param_set_name", updateStrategy = FieldStrategy.NEVER)
    private String paramSetName;

    @TableField(value = "deploy_template_id", updateStrategy = FieldStrategy.NEVER)
    private Long deployTemplateId;

    @TableField(value = "deploy_param_key", updateStrategy = FieldStrategy.NEVER)
    private String deployParamKey;

    @TableField("deploy_param_value")
    private String deployParamValue;

    @TableField("param_set_status")
    private String paramSetStatus;

    @TableField(value = "param_set_uuid", updateStrategy = FieldStrategy.NEVER)
    private String paramSetUuid;
}
