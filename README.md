# easy-guest-tracker

# 一种~~基于无线传感网络的~~隐式“健康宝”到访登记系统（概念版）

基本原理为：顾客进入商场大门时扫“健康宝”并领取与之绑定的RFID手环，这样进入各店铺购物时即可通过读卡器实现自动扫码，同时测温，免去了每去一家店都要扫码的麻烦。

## Components

- [x] `arduino-code`
- [x] `guest-analyzer`
- [x] `guest-registry`
- [x] `rfid2onenet`
- [x] `onenet-script`

## arduino-code

Arduino程序，通过Arduino IDE将`temp2OneNET.ino`上传至主板，之后只要供电即可自动持续运行，无需连接电脑。

核心逻辑为：

 1. `setup`，开启串口，初始化gprs
 2. 开始`loop`
 3. 传感器读温度
 4. 向OneNET平台发起TCP连接
 5. 发送鉴权信息，即连接到小组的设备
 6. 发送温度数据
 7. 断开连接
 8. 回到2

## guest-analyzer

Java图形化程序，分析OneNET平台上的访客数据，通过时间把访客的RFID和体温对应起来。

## guest-registry

Java图形化串口程序，输入访客姓名，自动生成一个USER值写入RFID tag，把USER-Name对写入数据库（`registration/guest_registry.csv`），以供分析程序读取实现对应。

## rfid2onenet

Java后台串口程序，自动盘点RFID Tag，并将读到的RFID信息上传至OneNET平台。

逻辑很简单，就是一直盘点，每盘点够100Tag次就结束盘点然后把读到的Tag的USER上传，然后再重新盘点。按任意键断开连接并停止。

## onenet-script

没什么用，就是声明了一个OneNET数据流的名字，改了模板最后几行。

## Instruction

除了analyzer是java15（也可以换成8），剩下的都是java8。无设备运行时把调用rfid的部分都注释掉。

基本流程为：
1. 先把Arduino代码拷进去，然后扔一边自己跑去。
2. 电脑连RFID reader，用registry给tag注册，绑定访客名字和USER值，这个就用完了。
3. 然后一直运行rfid2onenet，把tag靠近reader，这就是一次访客到访。
4. 再去analyzer上就能看到刚才的到访信息以及到访者的体温了。
