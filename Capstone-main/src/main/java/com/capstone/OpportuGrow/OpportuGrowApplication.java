package com.capstone.OpportuGrow;

import com.capstone.OpportuGrow.Config.StripeConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StripeConfig.class)
public class OpportuGrowApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpportuGrowApplication.class, args);
	}

}
