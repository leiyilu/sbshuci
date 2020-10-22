package cn.smbms.controller;

import cn.smbms.pojo.Bill;
import cn.smbms.pojo.Provider;
import cn.smbms.pojo.User;
import cn.smbms.service.bill.BillService;
import cn.smbms.service.bill.BillServiceImpl;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.service.provider.ProviderServiceImpl;
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
public class BillController {
    private static Logger logger =Logger.getLogger(UserController.class);

    @RequestMapping("/bill.do")
    public String getBill(Model model,String queryProductName,String queryProviderId,String queryIsPayment){
        List<Provider> providerList = new ArrayList<Provider>();
        ProviderService providerService = new ProviderServiceImpl();
        providerList = providerService.getProviderList("","");
        model.addAttribute("providerList", providerList);

        if(StringUtils.isNullOrEmpty(queryProductName)){
            queryProductName = "";
        }

        List<Bill> billList = new ArrayList<Bill>();
        BillService billService = new BillServiceImpl();
        Bill bill = new Bill();
        if(StringUtils.isNullOrEmpty(queryIsPayment)){
            bill.setIsPayment(0);
        }else{
            bill.setIsPayment(Integer.parseInt(queryIsPayment));
        }

        if(StringUtils.isNullOrEmpty(queryProviderId)){
            bill.setProviderId(0);
        }else{
            bill.setProviderId(Integer.parseInt(queryProviderId));
        }
        bill.setProductName(queryProductName);
        billList = billService.getBillList(bill);
        model.addAttribute("billList", billList);
        model.addAttribute("queryProductName", queryProductName);
        model.addAttribute("queryProviderId", queryProviderId);
        model.addAttribute("queryIsPayment", queryIsPayment);
        return "billlist";
    }
    @RequestMapping("/billview")
    public String getBillview(String billid, Model model){
            BillService billService = new BillServiceImpl();
            Bill bill = null;
            bill = billService.getBillById(billid);
            model.addAttribute("bill", bill);
        return "billview";
    }
    @RequestMapping("/billmodify")
    public String getBill(String billid, HttpSession session){
        List<Provider> providerList = new ArrayList<Provider>();
        ProviderService providerService = new ProviderServiceImpl();
        providerList = providerService.getProviderList("","");
        session.setAttribute("providerList", providerList);
        BillService billService = new BillServiceImpl();
        Bill bill = null;
        bill = billService.getBillById(billid);
        session.setAttribute("bill", bill);
        return "billmodify";

    }
    @RequestMapping(value = "/billmodify",method = RequestMethod.POST)
    public String getBillModel(Bill bill, HttpSession session){
        bill.setCreationDate(new Date());
        BillService billService = new BillServiceImpl();
        bill.setCreatedBy(((User)session.getAttribute(Constants.USER_SESSION)).getId());
        if (billService.modify(bill)){
            return "redirect:/bill.do";
        }
        return "redirect:/billmodify";
    }
    @RequestMapping("/billadd")
    public String getbilladd(Model model){
        List<Provider> providerList = new ArrayList<Provider>();
        ProviderService providerService = new ProviderServiceImpl();
        providerList = providerService.getProviderList("","");
        model.addAttribute("providerList", providerList);
        return "billadd";
    }

    @RequestMapping(value = "/bill/billadd",method = RequestMethod.POST)
    public  String getadd(Bill bill,HttpSession session){
        bill.setCreationDate(new Date());
        bill.setCreatedBy(((User)session.getAttribute(Constants.USER_SESSION)).getId());
        BillService providerService = new BillServiceImpl();
        if (providerService.add(bill)){

            return "redirect:/bill.do";
        }else {
            return "billadd";
        }
    }

    @RequestMapping(value = "/bill/del",method = RequestMethod.GET)
    @ResponseBody       //用来转json格式的注解
    public Object getdelBill(@RequestParam("billid") Integer billid){
        BillService billService =new BillServiceImpl();
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if(billid <= 0){
            resultMap.put("delResult", "notexist");
        }else{
            if(billService.deleteBillById(billid+"")){
                resultMap.put("delResult", "true");
            }else{
                resultMap.put("delResult", "false");
            }
        }
        return JSONArray.toJSONString(resultMap);
    }

}
