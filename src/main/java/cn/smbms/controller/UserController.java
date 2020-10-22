package cn.smbms.controller;

import cn.smbms.pojo.Bill;
import cn.smbms.pojo.Provider;
import cn.smbms.pojo.Role;
import cn.smbms.pojo.User;
import cn.smbms.service.bill.BillService;
import cn.smbms.service.bill.BillServiceImpl;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.service.provider.ProviderServiceImpl;
import cn.smbms.service.role.RoleService;
import cn.smbms.service.role.RoleServiceImpl;
import cn.smbms.service.user.UserService;
import cn.smbms.service.user.UserServiceImpl;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;
import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
public class UserController {
    private static Logger logger =Logger.getLogger(UserController.class);

    @RequestMapping(value = "/login.do",method = RequestMethod.POST)
    public String getUser_01(String userCode, String userPassword, HttpSession session){

        UserService userService = new UserServiceImpl();
        User user = userService.login(userCode,userPassword);
        if (user!=null){
            System.out.println(user.getUserName());
            session.setAttribute(Constants.USER_SESSION,user);
            return "redirect:/frame.html";
        }else {
            session.setAttribute("error","用户名或密码不正确");
        }
        return "login";
    }

    @RequestMapping("/login.html")
    public String getUser_02(){
//        int i=5/0;
        return "login";
    }

    @RequestMapping(value = "/user.do")
    public String getUser_03(String queryname,String queryUserRole,String pageIndex,Model model){
        int urole = 0;
        UserService userService = new UserServiceImpl();
        List<User> userList = null;
        //设置页面容量
        int pageSize = Constants.pageSize;
        //当前页码
        int currentPageNo = 1;
        /**
         * http://localhost:8090/SMBMS/userlist.do
         * ----queryUserName --NULL
         * http://localhost:8090/SMBMS/userlist.do?queryname=
         * --queryUserName ---""
         */
        System.out.println("queryUserName servlet--------"+queryname);
        System.out.println("queryUserRole servlet--------"+queryUserRole);
        System.out.println("query pageIndex--------- > " + pageIndex);
        if(queryname == null){
            queryname = "";
        }
        if(queryUserRole != null && !queryUserRole.equals("")){
            urole = Integer.parseInt(queryUserRole);
        }

        if(pageIndex != null){
            try{
                currentPageNo = Integer.valueOf(pageIndex);
            }catch(NumberFormatException e){
                return  "error";
            }
        }
        //总数量（表）
        int totalCount	= userService.getUserCount(queryname,urole);
        //总页数
        PageSupport pages=new PageSupport();
        pages.setCurrentPageNo(currentPageNo);
        pages.setPageSize(pageSize);
        pages.setTotalCount(totalCount);

        int totalPageCount = pages.getTotalPageCount();

        //控制首页和尾页
        if(currentPageNo < 1){
            currentPageNo = 1;
        }else if(currentPageNo > totalPageCount){
            currentPageNo = totalPageCount;
        }


        userList = userService.getUserList(queryname,urole,currentPageNo, pageSize);
        model.addAttribute("userList", userList);
        List<Role> roleList = null;
        RoleService roleService = new RoleServiceImpl();
        roleList = roleService.getRoleList();
        model.addAttribute("roleList", roleList);
        model.addAttribute("queryUserName", queryname);
        model.addAttribute("queryUserRole", queryUserRole);
        model.addAttribute("totalPageCount", totalPageCount);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPageNo", currentPageNo);

        return "/userlist";
    }
    @RequestMapping("/logout.do")
    public String getLogout(){
        return "login";
    }

    @RequestMapping("/frame.html")
    public String getFrame(HttpSession session){
        User user= (User)session.getAttribute(Constants.USER_SESSION);
        if (user!=null){
            return "frame";
        }
        return "login";
    }

    @RequestMapping("/pwdmodify")
    public String getUserPwd(){

        return "pwdmodify";
    }
    @RequestMapping("/userpwd")
    public String getPwdUser(HttpSession session,String newpassword){
        Object o = session.getAttribute(Constants.USER_SESSION);
        boolean flag = false;
        if(o != null && !StringUtils.isNullOrEmpty(newpassword)){
            UserService userService = new UserServiceImpl();
            flag = userService.updatePwd(((User)o).getId(),newpassword);
            if(flag){
                session.setAttribute(Constants.SYS_MESSAGE, "修改密码成功,请退出并使用新密码重新登录！");
                session.removeAttribute(Constants.USER_SESSION);//session注销
            }else{
                session.setAttribute(Constants.SYS_MESSAGE, "修改密码失败！");
            }
        }else{
            session.setAttribute(Constants.SYS_MESSAGE, "修改密码失败！");
        }
        return "pwdmodify";
    }
    @RequestMapping(value = "/userview",params = "userid")
    public String getViewUser(String userid,Model model){
        if(!StringUtils.isNullOrEmpty(userid)){
            //调用后台方法得到user对象
            UserService userService = new UserServiceImpl();
            User user = userService.getUserById(userid);
            model.addAttribute("user", user);
        }
        return "userview";
    }
    @RequestMapping("/useradd")
    public String getUserAddhtml(){

        return "useradd";
    }

    /**
     * 添加用户
     * @param user
     * @param session
     * @return
     */
    @RequestMapping("/user/add")
    public String getUserAdd(User user,HttpSession session){
        System.out.println("用户名为："+user.getUserName());
            user.setCreationDate(new Date());
            user.setCreatedBy(((User)session.getAttribute(Constants.USER_SESSION)).getId());
            UserService userService = new UserServiceImpl();
            if(userService.add(user)){
                return "redirect:/user.do";
            }else{
            return "useradd";
        }

    }
    @RequestMapping(value = "/user/del",method =RequestMethod.GET)
    @ResponseBody
    public Object getUserDel( Model model,@RequestParam("id") Integer id){
        System.out.println("进来了！！"+id);
        HashMap<String, String> resultMap = new HashMap<String, String>();
        UserService userService = new UserServiceImpl();
        if(id <= 0){
            resultMap.put("delResult", "notexist");
        }else{
            if(userService.deleteUserById(id)){
                resultMap.put("delResult", "true");
            }else{
                resultMap.put("delResult", "false");
            }
        }
//        model.addAttribute("resultMap",resultMap);
//        //把resultMap转换成json对象输出
//        response.setContentType("application/json");
//        PrintWriter outPrintWriter = response.getWriter();
//        outPrintWriter.write(JSONArray.toJSONString(resultMap));
//        outPrintWriter.flush();
//        outPrintWriter.close();

        return JSONArray.toJSONString(resultMap);
    }


    @RequestMapping("/userupd")
    public String getUserupd(String uid,Model model){
        if(!StringUtils.isNullOrEmpty(uid)){
            //调用后台方法得到user对象
            UserService userService = new UserServiceImpl();
            User user = userService.getUserById(uid);
            model.addAttribute("user",user);
            return "usermodify";
        }
        return "redirect:/user.do";
    }
    @RequestMapping(value = "/userupd",method = RequestMethod.POST)
    public String getUserupdd(User user,HttpSession session){
        UserService userService = new UserServiceImpl();
        user.setCreationDate(new Date());
        User user1 =(User) session.getAttribute(Constants.USER_SESSION);
        user.setModifyBy(user1.getId());
        if (userService.modify(user)){
            return "redirect:/user.do";
        }
        return "redirect:/userupd";
    }
    @RequestMapping("/userview")
    public String getUserview(String uid,HttpSession session){
        if(!StringUtils.isNullOrEmpty(uid)){
            //调用后台方法得到user对象
            UserService userService = new UserServiceImpl();
            User user = userService.getUserById(uid);
            session.setAttribute("user", user);
            return "userview";
        }
        return "redirect:/user.do";
    }
}


