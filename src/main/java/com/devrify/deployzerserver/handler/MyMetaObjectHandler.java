package com.devrify.deployzerserver.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "creationDate", LocalDateTime::now, LocalDateTime.class);
        metaObject.setValue("createdBy", "System");
        this.strictInsertFill(metaObject, "lastUpdatedDate", LocalDateTime::now, LocalDateTime.class);
        metaObject.setValue("lastUpdatedBy", "System");
        metaObject.setValue("status", "VALID");
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "lastUpdatedDate", LocalDateTime::now, LocalDateTime.class);
        metaObject.setValue("lastUpdatedBy", "System");
    }
}
