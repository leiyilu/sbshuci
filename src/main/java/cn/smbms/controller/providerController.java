package cn.smbms.controller;

import cn.smbms.pojo.Provider;
import cn.smbms.pojo.User;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.service.provider.ProviderServiceImpl;
import cn.smbms.service.user.UserService;
import cn.smbms.service.user.UserServiceImpl;
import cn.smbms.tools.Constants;
import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
public class providerController {
    private static Logger logger =Logger.getLogger(providerController.class);

    @RequestMapping("/providerview")
    public String getprovider(String proid, Model model){
            ProviderService providerService = new ProviderServiceImpl();
            Provider provider = null;
            provider = providerService.getProviderById(proid);
            model.addAttribute("provider", provider);
        return "providerview";
    }

    @RequestMapping("/providermodify")
    public String getprovidermodify(String proid, HttpSession session){
            ProviderService providerService = new ProviderServiceImpl();
            Provider provider = null;
            provider = providerService.getProviderById(proid);
            session.setAttribute("provider", provider);
        return "providermodify";
    }

    @RequestMapping(value = "/providermod",method = RequestMethod.POST)
    public String getmodify(HttpSession session,Provider provider){
        System.out.println("进来了！！");
        provider.setCreationDate(new Date());
        provider.setCreatedBy(((User)session.getAttribute(Constants.USER_SESSION)).getId());
        ProviderService providerService = new ProviderServiceImpl();
        System.out.println("=============s"+provider);
        if(providerService.modify(provider)){
            System.out.println("??");
            return "redirect:/provider.do";
        }else{
            System.out.println("修改失败！");
            return "providermodify";
        }
    }
    @RequestMapping("/provider.do")
    public String getProvide(Model model,String queryProName,String queryProCode){
        if(StringUtils.isNullOrEmpty(queryProName)){
            queryProName = "";
        }
        if(StringUtils.isNullOrEmpty(queryProCode)){
            queryProCode = "";
        }
        List<Provider> providerList = new ArrayList<Provider>();
        ProviderService providerService = new ProviderServiceImpl();
        providerList = providerService.getProviderList(queryProName,queryProCode);
        model.addAttribute("providerList", providerList);
        model.addAttribute("queryProName", queryProName);
        model.addAttribute("queryProCode", queryProCode);
        return "providerlist";
    }
    @RequestMapping("/provideradd")
    public String getprovideradd(){

        return "provideradd";
    }

    @RequestMapping("/pro/provideradd")
    public String add(@RequestParam("provider") Provider provider,HttpSession session){
        provider.setCreationDate(new Date());
        provider.setCreatedBy(((User)session.getAttribute(Constants.USER_SESSION)).getId());
        ProviderService providerService = new ProviderServiceImpl();
        if (providerService.add(provider)){
            return "redirect:/provider.do";
        }else {
            return "provideradd";
        }
    }
    @RequestMapping(value = "/pro/prodel",method = RequestMethod.GET)
    @ResponseBody       //用来转json格式的注解
    public Object getdelBill(@RequestParam("proid") String proid){
        ProviderService providerService = new ProviderServiceImpl();
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if(!StringUtils.isNullOrEmpty(proid)){
            int flag = providerService.deleteProviderById(proid);
            if(flag == 0){//删除成功
                resultMap.put("delResult", "true");
            }else if(flag == -1){//删除失败
                resultMap.put("delResult", "false");
            }else if(flag > 0){//该供应商下有订单，不能删除，返回订单数
                resultMap.put("delResult", String.valueOf(flag));
            }
        }else{
            resultMap.put("delResult", "notexit");
        }
        return JSONArray.toJSONString(resultMap);
    }



}
