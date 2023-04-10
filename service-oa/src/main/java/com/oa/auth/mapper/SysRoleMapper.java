package com.oa.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.model.system.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author NO
 * @create 2023-03-20-10:36
 */
@Repository
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

}
