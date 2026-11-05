//package com.dianora.backend.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@Configuration
//public class ProductImageResourceConfig implements WebMvcConfigurer {
//
//    @Value("${dianora.upload-dir:uploads/products}")
//    private String uploadDirectory;
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
//
//        registry.addResourceHandler("/uploads/products/**")
//                .addResourceLocations(uploadPath.toUri().toString());
//    }
//}

package com.dianora.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ProductImageResourceConfig implements WebMvcConfigurer {

    @Value("${dianora.upload-dir:uploads/products}")
    private String uploadDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();

        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(uploadPath.toUri().toString());
    }
}
