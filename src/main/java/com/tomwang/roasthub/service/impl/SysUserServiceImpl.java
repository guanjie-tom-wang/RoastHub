package com.tomwang.roasthub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tomwang.roasthub.dao.mapper.SysUserMapper;
import com.tomwang.roasthub.dao.pojo.Users;
import com.tomwang.roasthub.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SysUserServiceImpl implements SysUserService {
    //这个爆红只需要在对应的mapper上加上@Repository,让spring识别到即可解决爆红的问题
    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Users findUserById(Long id) {
        //根据id查询
        //为防止sysUser为空增加一个判断
        Users User = sysUserMapper.selectById(id);
        if (User == null){
            User = new Users();
        }
        return User;
    }

    @Override
    public Users findUser(String account, String password) {
//        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SysUser.class);
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getUsername,account);
        queryWrapper.eq(Users::getPassword,password);
        //account id 头像 名称
        queryWrapper.select(Users::getUsername);
        //增加查询效率，只查询一条
        queryWrapper.last("limit 1");

        return sysUserMapper.selectOne(queryWrapper);
    }


    @Override
    public Users findUserByAccount(String account) {
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getUsername,account);
        //确保只能查询一条
        queryWrapper.last("limit 1");
        return this.sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public void save(Users user) {
        //保存用户这个id会自动生成
        //默认生成的id是分布式id，从采用的雪花算法
        this.sysUserMapper.insert(user);

    }

    @Override
    public void delete(Users user) {
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getUsername,user.getUsername());
        Users tuser = this.sysUserMapper.selectOne(queryWrapper); // 查询用户
        if (tuser != null) {
            this.sysUserMapper.deleteById(tuser.getId()); // 如果用户存在，则删除
        }
    }
}
