package com.devrify.deployzerserver.entity.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResultDto<T> {

    @JsonProperty("code")
    private int code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    public static ResultDto<String> success(String message) {
        return new ResultDto<>(200, "成功", message);
    }

    public static ResultDto<String> success() {
        return new ResultDto<>(200, "成功", null);
    }

    public static <T> ResultDto<T> success(T data) {
        return new ResultDto<>(200, "成功", data);
    }

    public static <T> ResultDto<T> fail(String message, T data) {
        return new ResultDto<>(500, message, data);
    }

    public static <T> ResultDto<T> fail(T data) {
        return new ResultDto<>(500, "失败", data);
    }
}
