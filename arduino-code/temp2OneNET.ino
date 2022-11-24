#include <gprs.h>
#include <SoftwareSerial.h>
#include <dht11.h>

GPRS gprs;
dht11 DHT11;

#define DHT11PIN A2

bool gprsInit() //入网初始化
{
  gprs.preInit();
  int i = 0;
  while(0 != gprs.init()) {
    delay(500);
    Serial.println("init error");
    i++;
    if(i>=10){
      return false;
    }
  }  
  while(0 != gprs.connectTCP("183.230.40.40",1811)){
    Serial.println("connect error");
    i++;
    delay(2000);
    if(i >= 3)
      break;
  }
  gprs.closeTCP();
  Serial.println("gprs connect ok");
  i = 0;
  //入网过程
  while(!gprs.join()) {  
    //change "cmnet" to your own APN
    Serial.println("gprs join network error");
    delay(2000);
    i++;
    if(i>=5){
      return false;
    }    
  }
  // successful DHCP
  Serial.print("IP Address is ");
  Serial.println(gprs.getIPAddress());
  Serial.println("Init success, start to connect mbed.org...");
  return true;
}


void setup() 
{
  Serial.begin(9600);
  while(!Serial);
  Serial.println("Setup Complete!");
  Serial.println("DHT11 TEST PROGRAM ");
  Serial.print("LIBRARY VERSION: ");
  Serial.println(DHT11LIB_VERSION);
  Serial.println();
  Serial.println("startInit");
  delay(5000);  
  gprsInit();//A9入网初始化
  String identify = gprs.getIdentify();
  Serial.print("identify:");
  Serial.println(identify);
}

void loop() 
{
  Serial.println("startloop");
  //读取传感器数据
  int chk = DHT11.read(DHT11PIN);
  Serial.print("Read sensor: ");
  switch (chk)
  {
    case DHTLIB_OK: 
                Serial.println("OK"); 
                break;
    case DHTLIB_ERROR_CHECKSUM: 
                Serial.println("Checksum error"); 
                goto STOP;
                break;
    case DHTLIB_ERROR_TIMEOUT: 
                Serial.println("Time out error"); 
                goto STOP;
                break;
    default: 
                Serial.println("Unknown error"); 
                goto STOP;
                break;
  }

  //TCP连接 使用gprs.connectTCP函数  IP地址：183.230.40.40  端口号：1811
  /*
   * int GPRS::connectTCP(const char *ip, int port)  TCP连接函数
   * ip:ip地址
   * port:端口号
   * 返回值：-1（连接失败）0（连接成功）
   * 源码位置：试验箱(新)\arduino-1.6.13\libraries\Seeeduino_GPRS-masterA9\gprs.cpp 507行
   */
  int i=0;
  while(0 != gprs.connectTCP("183.230.40.40",1811)){
    Serial.println("connect error");
    i++;
    delay(2000);
    if(i >= 3)
      break;
  }

  //登录OneNET平台 使用gprs.sendTCPData函数  登陆报文：*产品ID#设备号#数据流名称*
  /*
   * int GPRS::sendTCPData(char *data)  TCP发送数据
   * data:需要发送的数据
   * 返回值：-1（发送失败）0（发送成功）
   * 源码位置：试验箱(新)\arduino-1.6.13\libraries\Seeeduino_GPRS-masterA9\gprs.cpp 522行
   */
  if(0 != gprs.sendTCPData("*153149#06#temp1*")){
    Serial.println("send TCP data error");
    goto STOP;
  }

  //发送传感器数据  使用gprs.sendTCPData函数发送数据 在发送数据之前需要使用itoa函数，把传感器数据转换成字符串 
  /*
   * itoa是广泛应用的非标准C语言和C++语言扩展函数
   * char *itoa(int value,char *string,int radix)  TCP发送数据
   * int value：被转换的整数
   * char *string：转换后储存的字符数组
   * int radix：转换进制数，如2,8,10,16 进制等
   */
  char temp[8];
  itoa( (int) round(DHT11.temperature), temp, 10);
  if(0 != gprs.sendTCPData(temp)){
    Serial.println("send TCP data error");
    goto STOP;
  }

  //断开TCP连接  使用gprs.closeTCP()函数断开TCP连接
  /*
   * int GPRS::closeTCP(void)  断开TCP连接
   * 返回值：0
   * 源码位置：试验箱(新)\arduino-1.6.13\libraries\Seeeduino_GPRS-masterA9\gprs.cpp 542行
   */
STOP:
  gprs.closeTCP();


  //重复以上步骤，将不同的传感器数据上传到OneNET平台对应的数据流


  
  delay(2000);
}
