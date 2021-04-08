package com.epam.task1.solution;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONString;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
import java.net.SocketTimeoutException;
import java.rmi.ConnectIOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/solution")
public class SolutionController {


    //请求缓存
    private Map<String, Object> provinces = null;
    private Map<String, Map<String, Object>> citys = new HashMap<String, Map<String, Object>>();
    private Map<String, Map<String, Object>> countrys = new HashMap<String, Map<String, Object>>();

    private TokenServer tokenServer;
    {
            tokenServer = TokenServer.newBuilder();
            tokenServer.maxFlowRate(100);
            tokenServer.avgFlowRate(50);
//            tokenServer
            tokenServer.build();
    }

    @RequestMapping("/index")
    @ResponseBody
    public Optional getTemperature(String province, String city, String country) throws InterruptedException {


        if (!tokenServer.tryAcquire()) {
            return Optional.of("服务器繁忙，请稍后再试");
        }
        //构建Http请求
        String url = "http://www.weather.com.cn/data/city3jdata/china.html";
        try {


            if (null == provinces) {
                //push to the cahe
                this.provinces = getOptionalResult(url);
            }
            String proviceCode = getCode(province, this.provinces);
            if (null != proviceCode) {
                String cityCode = getCityCodeByprovinceCode(proviceCode, city);
                if (null != cityCode) {
                    String contryCode = getContryCodeByCityCode(proviceCode + cityCode, country);
                    if (null != contryCode) {
                        return Optional.of(
                                getOptionalResult("http://www.weather.com.cn/data/sk/"
                                        + proviceCode + cityCode + contryCode + ".html", null).
                                        get("weatherinfo", JSONObject.class).get("temp"));
                    } else {
                        return Optional.of("县不存在");
                    }
                } else {
                    return Optional.of("城市不存在");
                }
            } else {
                return Optional.of("省份不存在");
            }
        } catch (InterruptedException e) {

            e.printStackTrace();
            return Optional.of("请求超时");
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.of("服务器错误");
        }

    }

    private String getContryCodeByCityCode(String cityCode, String country) throws InterruptedException {
        Map<String, Object> countrys = this.countrys.get(cityCode);
        if (null != countrys) {
            return getCode(country, countrys);
        }
        String cityUrl = "http://www.weather.com.cn/data/city3jdata/station/" + cityCode + ".html";
        countrys = getOptionalResult(cityUrl);
        this.countrys.put(cityCode, countrys);

        return getCode(country, countrys);
    }

    private String getCityCodeByprovinceCode(String proviceCode, String city) throws InterruptedException {
        Map<String, Object> citys = this.citys.get(proviceCode);
        if (null != citys) {
            return getCode(city, citys);
        }
        String cityUrl = "http://www.weather.com.cn/data/city3jdata/provshi/" + proviceCode + ".html";
        citys = getOptionalResult(cityUrl);

        this.citys.put(proviceCode, citys);
        return getCode(city, citys);
    }

    //get the code from the collentions
    private String getCode(String province, Map<String, Object> maps) {
        //获取指定省份 存在则返回code
        Iterator<Map.Entry<String, Object>> entries = maps.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Object> entry = entries.next();
            if (province.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    //get url returns
    private JSONObject getOptionalResult(String url) throws InterruptedException {
        //default try 5 times
        return this.getOptionalResult(url, 5);
    }

    private JSONObject getOptionalResult(String url, Integer times) throws InterruptedException {


        if (null == times) {
            //we have five time to get the url returns
            times = 5;
        }
        HttpResponse result = HttpRequest.get(url)
                .header(Header.USER_AGENT, "Native http")//头信息，多个头信息多次调用此方法即可
                .setConnectionTimeout(2000)
                .execute();

        if (!result.isOk() ) {
            if(times==0){
                throw new InterruptedException();
            }
            times--;
            Thread.sleep(500L);
            getOptionalResult(url, times);
        }
        return new JSONObject(result.body()
                , true);
    }


}
