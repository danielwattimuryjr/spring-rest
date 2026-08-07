package danielwattimury.rest_api.configuration;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import danielwattimury.rest_api.resolver.UserArgumentResolver;
import lombok.Getter;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Getter
    private UserArgumentResolver argumentResolver;

    public WebConfiguration(UserArgumentResolver userArgumentResolver) {
        this.argumentResolver = userArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
        resolvers.add(argumentResolver);
    }

}
