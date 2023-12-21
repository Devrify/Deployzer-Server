package com.devrify.deployzerserver.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.devrify.deployzerserver.common.enums.DeployzerTemplateStatusEnum;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "creationDate", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "createdBy", () -> "System", String.class);
        this.strictInsertFill(metaObject, "lastUpdatedDate", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "lastUpdatedBy", () -> "System", String.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("lastUpdatedDate", LocalDateTime.now(), metaObject);
        this.setFieldValByName("lastUpdatedBy", "System" ,metaObject);
    }
}
