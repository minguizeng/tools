package com.zmg.tools.api;

import com.zmg.tools.client.UDPClient;
import com.zmg.tools.util.CommUtils;
import com.zmg.tools.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@Controller
public class ToolsController {

    @Autowired
    private UDPClient udpClient;

    //wake on lan
    @ResponseBody
    @RequestMapping(path ={"/api/wol"},method = {RequestMethod.GET})
    public Map<String,Object> wol(HttpServletRequest request) throws Exception{
        Map<String,Object> result = new HashMap<>();
        String hostName = request.getParameter("hostName");
        Integer port = Integer.parseInt(request.getParameter("port"));
        String macAddress = request.getParameter("macAddress");
        byte[] buf = CommUtils.createMagicPacket(macAddress);
        udpClient.send(buf,0,buf.length,hostName,port);
        result.put("code",0);
        result.put("message","success");
        result.put("hostName",hostName);
        result.put("port",port);
        result.put("macAddress",macAddress);
        return result;
    }

    //get external ip
    @ResponseBody
    @RequestMapping(path = {"/api/getExtIp"},method = {RequestMethod.GET,RequestMethod.POST})
    public Map<String,Object> getExtIp(HttpServletRequest request)throws Exception{
        Map<String,Object> result = new HashMap<>();
        result.put("code",0);
        result.put("message","success");
        result.put("host",request.getRemoteHost());
        result.put("port",request.getRemotePort());
        return result;
    }
}
