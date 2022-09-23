package edu.miu.cs523;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.spark.JavaHBaseContext;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

public class Main
{

	private static final String TABLE_NAME = "cryptoprice";
	    
	public static void main(String[] args) throws Exception
	{
		SparkConf sparkConf =
                new SparkConf().setMaster("local[*]").setAppName("CryptopriceKafkaToHbase");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);

        //Kafka configuration
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", "localhost:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "gid1");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", true);

        Collection<String> topics = Arrays.asList("cryptoprice");

        try {
            JavaStreamingContext jssc =
                    new JavaStreamingContext(jsc, new Duration(1000));

            JavaInputDStream<ConsumerRecord<String, String>> stream =
                    KafkaUtils.createDirectStream(
                            jssc,
                            LocationStrategies.PreferConsistent(),
                            ConsumerStrategies.Subscribe(topics, kafkaParams)
                    );

            JavaDStream<String[]> javaDstream = stream.map(x-> x.value().split(" "));

            JavaDStream<String[]> filtered = javaDstream.filter(record -> record.length >= 3);
            
            //HBase configuration
            Configuration conf = HBaseConfiguration.create();
            conf.addResource(new Path("/etc/hadoop/conf.cloudera.hdfs/core-site.xml"));
            conf.addResource(new Path("/etc/hbase/conf.cloudera.hbase/hbase-site.xml"));

            JavaHBaseContext hbaseContext = new JavaHBaseContext(jsc, conf);
            
            hbaseContext.streamBulkPut(filtered,
                    TableName.valueOf(TABLE_NAME),
                    new PutFunction());
            
            jssc.start();
            try {
                jssc.awaitTermination();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            jsc.stop();
        }
    }

    public static class PutFunction implements Function<String[], Put> {

        private static final long serialVersionUID = 1L;

        @Override
        public Put call(String[] data) {
        	String datetime = data[0];
        	String currency = data[1];
        	Double price = Double.valueOf(data[2]);
        	
            Put put = new Put(Bytes.toBytes(data[1] + "_" + data[0]));
            put.addColumn(Bytes.toBytes("price"), Bytes.toBytes("datetime"), Bytes.toBytes(datetime));
            put.addColumn(Bytes.toBytes("price"), Bytes.toBytes("currency"), Bytes.toBytes(currency));
            put.addColumn(Bytes.toBytes("price"), Bytes.toBytes("price"), Bytes.toBytes(price));
            
            return put;
        }
    }
}
