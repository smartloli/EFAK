#! /bin/bash

############################
# Config MySQL Information #
############################
SYSTEM_CONFIG=$KE_HOME/conf/system-config.properties
TMP_URL=`grep kafka.eagle.url ${SYSTEM_CONFIG}|cut -d'=' -f2`
IP_PORT_SPLIT_LEFT=${TMP_URL#*//}
IP_PORT=${IP_PORT_SPLIT_LEFT%/*}
TMP_DB=${IP_PORT_SPLIT_LEFT#*/}
DBNAME=${TMP_DB%\?*}
HOSTNAME=${IP_PORT%:*}
PORT=${IP_PORT#*:}
USERNAME=`grep kafka.eagle.username ${SYSTEM_CONFIG}|cut -d'=' -f2`
PASSWORD=`grep kafka.eagle.password ${SYSTEM_CONFIG}|cut -d'=' -f2`

############################
# Create Database[ke]      #
############################

show_db_sql="SHOW DATABASES;"
db=$(/usr/local/mysql/bin/mysql -h${HOSTNAME} -P${PORT} -u${USERNAME} -p${PASSWORD} -e "${show_db_sql}")
result=$(echo "$db" | grep "${DBNAME}")
if [[ "$result" != "" ]];then
  echo "INFO: [${DBNAME}] database has successed. Duplicate create is not required."
else
  create_db_sql="CREATE DATABASE IF NOT EXISTS ${DBNAME};"
  /usr/local/mysql/bin/mysql -h${HOSTNAME} -P${PORT} -u${USERNAME} -p${PASSWORD} -e "${create_db_sql}"
  echo "INFO: Initialize load create [${DBNAME}] database successed."
fi

############################
# Create table[...] schema #
############################
create_table_ke_p_role="CREATE TABLE IF NOT EXISTS \`ke_p_role\` (\`id\` tinyint(4) NOT NULL AUTO_INCREMENT,\`name\` varchar(64) CHARACTER SET utf8 NOT NULL COMMENT 'role name',\`seq\` tinyint(4) NOT NULL COMMENT 'rank',\`description\` varchar(128) CHARACTER SET utf8 NOT NULL COMMENT 'role describe',PRIMARY KEY (\`id\`)) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;BEGIN;INSERT INTO \`ke_p_role\` VALUES ('1', 'Administrator', '1', 'Have all permissions'), ('2', 'Devs', '2', 'Own add or delete'), ('3', 'Tourist', '3', 'Only viewer');COMMIT;"
create_table_ke_resources="CREATE TABLE IF NOT EXISTS \`ke_resources\` (\`resource_id\` int(11) NOT NULL AUTO_INCREMENT,\`name\` varchar(255) CHARACTER SET utf8 NOT NULL COMMENT 'resource name',\`url\` varchar(255) NOT NULL,\`parent_id\` int(11) NOT NULL,PRIMARY KEY (\`resource_id\`)) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4;BEGIN;
INSERT INTO \`ke_resources\` VALUES ('1', 'System', '/system', '-1'), ('2', 'User', '/system/user', '1'), ('3', 'Role', '/system/role', '1'), ('4', 'Resource', '/system/resource', '1'), ('5', 'Notice', '/system/notice', '1'), ('6', 'Topic', '/topic', '-1'), ('7', 'Message', '/topic/message', '6'), ('8', 'Create', '/topic/create', '6'), ('9', 'Alarm', '/alarm', '-1'), ('10', 'Add', '/alarm/add', '9'), ('11', 'Modify', '/alarm/modify', '9'), ('12', 'Cluster', '/cluster', '-1'), ('13', 'ZkCli', '/cluster/zkcli', '12'), ('14', 'UserDelete', '/system/user/delete', '1'), ('15', 'UserModify', '/system/user/modify', '1'), ('16', 'Mock', '/topic/mock', '6');
COMMIT;"
create_table_ke_role_resource="CREATE TABLE IF NOT EXISTS \`ke_role_resource\` (\`id\` int(11) NOT NULL AUTO_INCREMENT,\`role_id\` int(11) NOT NULL,\`resource_id\` int(11) NOT NULL,PRIMARY KEY (\`id\`)) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4;BEGIN;
INSERT INTO \`ke_role_resource\` VALUES ('1', '1', '1'), ('2', '1', '2'), ('3', '1', '3'), ('4', '1', '4'), ('5', '1', '5'), ('6', '1', '7'), ('7', '1', '8'), ('8', '1', '10'), ('9', '1', '11'), ('10', '1', '13'), ('11', '2', '7'), ('12', '2', '8'), ('13', '2', '13'), ('14', '2', '10'), ('15', '2', '11'), ('16', '1', '14'), ('17', '1', '15'), ('18', '1', '16');
COMMIT;"
create_table_ke_trend="CREATE TABLE IF NOT EXISTS \`ke_trend\` (\`cluster\` varchar(64) NOT NULL,\`key\` varchar(64) NOT NULL,\`value\` varchar(64) NOT NULL,\`hour\` varchar(2) NOT NULL,\`tm\` varchar(16) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
create_table_ke_user_role="CREATE TABLE IF NOT EXISTS \`ke_user_role\` (\`id\` int(11) NOT NULL AUTO_INCREMENT,\`user_id\` int(11) NOT NULL,\`role_id\` tinyint(4) NOT NULL,PRIMARY KEY (\`id\`)) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;BEGIN;
INSERT INTO \`ke_user_role\` VALUES ('1', '1', '1');
COMMIT;"
create_table_ke_users="CREATE TABLE IF NOT EXISTS \`ke_users\` (\`id\` int(11) NOT NULL AUTO_INCREMENT,\`rtxno\` int(11) NOT NULL,\`username\` varchar(64) NOT NULL,\`password\` varchar(128) NOT NULL,\`email\` varchar(64) NOT NULL,\`realname\` varchar(128) NOT NULL,PRIMARY KEY (\`id\`)) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;BEGIN;
INSERT INTO \`ke_users\` VALUES ('1', '1000', 'admin', '123456', 'admin@email.com', 'Administrator');
COMMIT;"

show_tables_sql="SHOW TABLES;"
t=$(/usr/local/mysql/bin/mysql -h${HOSTNAME} -P${PORT} -u${USERNAME} -p${PASSWORD} -D ${DBNAME} -e "${show_tables_sql}")
tables=(ke_p_role ke_resources ke_role_resource ke_trend ke_user_role ke_users)
echo "INFO: Table collect number is [${#tables[@]}]"

for var in ${tables[*]}
do
  result=$(echo "$t" | grep "${var}")
  if [[ "$result" != "" ]];then
    echo "INFO: [${var}] table has exist. Duplicate create is not required."
  else
    tablename=create_table_${var}
    /usr/local/mysql/bin/mysql -h${HOSTNAME} -P${PORT} -u${USERNAME} -p${PASSWORD} -D ${DBNAME} -e "${!tablename}"
    echo "INFO: Initialize load create [${var}] tables successed."
  fi
done
