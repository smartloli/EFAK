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
# Create by Aug 26, 2023

export MALLOC_ARENA_MAX=1
export KE_JAVA_OPTS="-server -Xmx2g -Xms2g -XX:MaxGCPauseMillis=20 -XX:+UseG1GC -XX:MetaspaceSize=128m -XX:InitiatingHeapOccupancyPercent=35 -XX:G1HeapRegionSize=16M -XX:MinMetaspaceFreeRatio=50 -XX:MaxMetaspaceFreeRatio=80"
source ~/.bash_profile
source /etc/profile

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

DIALUP_PID=$KE_HOME/bin/ke.pid

start()
{
 echo -n [$stime] INFO:  $"Starting $prog "
 echo "EFAK( Eagle For Apache Kafka ) environment check ..."
 
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
 
 PID=`ps -ef | grep efak | grep KafkaEagle | awk '{print $2}'`
     
 if [ -n "$PID" ]; then
  echo "[$stime] Error: The EFAK[$PID] has started."
  exit 1
 fi
 
 bin=`dirname "$0"`
 export KE_HOME=`cd $bin/../; pwd`
 
 KE_HOME_CONF_DIR=$KE_HOME/config
 CLASSPATH="${KE_HOME_CONF_DIR}"
 LOG_DIR=${KE_HOME}/logs

 KE_JAR_PATH=$KE_HOME/libs/$(basename *.jar)
 
 cd ${KE_HOME}

 ${JAVA_HOME}/bin/java $KE_JAVA_OPTS -jar $KE_JAR_PATH 2>&1
 
 ADMIN="Account:admin ,Password:123456"
 
 echo "*******************************************************************"
 echo "* Kafka Eagle service started successfully."
 echo -e "* "$COLOR_G$ADMIN$RESET
 echo "*******************************************************************"
 echo "* <Usage> ke.sh [start|status|stop|restart|stats] </Usage>"
 echo "* <Usage> https://www.kafka-eagle.org/ </Usage>"
 echo "*******************************************************************"
 ps -ef | grep efak | grep KafkaEagle | awk '{print $2}' > $DIALUP_PID
}

startup()
{
 for i in `cat ${KE_HOME}/conf/works`
 do
  efak_stime=`date "+%Y-%m-%d %H:%M:%S"`
  ssh $i -q "source /etc/profile;source ~/.bash_profile;${KE_HOME}/bin/worknode.sh start>/dev/null" &
  echo "[$efak_stime] INFO: EFAK Slave WorkNodeServer-$i Start Success."
  sleep 1
 done
}

shutdown()
{
 for i in `cat ${KE_HOME}/conf/works`
 do
  efak_stime=`date "+%Y-%m-%d %H:%M:%S"`
  ssh $i -q "source /etc/profile;source ~/.bash_profile;${KE_HOME}/bin/worknode.sh stop>/dev/null" &
  echo "[$efak_stime] INFO: EFAK Slave WorkNodeServer-$i Stop Success."
  sleep 1
 done
}

list()
{
 starttime=`date "+%Y-%m-%d %H:%M:%S"`
 for i in `cat ${KE_HOME}/conf/works`
 do
  log=`ssh $i -q "source /etc/profile;source ~/.bash_profile;${KE_HOME}/bin/worknode.sh status" &`
  echo $log
 done
 endtime=`date +'%Y-%m-%d %H:%M:%S'`
 start_seconds=$(date --date="$starttime" +%s);
 end_seconds=$(date --date="$endtime" +%s);
 echo "Time taken: "$((end_seconds-start_seconds))" seconds."
}

stop()
{
 if [ -f $KE_HOME/bin/ke.pid ];then
  SPID=`cat $KE_HOME/bin/ke.pid`
  if [ "$SPID" != "" ];then
   ${KE_HOME}/kms/bin/shutdown.sh>/dev/null
    kill -9  $SPID
    echo > $DIALUP_PID
   echo "[$stime] INFO: EFAK-`hostname -i` Stop Success."
  fi
 fi
}

stats()
{
 if [ -f $KE_HOME/bin/ke.pid ];then
  SPID=`cat $KE_HOME/bin/ke.pid`
  if [ "$SPID" != "" ];then
   echo "===================== TCP Connections Count  =========================="
   netstat -natp|awk '{print $7}'|sort|uniq -c|sort -rn|grep $SPID
  fi
 fi
 
 echo "===================== ESTABLISHED/TIME_OUT Status  ===================="
 netstat -nat|grep ESTABLISHED|awk '{print$5}'|awk -F : '{print$1}'|sort|uniq -c|sort -rn
 
 echo "===================== Connection Number Of Different States ==========="
 netstat -an | awk '/^tcp/ {++y[$NF]} END {for(w in y) print w, y[w]}'
 
 echo "===================== End ============================================="
}

find()
{
  echo "===================== Find [$1] Path  ===================="
  for f in $KE_HOME/kms/webapps/ke/WEB-INF/lib/*.jar; do
   ${JAVA_HOME}/bin/jar vtf $f|grep $1 && echo $f;
  done	
  echo "===================== End ============================================="
}

CheckProcessStata()
{
  CPS_PID=$1
  if [ "$CPS_PID" != "" ] ;then
   CPS_PIDLIST=`ps -ef|grep $CPS_PID|grep -v grep|awk -F" " '{print $2}'`
  else
   CPS_PIDLIST=`ps -ef|grep "$CPS_PNAME"|grep -v grep|awk -F" " '{print $2}'`
  fi

  for CPS_i in `echo $CPS_PIDLIST`
  do
   if [ "$CPS_PID" = "" ] ;then
    CPS_i1="$CPS_PID"
   else
    CPS_i1="$CPS_i"
   fi

   if [ "$CPS_i1" = "$CPS_PID" ] ;then
    kill -0 $CPS_i >/dev/null 2>&1
    if [ $? != 0 ] ;then
     echo "[`date`] MC-10500: Process $i have Dead"
     kill -9 $CPS_i >/dev/null 2>&1

     return 1
    else
     return 0
    fi
   fi
  done
  echo "[`date`] MC-10502: Process $CPS_i is not exists"
  return 1
}

status()
{
  SPID=`cat $KE_HOME/bin/ke.pid`
  HOSTNAME=`hostname -i`
  CheckProcessStata $SPID >/dev/null
  if [ $? != 0 ];then
   echo "[$stime] INFO : EFAK-$HOSTNAME has stopped, [$SPID] ."
  else
   echo "[$stime] INFO : EFAK-$HOSTNAME is running, [$SPID] ."
  fi

}

restart()
{
  echo "[$stime] INFO : EFAK is stoping ... "
  stop
  echo "[$stime] INFO : EFAK is starting ..."
  start
}

gc()
{	
  if [ -f $KE_HOME/bin/ke.pid ];then
   SPID=`cat $KE_HOME/bin/ke.pid`
   if [ "$SPID" != "" ];then
    echo "[$stime] INFO : EFAK Process[$SPID] GC."
    ${JAVA_HOME}/bin/jstat -gcutil $SPID 1000
   fi
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

version()
{
 if [ -f $KE_HOME/bin/ke.pid ];then
  SPID=`cat $KE_HOME/bin/ke.pid`
  if [ "$SPID" != "" ];then
    cd ${KE_HOME}
    for f in $KE_HOME/kms/webapps/ke/WEB-INF/lib/*.jar; do
     JCLASSPATH=${JCLASSPATH}:$f;
    done
    JCLASS=org.smartloli.kafka.eagle.plugin.font.KafkaEagleVersion
    ${JAVA_HOME}/bin/java -classpath "$JCLASSPATH" $JCLASS 2>&1
  else
    echo "[$stime] ERROR: EFAK has stopped."
  fi
 fi
}

sdate()
{	
  if [ -f $KE_HOME/bin/ke.pid ];then
   SPID=`cat $KE_HOME/bin/ke.pid`
   if [ "$SPID" != "" ];then
    echo "[$stime] INFO : EFAK Process[$SPID] Runtime."
    ps -eo pid,user,lstart | grep $SPID
   fi
  fi
}

start_cluster()
{
 start
 HOSTNAME=`hostname -i`
 efak_stime=`date "+%Y-%m-%d %H:%M:%S"`
 echo "[$efak_stime] INFO: EFAK Master-$HOSTNAME WebConsole Start Success."
 sleep 1
 for i in `cat ${KE_HOME}/conf/works`
 do
  ssh $i -q "source /etc/profile;source ~/.bash_profile;${KE_HOME}/bin/ke.sh start>/dev/null" &
  echo "[$stime] INFO: EFAK Slave-$i WebConsole Start Success."
  sleep 1
 done
 sleep 1
 source /etc/profile
 source ~/.bash_profile
 ${KE_HOME}/bin/worknode.sh start>/dev/null &
 echo "[$efak_stime] INFO: EFAK Master WorkNodeServer Start Success."
 sleep 1
 startup
}

status_cluster()
{
 efak_stime=`date "+%Y-%m-%d %H:%M:%S"`
 echo "[$efak_stime] INFO: EFAK WebConsole Status."
 status
 sleep 1
 for i in `cat ${KE_HOME}/conf/works`
 do
  log=`ssh $i -q "source /etc/profile;source ~/.bash_profile;${KE_HOME}/bin/ke.sh status" &`
  echo $log
  sleep 1
 done
 sleep 1
 echo "[$efak_stime] INFO: EFAK WorkNodeServer Status."
 source /etc/profile
 source ~/.bash_profile
 ${KE_HOME}/bin/worknode.sh status &
 list
}

stop_cluster()
{
 stop
 efak_stime=`date "+%Y-%m-%d %H:%M:%S"`
 echo "[$efak_stime] INFO: EFAK WebConsole has stopped ."
 sleep 1
 for i in `cat ${KE_HOME}/conf/works`
 do
  log=`ssh $i -q "source /etc/profile;source ~/.bash_profile;${KE_HOME}/bin/ke.sh stop" &`
  echo $log
  sleep 1
 done
 sleep 1
 source /etc/profile
 source ~/.bash_profile
 ${KE_HOME}/bin/worknode.sh stop>/dev/null &
 echo "[$efak_stime] INFO: EFAK WorkNodeServer Stop Success."
 sleep 1
 shutdown
}

restart_cluster()
{
  echo "[$stime] INFO : EFAK cluster is stoping ... "
  stop_cluster
  echo "[$stime] INFO : EFAK cluster is starting ..."
  start_cluster
}

cluster()
{
  case "$1" in
  start)
      start_cluster
      ;;
  stop)
      stop_cluster
      ;;
  status)
      status_cluster
      ;;
  restart)
      restart_cluster
      ;;
  *)
      echo $"Usage: $0 {start|stop|status|restart}"
esac
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
  stats)
       stats
      ;;
  find)
       find $2
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
  version)
      version
      ;;
  sdate)
      sdate
      ;;
  cluster)
      cluster $2
      ;;
  *)
      echo $"Usage: $0 {start|stop|restart|status|stats|find|gc|jdk|version|sdate|cluster}"
      RETVAL=1
esac
exit $RETVAL
