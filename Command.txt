# run StreamReceiver
$ java -jar /home/botgonbaatar/eclipse-workspace/KafkaProducer/target/KafkaProducer-0.0.1-SNAPSHOT.jar \
edu.miu.cs523.KafkaProducer.StreamReceiverApplication

# check kafka topic
$ ~/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic cryptoprice --from-beginning


# create table HBase table
$ hbase shell
disable 'cryptoprice'
drop 'cryptoprice'
create 'cryptoprice', 'price'
count 'cryptoprice'


# run SparkStreaming+Kafka to HBase (local)
$ java -jar /home/botgonbaatar/eclipse-workspace/KafkaConsumerHBase/target/KafkaConsumerHBase-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
edu.miu.cs523.KafkaConsumerHBase.Main


# run SparkStreaming+Kafka to HBase (yarn client mode)
export JAVA_HOME=/usr/lib/jvm/jre-1.8.0-openjdk.x86_64 && \
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop && \
export YARN_CONF_DIR=$HADOOP_HOME/etc/hadoop && \
export SPARK_KAFKA_VERSION=0.10 && \
spark-submit \
--class edu.miu.cs523.Main \
--master yarn \
--deploy-mode client \
/hostdir/KafkaToHBase-0.0.1-SNAPSHOT-jar-with-dependencies.jar

# create hive Table
$ hive
DROP TABLE IF EXISTS hbaseCryptoPrice;

CREATE EXTERNAL TABLE hbaseCryptoPrice (
    id string,
    currency string,
    price double,
    ts string)
 STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
 WITH SERDEPROPERTIES (
   "hbase.columns.mapping" =
   ":key,price:currency,price:price#b,price:datetime"
 )
 TBLPROPERTIES("hbase.table.name" = "cryptoprice");