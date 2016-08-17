#! /bin/bash

# source function library
#. /etc/rc.d/init.d/functions

DIALUP_PID=$KE_HOME/bin/ke.pid
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
	CLASS=com.smartloli.kafka.eagle.plugins.main.TomcatServerListen
	nohup ${JAVA_HOME}/bin/java -classpath "$CLASSPATH" $CLASS > ${LOG_DIR}/ke.out 2>&1
	echo "*******************************************************************"
    echo "* Listen port has successed! *"
	echo "*******************************************************************"
	sleep 5
	chmod +x ${KE_HOME}/kms/bin/*.sh
	${KE_HOME}/kms/bin/startup.sh > ${LOG_DIR}/ke.out 2>&1 < /dev/null & new_agent_pid=$!
	echo "$new_agent_pid" > $DIALUP_PID
	echo "*******************************************************************"
    echo "* KE service has started success!                              *"
    echo "* Welcome, Now you can visit 'http://<your_host_or_ip>:port/ke'  *"
	echo "*******************************************************************"
	ln -s ${KE_HOME}/kms/logs/catalina.out ${LOG_DIR}/ke_console.out
}

stop()
{

	 if [ -f $KE_HOME/bin/ke.pid ];then
                    SPID=`cat $KE_HOME/bin/ke.pid`
					  if [ "$SPID" != "" ];then
                         ${KE_HOME}/kms/bin/shutdown.sh
						 echo  > $DIALUP_PID
						 echo "stop success"
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
    restart)
        restart
        ;;
    *)
        echo $"Usage: $0 {start|stop|restart}"
        RETVAL=1
esac
exit $RETVAL
