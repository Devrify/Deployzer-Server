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

    @JsonProperty("data")
    private T data;

    public static ResultDto<String> success(String message) {
        return new ResultDto<>(200, message);
    }

    public static ResultDto<String> success() {
        return new ResultDto<>(200, "成功");
    }

    public static <T> ResultDto<T> success(T data) {
        return new ResultDto<T>(200, data);
    }
}
