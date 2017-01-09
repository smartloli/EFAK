#! /bin/bash

# source function library
#. /etc/rc.d/init.d/functions

DIALUP_PID=$KE_HOME/bin/ipc.pid
start()
{
    echo -n $"Starting $prog: "
    echo "Ke service check ..."
    
    if [ "$KE_HOME" = "" ]; then
  		echo "Error: KE_HOME is not set."
  		exit 1
	fi
	
	if [ "$JAVA_HOME" = "" ]; then
  		echo "Error: JAVA_HOME is not set."
  		exit 1
	fi

	bin=`dirname "$0"`
	export KE_HOME=`cd $bin/../; pwd`

	KE_HOME_CONF_DIR=$KE_HOME/conf
	CLASSPATH="${KE_HOME_CONF_DIR}"

	for f in $KE_HOME/lib/*.jar; do
  		CLASSPATH=${CLASSPATH}:$f;
	done

	LOG_DIR=${KE_HOME}/logs
	
	cd ${KE_HOME}
	CLASS=org.smartloli.kafka.eagle.plugins.ipc.RpcServer
	nohup ${JAVA_HOME}/bin/java -classpath "$CLASSPATH" $CLASS > ${LOG_DIR}/ipc.out 2>&1 < /dev/null & new_agent_pid=$!
	echo "$new_agent_pid" > $DIALUP_PID
}

stop()
{
	 if [ -f $KE_HOME/bin/ipc.pid ];then
                    SPID=`cat $KE_HOME/bin/ipc.pid`
					  if [ "$SPID" != "" ];then
                         ${KE_HOME}/kms/bin/shutdown.sh
                         kill -9  $SPID
						 echo  > $DIALUP_PID
						 echo "stop success"
					  fi
	 fi
}

stats()
{
	if [ -f $KE_HOME/bin/ipc.pid ];then
                    SPID=`cat $KE_HOME/bin/ipc.pid`
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
  SPID=`cat $KE_HOME/bin/ipc.pid`
  CheckProcessStata $SPID >/dev/null
  if [ $? != 0 ];then
	echo "unixdialup:{$SPID}  Stopped ...."
  else
	echo "unixdialup:{$SPID} Running Normal."
  fi

}

restart()
{
    echo "stoping ... "
    stop
    echo "staring ..."
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
    restart)
        restart
        ;;
    *)
        echo $"Usage: $0 {start|stop|restart|status|stats}"
        RETVAL=1
esac
exit $RETVAL
