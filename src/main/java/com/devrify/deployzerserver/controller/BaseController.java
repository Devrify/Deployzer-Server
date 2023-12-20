package com.devrify.deployzerserver.controller;

import com.devrify.deployzerserver.common.exception.DeployzerException;
import com.devrify.deployzerserver.entity.dto.ResultDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseController {

    public ResultDto<String> easyReturn(CheckedFunction function) {
        try {
            function.run();
        } catch (DeployzerException e) {
            log.error(e.getMessage());
            return ResultDto.fail(e.getMessage());
        }
        return ResultDto.success();
    }

    public <T> ResultDto<T> easyReturn(CheckedFunctionWithReturn<T> function, T emptyData) {
        T result;
        try {
            result = function.run();
        } catch (DeployzerException e) {
            log.error(e.getMessage());
            return ResultDto.fail(e.getMessage(), emptyData);
        }
        return ResultDto.success(result);
    }

    @FunctionalInterface
    public interface CheckedFunction {
        void run() throws DeployzerException;
    }

    @FunctionalInterface
    public interface CheckedFunctionWithReturn<T> {
        T run() throws DeployzerException;
    }
}
