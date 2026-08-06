package danielwattimury.rest_api.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import danielwattimury.rest_api.constants.ApiConstants;

@Configuration
public class ApiConfiguration implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(ApiConstants.API_BASE_PATH, HandlerTypePredicate.forAnnotation(RestController.class));
    }
}
