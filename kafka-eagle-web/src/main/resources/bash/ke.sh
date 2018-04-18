#! /bin/bash

# source function library
#. /etc/rc.d/init.d/functions

export MALLOC_ARENA_MAX=1

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

	bin=`dirname "$0"`
	export KE_HOME=`cd $bin/../; pwd`

	KE_HOME_CONF_DIR=$KE_HOME/conf
	CLASSPATH="${KE_HOME_CONF_DIR}"
	
	rm -rf $KE_HOME/kms/webapps/ke
	rm -rf $KE_HOME/kms/work
	mkdir -p $KE_HOME/kms/webapps/ke
	cd $KE_HOME/kms/webapps/ke
	${JAVA_HOME}/bin/jar -xvf $KE_HOME/kms/webapps/ke.war

	sleep 2
	
	for f in $KE_HOME/kms/webapps/ke/WEB-INF/lib/*.jar; do
  		CLASSPATH=${CLASSPATH}:$f;
	done

	LOG_DIR=${KE_HOME}/logs
	
	cd ${KE_HOME}
	CLASS=org.smartloli.kafka.eagle.plugin.server.TomcatServerListen
	${JAVA_HOME}/bin/java -classpath "$CLASSPATH" $CLASS > ${LOG_DIR}/ke.out 2>&1
	echo "*******************************************************************"
    echo "* Kafka Eagle system monitor port successful... "
	echo "*******************************************************************"
	sleep 3
	rm -rf ${KE_HOME}/kms/webapps/ke/WEB-INF/classes/*.properties
	cp ${KE_HOME}/conf/*.properties ${KE_HOME}/kms/webapps/ke/WEB-INF/classes/
	sleep 3
	rm -rf ${KE_HOME}/kms/logs/*
	chmod +x ${KE_HOME}/kms/bin/*.sh
	sleep 2
	nohup ${KE_HOME}/kms/bin/startup.sh >> ${LOG_DIR}/ke.out 2>&1
	ret=$?
	echo "[$stime] INFO: Status Code["$ret"]"
	isexit $ret
	
	CLASS=org.smartloli.kafka.eagle.plugin.font.KafkaEagleVersion
	${JAVA_HOME}/bin/java -classpath "$CLASSPATH" $CLASS 2>&1
	
	ADMIN="Account:admin ,Password:123456"
	
	echo "*******************************************************************"
    	echo "* Kafka Eagle Service has started success."
    	echo "* Welcome, Now you can visit 'http://<your_host_or_ip>:port/ke'"
    	echo -e "* "$COLOR_G$ADMIN$RESET
	echo "*******************************************************************"
    	echo "* <Usage> ke.sh [start|status|stop|restart|stats] </Usage>"
    	echo "* <Usage> http://ke.smartloli.org/ </Usage>"
	echo "*******************************************************************"
	ps -ef | grep ${KE_HOME}/kms/bin/ | grep -v grep | awk '{print $2}' > $DIALUP_PID
	rm -rf ${LOG_DIR}/ke_console.out
	ln -s ${KE_HOME}/kms/logs/catalina.out ${LOG_DIR}/ke_console.out
}

stop()
{
	 if [ -f $KE_HOME/bin/ke.pid ];then
	 				SPID=`cat $KE_HOME/bin/ke.pid`
					  if [ "$SPID" != "" ];then
                         ${KE_HOME}/kms/bin/shutdown.sh
                         kill -9  $SPID
                         echo > $DIALUP_PID
						 echo "[$stime] INFO: Stop Success."
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
            #kill -s 0 $CPS_i
            kill -0 $CPS_i >/dev/null 2>&1
            if [ $? != 0 ] ;then
                echo "[`date`] MC-10500: Process $i have Dead"
                kill -9 $CPS_i >/dev/null 2>&1

                return 1
            else
                #echo "[`date`]: Process is alive"
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
  CheckProcessStata $SPID >/dev/null
  if [ $? != 0 ];then
	echo "[$stime] INFO : Kafka Eagle has stopped, [$SPID] ."
  else
	echo "[$stime] INFO : Kafka Eagle is running, [$SPID] ."
  fi

}

restart()
{
    echo "[$stime] INFO : Kafka Eagle is stoping ... "
    stop
    echo "[$stime] INFO : Kafka Eagle is starting ..."
    start
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
    *)
        echo $"Usage: $0 {start|stop|restart|status|stats}"
        RETVAL=1
esac
exit $RETVAL
