package com.devrify.deployzerserver.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("deploy_token_t")
public class DeployTokenVo extends BaseVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "deploy_token_id", type = IdType.AUTO)
    private Long deployTokenId;

    @TableField("token")
    private String token;

    @TableField("token_status")
    private String tokenStatus;
}
