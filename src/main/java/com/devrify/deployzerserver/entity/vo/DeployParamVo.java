package com.devrify.deployzerserver.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;

import lombok.*;

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
@TableName("deploy_param_t")
public class DeployParamVo extends BaseVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "deploy_param_id", type = IdType.AUTO)
    private Long deployParamId;

    @TableField("param_content")
    private String paramContent;

    @TableField("param_name")
    private String paramName;

    @TableField("deploy_template_id")
    private Long deployTemplateId;
}