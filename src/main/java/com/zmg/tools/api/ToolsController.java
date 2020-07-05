package com.zmg.tools.api;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.zmg.tools.client.UDPClient;
import com.zmg.tools.util.CommUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
@Controller
public class ToolsController {

    @Autowired
    private UDPClient udpClient;

    @Value("${token}")
    private String _token;

    private Map<String,Map<String, ConcurrentLinkedQueue<JSONObject>>> messageQueueMap = new ConcurrentHashMap<>();

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

    // get remote host
    @ResponseBody
    @RequestMapping(path = {"/api/getRemoteHost"},method = {RequestMethod.GET,RequestMethod.POST})
    public Map<String,Object> getRemoteHost(HttpServletRequest request)throws Exception{
        Map<String,Object> result = new HashMap<>();
        result.put("code",0);
        result.put("message","success");
        result.put("host",request.getRemoteHost());
        result.put("port",request.getRemotePort());
        return result;
    }

    //push message
    @ResponseBody
    @RequestMapping(path = {"/api/pushMessage"},method = {RequestMethod.POST})
    public Map<String,Object> pushMessage(HttpServletRequest request) throws Exception{
        Map<String,Object> result = new HashMap<>();
        String appId = request.getParameter("appId");
        String queueName = request.getParameter("queueName");
        String token = request.getParameter("token");
        JSONObject messageObj = JSONObject.parseObject(request.getParameter("message"));


        Map<String,ConcurrentLinkedQueue<JSONObject>> queueMap = messageQueueMap.containsKey(appId)?messageQueueMap.get(appId):new ConcurrentHashMap<>();
        messageQueueMap.put(appId,queueMap);
        ConcurrentLinkedQueue<JSONObject> queue = queueMap.containsKey(queueName)?queueMap.get(queueName):new ConcurrentLinkedQueue<>();
        queueMap.put(queueName,queue);

        if(!token.equalsIgnoreCase(_token)){
           result.put("code",1);
           result.put("message","invalid token");
           return result;
        }

        queue.add(messageObj);
        result.put("code",0);
        result.put("message","success");
        return result;
    }

    //pull message
    @RequestMapping(path = {"/api/pullMessage"},method = {RequestMethod.GET,RequestMethod.POST})
    public Map<String,Object> pullMessage(HttpServletRequest request) throws Exception{
        Map<String,Object> result = new HashMap<>();
        String appId = request.getParameter("appId");
        String queueName = request.getParameter("queueName");
        String token = request.getParameter("token");

        if(!messageQueueMap.containsKey(appId)){
            messageQueueMap.put(appId,new ConcurrentHashMap<>());
        }

        if(!messageQueueMap.get(appId).containsKey(queueName)){
            messageQueueMap.get(appId).put(queueName,new ConcurrentLinkedQueue<>());
        }

        if(!token.equalsIgnoreCase(_token)){
            result.put("code",1);
            result.put("message","invalid token");
            return result;
        }
        ConcurrentLinkedQueue<JSONObject> queue = messageQueueMap.get(appId).get(queueName);

        if(!queue.iterator().hasNext()){
            result.put("code",1);
            result.put("message","queue is empty");
            return result;
        }

        result.put("code",0);
        result.put("message",queue.iterator().next().toJSONString());
        queue.remove();
        return result;
    }
}
