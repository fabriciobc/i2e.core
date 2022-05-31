package br.com.i2e.core;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

//@ComponentScan(basePackages = { "br.com.i2e.common, br.com.i2e.core" })
@EntityScan({ "br.com.i2e.common.model", "br.com.i2e.common.model.catalog", "br.com.i2e.common.model.order"  })
@SpringBootApplication
public class I2EApplication {

	public static void main(String[] args) {
		SpringApplication.run(I2EApplication.class, args);
	}
	
	@Bean
	public ModelMapper modelMapper() {
	    return new ModelMapper();
	}

}
