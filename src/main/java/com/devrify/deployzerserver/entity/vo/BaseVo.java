package com.devrify.deployzerserver.entity.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseVo {
    @TableField(value = "creation_date", fill = FieldFill.INSERT)
    private LocalDateTime creationDate;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(value = "last_updated_date", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastUpdatedDate;

    @TableField(value = "last_updated_by", fill = FieldFill.INSERT_UPDATE)
    private String lastUpdatedBy;
}
