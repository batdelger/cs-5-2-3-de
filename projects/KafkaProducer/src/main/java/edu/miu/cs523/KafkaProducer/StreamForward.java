package edu.miu.cs523.KafkaProducer;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import io.ably.lib.realtime.AblyRealtime;
import io.ably.lib.realtime.Channel;
import io.ably.lib.realtime.ChannelBase.MessageListener;
import io.ably.lib.types.AblyException;
import io.ably.lib.types.Message;

@Component
public class StreamForward {

	@Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

	@Value(value = "${kafka.topicName}")
    private String topicName;
	
	public void send(String message){
        kafkaTemplate.send(topicName, message);
    }
	
	private void startChannelForCrypto(String cryptoName) {
		try {
			AblyRealtime realtime = new AblyRealtime("Hbqg4w.9riYMA:ltJGpqchKNhTfpOIoulpJCMyKVskOGdfzXpqtuMWM-0");
			
			String chanName = "[product:ably-coindesk/crypto-pricing]" + cryptoName + ":usd";
			
			Channel channel = realtime.channels.get(chanName);

			channel.subscribe(new MessageListener() {
				@Override
				public void onMessage(Message message) {
					String data =String.format("%s %s %s", LocalDateTime.now(), cryptoName, message.data);
					System.out.println(data);
					send(data);
				}
			});
		} catch (AblyException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		startChannelForCrypto("btc");
		startChannelForCrypto("eth");
	}

}
