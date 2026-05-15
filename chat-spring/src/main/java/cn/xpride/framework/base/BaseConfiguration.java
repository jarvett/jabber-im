package cn.xpride.framework.base;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import javax.servlet.MultipartConfigElement;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2WebMvc
public class BaseConfiguration implements WebMvcConfigurer {

    @Autowired
    private OpenApiExtensionResolver openApiExtensionResolver;

    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        SerializeConfig serializeConfig = SerializeConfig.globalInstance;
        serializeConfig.put(Long.class, ToStringSerializer.instance);
        serializeConfig.put(Long.TYPE, ToStringSerializer.instance);
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        fastJsonConfig.setSerializerFeatures(
            SerializerFeature.WriteMapNullValue,//值为null的也输出key
            SerializerFeature.WriteNullListAsEmpty
        );//这里的配置会影响openfeign里postjson格式
        List<MediaType> list = new ArrayList<>();
        list.add(MediaType.APPLICATION_JSON);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        fastConverter.setSupportedMediaTypes(list);
        return new HttpMessageConverters(fastConverter);
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(System.getProperty("java.io.tmpdir"));//配置上传图片的临时目录
        return factory.createMultipartConfig();
    }

    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        //添加head参数配置start
        ParameterBuilder tokenPar = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<>();
        tokenPar.name("token").description("登录令牌").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
        pars.add(tokenPar.build());
        //添加head参数配置end
        return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(new ApiInfoBuilder()
                .title("Chater-H5接口文档")
                .description("注意")
                .version("1.0")
                .build())
            //分组名称
            .select()
            //这里指定Controller扫描包路径
            .apis(RequestHandlerSelectors.basePackage("cn.xpride.framework"))
            .paths(PathSelectors.any())
            .build().globalOperationParameters(pars).extensions(openApiExtensionResolver.buildSettingExtensions());
    }

    //执行线程池的定义
    @Bean
    @Primary
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(64);
        return executor;
    }

    //配合vue打包到resource/static的项目运行
    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> myWebServerFactoryCustomizer(){
        return factory -> {
            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
            factory.addErrorPages(error404Page);
        };
    }

}
