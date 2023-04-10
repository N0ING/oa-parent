package com.oa.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.auth.service.SysMenuService;
import com.oa.auth.service.SysUserService;
import com.oa.common.config.exception.OaException;
import com.oa.common.jwt.JwtHelper;
import com.oa.common.result.Result;
import com.oa.common.utils.MD5;
import com.oa.model.system.SysMenu;
import com.oa.model.system.SysUser;
import com.oa.vo.system.LoginVo;
import com.oa.vo.system.RouterVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author NO
 * @create 2023-03-21-15:30
 *
 * 后台登录登出
 */
@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {

    @Autowired
    private SysUserService userService;

    @Autowired
    private SysMenuService MenuService;

    // login 登录
    @PostMapping("login")
    public Result Login(@RequestBody LoginVo loginVo){

        // 获取用户名 查询用户名是否存在
        String username = loginVo.getUsername();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = userService.getOne(wrapper);

        if(user ==null){
            throw new OaException(201,"用户名不存在");
        }
        // 判断密码是否正确
        String passwordDB = user.getPassword();
        String passwordMD5 = MD5.encrypt(loginVo.getPassword());
        if(!passwordDB.equals(passwordMD5)){
            throw new OaException(201,"密码错误");
        }
        // 判断用户是否被禁用
        if(user.getStatus().intValue() == 0){
            throw  new OaException(201, "用户被禁用，请联系管理员");
        }
        // jwt 根据用户名和用户id生成token字符串
        String token = JwtHelper.createToken(user.getId(), user.getUsername());

        Map<String,Object> map = new HashMap<>();
        map.put("token", token);
        return Result.ok(map);
    }

    // info 获取用户信息
    @GetMapping("info")
    public Result info(HttpServletRequest request){
        // 从请求头获取用户id和用户名（获取请求头token信息）
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);

        // 根据用户id查询数据库获取用户信息
        SysUser sysUser = userService.getById(userId);
        // 根据用户id获取用户可操作菜单列表和按钮权限
        List<RouterVo> sysMenuList = MenuService.getMenuListByUserId(userId);
        List<String> permsList = MenuService.getPermsListByUserId(userId);
        // 返回对应信息
        Map<String, Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name",sysUser.getName());
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        // 返回用户可以操作的菜单和按钮
        map.put("routers", sysMenuList);
        map.put("buttons", permsList);
        return Result.ok(map);
    }
    // logout 退出
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }

}
