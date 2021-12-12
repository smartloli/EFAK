#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Used to manage Kafka Eagle system start, stop, restart and other operations
#
#    http://www.kafka-eagle.org/
#
# Author smartloli.
# Update by Jul 27, 2019
# Update by Dec 11, 2021 -- add grep WorkNodeServer

export MALLOC_ARENA_MAX=1
export KE_JAVA_OPTS="-server -Xmx2g -Xms2g -XX:MaxGCPauseMillis=20 -XX:+UseG1GC -XX:MetaspaceSize=128m -XX:InitiatingHeapOccupancyPercent=35 -XX:G1HeapRegionSize=16M -XX:MinMetaspaceFreeRatio=50 -XX:MaxMetaspaceFreeRatio=80"

stime=`date "+%Y-%m-%d %H:%M:%S"`

COLOR_G="\x1b[0;32m"  # green
COLOR_R="\x1b[1;31m"  # red
RESET="\x1b[0m"
STR_ERR="[Oops! Error occurred! Please see the message upside!]"
STR_OK="[Job done!]"
MSG_ERR=$COLOR_R$STR_ERR$RESET
MSG_OK=$COLOR_G$STR_OK$RESET
isexit()
{
 if [[ $1 -eq 0 ]];then
  echo -e [$stime] INFO: $MSG_OK
 else
  echo -e [$stime] ERROR: $MSG_ERR
  exit 1
 fi
}

start()
{
 echo -n [$stime] INFO:  $"Starting $prog "
 echo "EFAK ( Eagle For Apache Kafka ) environment check ..."
 
 if [ "$KE_HOME" = "" ]; then
  echo "[$stime] Error: The KE_HOME environment variable is not defined correctly."
  echo "[$stime] Error: This environment variable is needed to run this program."
  exit 1
 fi
 
 if [ "$JAVA_HOME" = "" ]; then
  echo "[$stime] Error: The JAVA_HOME environment variable is not defined correctly."
  echo "[$stime] Error: This environment variable is needed to run this program."
  exit 1
 fi
 
 PID=`ps -ef | grep ${KE_HOME}/kms | grep -v grep | grep WorkNodeServer | awk '{print $2}'`
     
 if [ -n "$PID" ]; then
  echo "[$stime] Error: The WorkNodeServer[$PID] has started."
  exit 1
 fi
 
 bin=`dirname "$0"`
 export KE_HOME_WNS=`cd $bin/../; pwd`
 
 KE_HOME_WNS_CONF_DIR=$KE_HOME_WNS/conf
 WNS_CLASSPATH="${KE_HOME_WNS_CONF_DIR}"
 
 sleep 2
 
 for f in $KE_HOME/kms/webapps/ke/WEB-INF/lib/*.jar; do
  WNS_CLASSPATH=${WNS_CLASSPATH}:$f;
 done
 
 WNS_LOG_DIR=${KE_HOME}/logs
 
 cd ${KE_HOME}
 WNS_CLASS=org.smartloli.kafka.eagle.core.task.rpc.server.WorkNodeServer
 ${JAVA_HOME}/bin/java -classpath "$WNS_CLASSPATH" $WNS_CLASS > ${WNS_LOG_DIR}/worknode.log 2>&1
}

stop()
{
 SPID=`ps -ef | grep ${KE_HOME}/kms | grep -v grep | grep WorkNodeServer | awk '{print $2}'`
 if [ "$SPID" != "" ];then
  kill -9  $SPID
  echo "[$stime] INFO: WorkNodeServer-`hostname -i` Stop Success."
 fi
}

status()
{
  SPID=`ps -ef | grep ${KE_HOME}/kms | grep -v grep | grep WorkNodeServer | awk '{print $2}'`
  HOSTNAME=`hostname -i`
  if [ "$SPID" = "" ] ;then
    echo "[$stime] INFO : WorkNodeServer-$HOSTNAME has stopped, [$SPID] ."
  else
    PID_EXIST=$(ps aux | awk '{print $2}'| grep -w $SPID)
    if [ ! $PID_EXIST ];then
      echo "[$stime] INFO : WorkNodeServer-$HOSTNAME has stopped, [$SPID] ."
    else
      echo "[$stime] INFO : WorkNodeServer-$HOSTNAME is running, [$SPID] ."
    fi
  fi
}

restart()
{
  echo "[$stime] INFO : WorkNodeServer is stoping ... "
  stop
  echo "[$stime] INFO : WorkNodeServer is starting ..."
  start
}

gc()
{	
  SPID=`ps -ef | grep ${KE_HOME}/kms | grep -v grep | grep WorkNodeServer | awk '{print $2}'`
  if [ "$SPID" != "" ];then
   echo "[$stime] INFO : WorkNodeServer Process[$SPID] GC."
   ${JAVA_HOME}/bin/jstat -gcutil $SPID 1000
  fi
}

jdk()
{
  for f in $KE_HOME/kms/webapps/ke/WEB-INF/lib/*.jar; do
   CLASSPATH=${CLASSPATH}:$f;
  done
  
  CLASS_JDK_ENV=org.smartloli.kafka.eagle.plugin.net.KafkaEagleJDK
  ${JAVA_HOME}/bin/java -classpath "$CLASSPATH" $CLASS_JDK_ENV 2>&1
}

sdate()
{	
  SPID=`ps -ef | grep ${KE_HOME}/kms | grep -v grep | grep WorkNodeServer | awk '{print $2}'`
  if [ "$SPID" != "" ];then
   echo "[$stime] INFO : WorkNodeServer Process[$SPID] Runtime."
   ps -eo pid,user,lstart | grep $SPID
  fi
}

case "$1" in
  start)
      start
      ;;
  stop)
      stop
      ;;
  status)
       status
      ;;
  restart)
      restart
      ;;
  gc)
      gc
      ;;
  jdk)
      jdk
      ;;	
  sdate)
      sdate
      ;;
  *)
      echo $"Usage: $0 {start|stop|restart|status|gc|jdk|sdate}"
      RETVAL=1
esac
exit $RETVAL