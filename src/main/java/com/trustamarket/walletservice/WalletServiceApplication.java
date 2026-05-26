package com.trustamarket.walletservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.zaxxer.hikari.HikariConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class WalletServiceApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(WalletServiceApplication.class, args);

		HikariConfig hikariConfig = run.getBean(HikariConfig.class);
		log.info("[HIKARI Maximum PoolSize]" + hikariConfig.getMaximumPoolSize());
	}
}
