package com.devrify.deployzerserver.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devrify.deployzerserver.entity.vo.BaseVo;

import java.io.Serial;
import java.io.Serializable;

import lombok.*;
import lombok.experimental.Accessors;

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
public class DeployParamSetVo extends BaseVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "deploy_param_set_id", type = IdType.AUTO)
    private Long deployParamSetId;

    @TableField("deploy_param_id")
    private Long deployParamId;

    @TableField("param_set_name")
    private String paramSetName;

    @TableField("deploy_template_id")
    private Long deployTemplateId;

    @TableField("deploy_param_value")
    private String deployParamValue;
}
