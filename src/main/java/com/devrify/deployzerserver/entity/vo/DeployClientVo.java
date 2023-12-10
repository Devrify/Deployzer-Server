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
@NoArgsConstructor
@TableName("deploy_client_t")
public class DeployClientVo extends BaseVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "deploy_client_id", type = IdType.AUTO)
    private Long deployClientId;

    @TableField("client_name")
    private String clientName;

    @TableField("client_ip")
    private String clientIp;

    @TableField("client_uuid")
    private String clientUuid;

    @TableField("client_status")
    private String clientStatus;
}
