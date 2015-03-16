vertx install com.hongfu001.wns~wns.chats~3.2.1.0-SNAPSHOT
wns_env_filter=/home/play/.m2/wns.conf
export wns_env_name=prod
vertx runmod com.hongfu001.wns~wns.chats~3.2.1.0-SNAPSHOT -instances 1
