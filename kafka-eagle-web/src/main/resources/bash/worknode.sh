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

DIALUP_PID=$KE_HOME/bin/worknode.pid

start()
{
 echo -n [$stime] INFO:  $"Starting $prog "
 echo "kafka eagle environment check ..."
 
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
 
 PID=`ps -ef | grep org.smartloli.kafka.eagle.core.task.rpc.server.WorkNodeServer | grep -v grep | awk '{print $2}'`
     
 if [ -n "$PID" ]; then
  echo "[$stime] Error: The Kafka Eagle[$PID] has started."
  exit 1
 fi
 
 bin=`dirname "$0"`
 export KE_HOME=`cd $bin/../; pwd`
 
 KE_HOME_CONF_DIR=$KE_HOME/conf
 CLASSPATH="${KE_HOME_CONF_DIR}"
 
 rm -rf $KE_HOME/kms/webapps/ke
 rm -rf $KE_HOME/kms/ROOT
 rm -rf $KE_HOME/kms/work
 mkdir -p $KE_HOME/kms/webapps/ke
 mkdir -p $KE_HOME/kms/ROOT
 cd $KE_HOME/kms/webapps/ke
 ${JAVA_HOME}/bin/jar -xvf $KE_HOME/kms/webapps/ke.war
 
 sleep 2
 
 for f in $KE_HOME/kms/webapps/ke/WEB-INF/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
 done
 
 LOG_DIR=${KE_HOME}/logs
 
 cd ${KE_HOME}
 CLASS=org.smartloli.kafka.eagle.core.task.rpc.server.WorkNodeServer
 ${JAVA_HOME}/bin/java -classpath "$CLASSPATH" $CLASS > ${LOG_DIR}/worknode.log 2>&1
 ps -ef | grep org.smartloli.kafka.eagle.core.task.rpc.server.WorkNodeServer | grep -v grep | awk '{print $2}' > $DIALUP_PID
 
}

stop()
{
 if [ -f $KE_HOME/bin/worknode.pid ];then
  SPID=`cat $KE_HOME/bin/worknode.pid`
  if [ "$SPID" != "" ];then
    kill -9  $SPID
    echo > $DIALUP_PID
   echo "[$stime] INFO: WorkNode Server Stop Success."
  fi
 fi
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
  SPID=`cat $KE_HOME/bin/worknode.pid`
  CheckProcessStata $SPID >/dev/null
  if [ $? != 0 ];then
   echo "[$stime] INFO : WorkNode Server has stopped, [$SPID] ."
  else
   echo "[$stime] INFO : WorkNode Server is running, [$SPID] ."
  fi

}

restart()
{
  echo "[$stime] INFO : WorkNode Server is stoping ... "
  stop
  echo "[$stime] INFO : WorkNode Server is starting ..."
  start
}

gc()
{	
  if [ -f $KE_HOME/bin/worknode.pid ];then
   SPID=`cat $KE_HOME/bin/worknode.pid`
   if [ "$SPID" != "" ];then
    echo "[$stime] INFO : WorkNode Server Process[$SPID] GC."
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

sdate()
{	
  if [ -f $KE_HOME/bin/worknode.pid ];then
   SPID=`cat $KE_HOME/bin/worknode.pid`
   if [ "$SPID" != "" ];then
    echo "[$stime] INFO : WorkNode Server Process[$SPID] Runtime."
    ps -eo pid,user,lstart | grep $SPID
   fi
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
