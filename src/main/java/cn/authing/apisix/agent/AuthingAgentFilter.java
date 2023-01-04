package cn.authing.apisix.agent;

import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.filter.PluginFilter;
import org.apache.apisix.plugin.runner.filter.PluginFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gao FeiHu
 * @email gaofeihu@authing.cn
 */
@Component
public class AuthingAgentFilter implements PluginFilter {

    private final Logger logger = LoggerFactory.getLogger(AuthingAgentFilter.class);

    @Override
    public String name() {
        return "authing_agent";
    }

    @Override
    public void filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {

        try {

            logger.info("authing_agent is running");

            // get conf of current filter
            String configStr = request.getConfig(this);
            Gson gson = new Gson();

            Map<String, Object> conf = new HashMap<>();
            // convert according to the actual configured conf type
            conf = gson.fromJson(configStr, conf.getClass());

            logger.info("request = {} ", JSONUtil.toJsonStr(request));
            logger.info("conf = {} ", configStr);
//            logger.info("response = {} ", JSONUtil.toJsonStr(response));
//            logger.info("chain = {} ", JSONUtil.toJsonStr(chain));

            HashMap<String, Object> paramMap = new HashMap<>();
            HashMap<String, Object> requestMap = new HashMap<>();
            requestMap.put("uri", request.getPath());
            requestMap.put("method", request.getMethod());
            requestMap.put("request_id", request.getRequestId());
            requestMap.put("remote_addr", request.getVars("remote_addr"));
            requestMap.put("source_ip", request.getSourceIP());
            requestMap.put("args", request.getArgs());
            requestMap.put("headers", request.getHeaders());
            // 组装 agent 参数
            paramMap.put("request", requestMap);
            paramMap.put("pluginConfig", conf);


            cn.hutool.http.HttpResponse httpResponse = cn.hutool.http.HttpRequest.post((String) conf.get("url"))
                    .body(new Gson().toJson(paramMap))
                    .timeout(5000)
                    .execute();

            logger.info("response = {} ", JSONUtil.toJsonStr(httpResponse));

            if (!httpResponse.isOk()) {
                httpResponse.headers().forEach((k, v) -> {
                    response.setReqHeader(k, v.get(0));
                });
                response.setStatusCode(httpResponse.getStatus());
                response.setBody(httpResponse.body());
            }
            // TODO 需要验证下在不放行请求的时候是否需要调用
            chain.filter(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatus.HTTP_INTERNAL_ERROR);
            response.setBody(e.getMessage());
            chain.filter(request, response);
        }
    }

    /**
     * If you need to fetch some Nginx variables in the current plugin, you will need to declare them in this function.
     *
     * @return a list of Nginx variables that need to be called in this plugin
     */
    @Override
    public List<String> requiredVars() {
        List<String> vars = new ArrayList<>();
        vars.add("remote_addr");
        vars.add("server_port");
        return vars;
    }

    /**
     * If you need to fetch request body in the current plugin, you will need to return true in this function.
     */
    @Override
    public Boolean requiredBody() {
        return true;
    }
}