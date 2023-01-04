package cn.authing.apisix.agent;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Gao FeiHu
 * @email gaofeihu@authing.cn
 */
@SpringBootApplication(scanBasePackages = {"cn.authing.apisix", "org.apache.apisix.plugin.runner"})
public class AuthingAgentApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AuthingAgentApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}
