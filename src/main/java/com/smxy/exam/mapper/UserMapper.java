package com.smxy.exam.mapper;

import com.smxy.exam.beans.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fsy
 * @since 2022-02-10
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
