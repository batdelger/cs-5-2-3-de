package edu.miu.cs523.KafkaProducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import io.ably.lib.types.AblyException;



@SpringBootApplication
public class StreamReceiverApplication {


	public static void main(String[] args) throws AblyException {
		ConfigurableApplicationContext context = SpringApplication.run(StreamReceiverApplication.class, args);
		StreamForward streamForward = (StreamForward) context.getBean(StreamForward.class);
		streamForward.start();
	}

}
