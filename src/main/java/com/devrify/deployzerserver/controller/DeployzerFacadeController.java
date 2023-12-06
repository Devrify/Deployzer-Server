package com.devrify.deployzerserver.controller;

import com.devrify.deployzerserver.entity.dto.GetCommandResponseDto;
import com.devrify.deployzerserver.entity.dto.RegistrationDto;
import com.devrify.deployzerserver.entity.dto.ReportCommandResultDto;
import com.devrify.deployzerserver.entity.dto.ResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author houance
 * @since 2023-12-06 11:55:48
 */
@RestController
@RequestMapping("/deployzer")
@Slf4j
public class DeployzerFacadeController {

    @PostMapping("/registration")
    public ResultDto<String> registration(
            @RequestHeader("Authorization") String token, @RequestBody RegistrationDto registrationDto) {
        log.info(registrationDto.toString());
        log.info(token);

        return ResultDto.success();
    }

    @PostMapping("/get-command")
    public ResultDto<GetCommandResponseDto> getCommand(@RequestBody RegistrationDto registrationDto) {
        log.info(registrationDto.toString());
        return ResultDto.success(new GetCommandResponseDto("ls"));
    }

    @PostMapping("/report-command-result")
    public ResultDto<String> reportResult(@RequestBody ReportCommandResultDto reportCommandResultDto) {
        log.info(reportCommandResultDto.toString());

        return ResultDto.success();
    }
}
