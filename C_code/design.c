/*
*   1.串口接收数据 
        ps:如果串口没有更改权限，在运行的时候要加sudo
*   2.保存到数据库 
        ps:MySQL数据库编译加 -lmysqlclient
            gcc -o total total.c -lmysqlclient
            头文件要加 #include  <mysql/mysql.h>
*   3.TCP发送数据到Android客户端  
        ps:运行时要加上TCP服务端的IP地址
*   4.mqtt协议控制执行器  
        ps:编译：gcc -o exp4 exp4.c -I ~/3rdlib/include/ -L ~/3rdlib/lib/ -lpaho-mqtt3a
*   5.读串口、TCP在主线程  mqtt在子线程
        ps:多线程编译：添加-lpthread

        gcc -o total1 total1.c -lpthread -lmysqlclient -I ~/3rdlib/include/ -L ~/3rdlib/lib/ -lpaho-mqtt3a

*/
#include<stdio.h>
#include<unistd.h>
#include<fcntl.h>
#include<termios.h>
#include<string.h>
#include<time.h>
#include<mysql/mysql.h>
#include<sys/types.h>
#include<semaphore.h>
#include<pthread.h>
#include <strings.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include<MQTTAsync.h>
#include<stdlib.h>
//定义时间格式
char timebuf[20];//YYYY-MM-DD hh:mm:ss

//MySQL的定义内容
MYSQL com_mysql;//声明对MySQL的连接句柄
char sqlcommand[100];//定义一个可以赋值sql语句的变量
MYSQL_RES *pRes;//声明一个数据集，从数据库中读取数据，最后从MYSQL_RES中读取数据
MYSQL_ROW hs;

//定义温湿度数据
char temp[3];//温度
char humid[3];//湿度

//串口的定义内容
int fd;//设备描述符
int count=0;//串口读出来的字节数
char buffer[100];//指定存储器读出数据的缓冲区
struct termios uart_cfg;////定义termios结构

//TCP定义的内容
int client_fd;//定于一个TCP套接字
int ret;//定义一个值，来判断连接TCP服务器是否成功以及发送数据到TCP服务器是否成功
int count1;
struct sockaddr_in server_addr;
char buf[512];
char recv_buf[512];
int sock_size=sizeof(struct sockaddr_in);

//MQTT定义的内容
#define ADDRESS ""  //阿里云MQTT服务器地址
#define CLIENTID "zzx123"  
#define TOPIC "test"    //订阅的主题
#define TOPIC1 "zzxhello"
#define PAYLOAD     "Hello World!"  //发送的消息
#define QOS         1   //服务质量
#define TIMEOUT     10000L  //允许尝试连接的过时时间
int isconnected=0;
int disc_finished = 0;
int subscribed = 0;
int finished = 0;
void *thread_mqtt(void *arg);//声明子线程函数
void onConnect(void* context, MQTTAsync_successData* response);//声明连接成功的函数
void onConnectFailure(void* context, MQTTAsync_failureData* response);//声明连接不成功的函数
void onSubscribe(void* context, MQTTAsync_successData* response);//声明订阅成功的函数
void onSubscribeFailure(void* context, MQTTAsync_failureData* response);//声明订阅不成功的函数
int msgarrvd(void *context, char *topicName, int topicLen, MQTTAsync_message *message);//声明接收信息的函数
void connlost(void *context, char *cause);//声明连接丢失
void onSend(void* context,MQTTAsync_successData* response);
MQTTAsync client;
MQTTAsync_connectOptions conn_opts = MQTTAsync_connectOptions_initializer;
int ret;
//获取时间函数
void get_time();


int main(int argc,char **argv)
{
    ret=MQTTAsync_create(&client, ADDRESS, CLIENTID, MQTTCLIENT_PERSISTENCE_NONE, NULL);
    if(ret!=MQTTASYNC_SUCCESS)
    {
        printf("Failed to start create, return code %d\n",ret);
        return -1;
    }
	ret=MQTTAsync_setCallbacks(client, client, connlost, msgarrvd, NULL);
    if(ret!=MQTTASYNC_SUCCESS)
    {
        printf("Failed to set setcallbacks, return code %d\n",ret);
        return -1;
    }
	conn_opts.username="zzx";
	conn_opts.password="981216";
	conn_opts.keepAliveInterval = 20;
	conn_opts.cleansession = 1;
	conn_opts.onSuccess = onConnect;
	conn_opts.onFailure = onConnectFailure;
	conn_opts.context = client;
    ret=MQTTAsync_connect(client,&conn_opts);
    if(ret!=MQTTASYNC_SUCCESS)
    {
        printf("Failed to start connect, return code %d\n",ret);
        return -1;
    }


    //定义主线程跟子线程
    pthread_t tid;
    //创建子线程
    if(pthread_create(&tid,NULL,thread_mqtt,NULL)<0)
    {
        printf("Create thread_mqtt failed!\n");//创建子线程失败
        exit(0);
    }

    pthread_join(tid,NULL);
    
    
    //1.开串口
    fd=open("/dev/ttyS1",O_RDWR|O_NONBLOCK|O_NOCTTY); 
    if(fd<0)//fd<0：打开串口失败；fd>=0：打开串口成功
    {
        perror("Failed to open serial:");
        return -1;
    }
    printf("打开串口成功\n");
    fcntl(fd,F_SETFL,0);//如果在open时使用了O_NONBLOCK或O_NDELAY标志，通常需要该语句将串口重新设定为阻塞方法
    cfmakeraw(&uart_cfg);//调用cfmakeraw()函数可以将终端设置为原始模式，采用原始模式进行串口数据通信。
    cfsetspeed(&uart_cfg,B9600);//同时完成设置输入、输出的波特率为9600
    uart_cfg.c_cflag|=CLOCAL|CREAD;//一般必设置的标志;None检验位，8数据位，1位停止位
    tcflush(fd,TCIOFLUSH);//清空收发缓冲区，使得新的设置生效
    tcsetattr(fd,TCSANOW,&uart_cfg);//激活配置
    //2.连数据库
    if(mysql_init(&com_mysql)==NULL)
    {
        printf("connot init mysql!\n");
        return 0;
    }
    if(mysql_real_connect(&com_mysql,"","","","db_temp",0,NULL,0)==NULL)
    {
        printf("%s\n",mysql_error(&com_mysql));//连接不上数据库服务器
        return 0;
    }
    printf("连接数据库成功!\n");
    
    //3.连接TCP发送数据
    if(argc<2)
    {
        printf("usage:./client serverip\n");//运行时要输入TCP服务器的IP地址
        return 0;
    }
    bzero(&server_addr,sock_size);//给套接字清零
	client_fd=socket(AF_INET,SOCK_STREAM,0);//生成一个TCP的套接字
	server_addr.sin_family=AF_INET;//创建套接字时，用该字段指定地址家族，对于TCP/IP协议的，必须设置为AF_INET
	server_addr.sin_port=htons(8000);//将监听套接字的端口设置为8000
	server_addr.sin_addr.s_addr=inet_addr("192.168.2.109");// 填写IP
	ret=connect(client_fd,(struct sockaddr*)&server_addr,sock_size);//连接TCP服务器
    if(ret<0){
		perror("Failed to connect TCPserver:");//连接失败
		return -1;
	}
    printf("连接TCP成功!\n");
    
    while(1)//打开串口、连接数据库服务器、连接TCP服务器都成功
    {
        count=read(fd,buffer,100);//读串口数据
        if(count>0)
        {
            //1.读串口数据 
            buffer[count]=0;//分割从串口读来的数据
            printf("%s\r\n",buffer);
            strncpy(temp,buffer,2);
            temp[2]='\0';
            humid[0]=buffer[2];
            humid[1]=buffer[3];
            humid[2]='\0';
            printf("temp is %s\r\n",temp);//温度
            printf("humid is %s\r\n",humid);//湿度
            get_time();//获取当前时间
            //2.保存数据到MySQL
            sprintf(sqlcommand,"insert into temp(time,temp,humid) values('%s','%s','%s')",timebuf,temp,humid);//将sql语句赋给sqlcommand
 	    	//printf("sqlcommand:%s\n",sqlcommand);
            if(mysql_query(&com_mysql,sqlcommand)!=0){//执行sql命令，保存到数据库中
       	 		printf("%s\n",mysql_error(&com_mysql));//插入数据失败
    		}else
    		{
        		printf("插入数据成功！\n");
    		}
            //3.连接TCP，发送数据到Android
            ret=write(client_fd,buffer,strlen(buffer));
            if(ret<=0)
            {
                break;
            }
        }
    }
    mysql_close(&com_mysql);//关闭数据库服务器
    close(fd);//关闭串口
    close(client_fd);//关闭TCP连接
    return 0;
}


//获取时间函数
void get_time()
{
    time_t aclock;
    time(&aclock);
    strftime(timebuf,20,"%Y-%m-%d %H:%M:%S",localtime(&aclock));
}

//MQTT子线程
void *thread_mqtt(void *arg)
{
    
}

//MQTT连接成功的函数
void onConnect(void* context, MQTTAsync_successData* response)
{
    
    MQTTAsync client=(MQTTAsync)context;
    MQTTAsync_responseOptions opts = MQTTAsync_responseOptions_initializer;
    int ret;
    printf("Subscribing to topic %s\nfor client %s using QoS%d\n", TOPIC, CLIENTID, QOS);
    opts.onSuccess=onSubscribe;
    opts.onFailure = onSubscribeFailure;
    opts.context = client;
    ret=MQTTAsync_subscribe(client, TOPIC, QOS, &opts);
    if(ret!=MQTTASYNC_SUCCESS)
    {
        printf("Failed to start subscribe, return code %d\n", ret);
        exit(EXIT_FAILURE);
    }
    isconnected=1;   
}

//MQTT连接失败的函数
void onConnectFailure(void* context, MQTTAsync_failureData* response)
{
    printf("Connect failed, rc %d\n", response ? response->code : 0);
}
//订阅成功
void onSubscribe(void* context, MQTTAsync_successData* response)
{
	printf("Subscribe succeeded\n");
}
//订阅失败
void onSubscribeFailure(void* context, MQTTAsync_failureData* response)
{
	printf("Subscribe failed, rc %d\n", response ? response->code : 0);
}
//接收信息
int msgarrvd(void *context, char *topicName, int topicLen, MQTTAsync_message *message)
{
    int i;
    const char* payloadptr;
    char onbuf[]="1";
    char offbuf[]="0";
    char* buf="1";
    char* buf2="0";
    printf("Message arrived\n");
    printf("     topic: %s\n", topicName);
    printf("   message: %s",(char*)message->payload);
    if (strcmp((char*)message->payload,(char*)buf)==0)//比对接收到的信息来选择写入串口的数据以此来控制灯的亮灭 灯亮：1  灯灭：0
    {
        printf("light on!\n");
	    write(fd,onbuf,strlen(onbuf));
    }else if (strcmp((char*)message->payload,(char*)buf2)==0)
    {
        printf("light off!\n");
	    write(fd,offbuf,strlen(offbuf));
    }
    putchar('\n');
    MQTTAsync_freeMessage(&message);//释放消息
    MQTTAsync_free(topicName);//释放主题
    return 1;
}
//连接丢失函数
void connlost(void *context, char *cause)
{
	MQTTAsync client = (MQTTAsync)context;
	MQTTAsync_connectOptions conn_opts = MQTTAsync_connectOptions_initializer;
	int rc;
	printf("\nConnection lost\n");
	if (cause)
		printf("     cause: %s\n", cause);

	printf("Reconnecting\n");
	conn_opts.keepAliveInterval = 20;
	conn_opts.cleansession = 1;
	if ((rc = MQTTAsync_connect(client, &conn_opts)) != MQTTASYNC_SUCCESS)
	{
		printf("Failed to start connect, return code %d\n", rc);
		finished = 1;
	}
}
void onSend(void* context,MQTTAsync_successData* response)
{
    printf("sended a message to mqtt server!\n");
}
