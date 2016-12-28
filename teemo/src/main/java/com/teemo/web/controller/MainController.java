/**
 * Copyright (c) 2016- https://github.com/beiyoufx
 *
 * Licensed under the GPL-3.0
 */
package com.teemo.web.controller;

import com.teemo.core.Constants;
import com.teemo.dto.Result;
import com.teemo.entity.Menu;
import com.teemo.entity.User;
import com.teemo.service.ResourceService;
import com.teemo.service.UserService;
import core.web.controller.BaseController;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author yongjie.teng
 * @date 16-11-22 上午8:59
 * @email yongjie.teng@foxmail.com
 * @package com.teemo.web.controller
 */
@Controller
public class MainController extends BaseController {
    @Resource
    private UserService userService;
    @Resource
    private ResourceService resourceService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String goLogin() {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User user = (User) session.getAttribute(Constants.CURRENT_USER);
        // 已登录状态直接跳转home页
        if (user != null) {
            return "redirect:/home";
        }
        return "login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public void login(HttpServletRequest request,
                      HttpServletResponse response,
                      String username,
                      String password,
                      Boolean rememberMe) throws IOException {
        Result result;
        try {
            Subject subject = SecurityUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
            subject.login(token);
            result = new Result(1, "登陆成功");
        } catch (UnknownAccountException ua) {
            result = new Result(-1, "用户名错误");
        } catch (IncorrectCredentialsException ic) {
            result = new Result(-2, "密码错误");
        } catch (LockedAccountException la) {
            result = new Result(-3, "账号被锁定");
        } catch (Exception e) {
            result = new Result(-4, "未知错误");
        }
        writeJSON(response, result);
    }

    @RequestMapping("/logout")
    public String logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "redirect:/login";
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String goRegister() {
        return "register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void register(HttpServletResponse response,
                         User user) throws IOException {
        Result result;
        user = userService.register(user);
        if (user.getId() != null) {
            result = new Result(1, "注册成功");
        } else {
            result = new Result(-1, "注册失败");
        }
        writeJSON(response, result);
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView home(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mav = new ModelAndView();
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User user = (User) session.getAttribute(Constants.CURRENT_USER);
        if (user != null) {
            List<Menu> menus = resourceService.findMenu(user);
            mav.addObject("menus", menus);
            mav.setViewName("admin/home");
        }
        return mav;
    }


    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return "admin/dashboard";
    }

    @RequestMapping("/unauthorized")
    public String unauthorized() {
        return "error/unauthorized";
    }
}
