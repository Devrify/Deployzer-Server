package com.devrify.deployzerserver.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devrify.deployzerserver.entity.vo.DeployClientVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author houance
 * @since 2023-12-06 11:55:48
 */
@Mapper
public interface DeployClientDao extends BaseMapper<DeployClientVo> {

}
