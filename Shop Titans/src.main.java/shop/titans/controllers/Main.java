package shop.titans.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
		Game.init();
		Game.printBossSchedule();
		log.info("----------------------------------------------------------------");
		Game.printBestCraftPerComponent();
		log.info("----------------------------------------------------------------");
		Game.printBestCraftForComponent("Chronos Crystal");
		log.info("----------------------------------------------------------------");
	}
	
}
