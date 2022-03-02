package com.smxy.exam.service.impl;

import com.smxy.exam.beans.Admin;
import com.smxy.exam.mapper.AdminMapper;
import com.smxy.exam.service.IAdminService;
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
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

}
