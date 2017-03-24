# Kafka Eagle

This is an monitor system and monitor your kafka clusters, and visual consumer thread,offsets,owners etc.

When you install [Kafka Eagle](http://download.smartloli.org/), you can see the current consumer group,for each group the topics that they are consuming and the offsets, lag, logsize position of the group in each topic. This is useful to understand how fast you are consuming from a message queue and how quick the message queue is increase. This will help you debuging kafka producers and consumers or just to have an idea of what is going on in your system.

The system shows the trend of consumer and producer trends on the same day, so you can see what happened that day.

Here are a few Kafka Eagle system screenshots:

# List of Consumer Groups & Active Group Graph
![Consumer & Active Graph](https://ke.smartloli.org/res/consumer@2x.png)

# List of Topics Detail
![Topics](https://ke.smartloli.org/res/list@2x.png)

# Consumer & Producer Rate Chart
![Rate Chart](https://ke.smartloli.org/res/consumer_producer_rate@2x.png)

# Kafka Offset Types

Kafka is designed to be flexible on how the offsets are managed. Consumer can choose arbitrary storage and format to persist kafka offsets. Kafka Eagle currently support following popular storage format:
  * Zookeeper. Old version of Kafka (0.8.2 before) default storage in Zookeeper.
  * Kafka. New version of Kafka (0.10.0 in the future) default recommend storage in Kafka Topic(__consumer_offsets).
  
Each runtime instance of Kafka Eagle can only support a single type of storage format.

# Kafka SQL

Use the SQL statement to query the topic message log, and visualize the results, you can read [Kafka SQL](https://ke.smartloli.org/3.Manuals/9.KafkaSQL.html) to view the syntax.

# Quickstart

Please read [Kafka Eagle Install](https://ke.smartloli.org/2.Install/2.Installing.html) for setting up and running Kafka Eagle.

# Deploy

The project is a maven project that uses the Maven command to pack the deployment as follows:
```bash
mvn clean && mvn package
```
# More Information

Please see the [Kafka Eagle Manual](https://ke.smartloli.org) for for more information including:
  * System environment settings and installation instructions.
  * Information about how to use script command.
  * Visual group,topic,offset metadata information etc.
  * Metadata collection and log change information.
 
# Contributing

The Kafka Eagle is released under the Apache License and we welcome any contributions within this license. Any pull request is welcome and will be reviewed and merged as quickly as possible.

Since this is an open source tool, please comply with the relevant laws and regulations, the use of civilization.

# Committers

Thanks to the following members for maintaining the project.

|Alias |Github |Email |
|:-- |:-- |:-- |
|smartloli|[smartloli](https://github.com/smartloli)|smartloli.org@gmail.com|
|hexiang|[hexian55](https://github.com/hexian55)|hexiang55@gmail.com|
