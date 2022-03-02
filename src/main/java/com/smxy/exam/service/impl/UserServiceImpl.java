package com.smxy.exam.service.impl;

import com.smxy.exam.beans.User;
import com.smxy.exam.mapper.UserMapper;
import com.smxy.exam.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fsy
 * @since 2022-02-10
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
