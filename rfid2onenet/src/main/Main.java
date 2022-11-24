package main;

import com.uhf.detailwith.InventoryDetailWith;
import com.uhf.linkage.Linkage;
import com.uhf.structures.InventoryArea;

import tcp.TCP;

import java.util.Map;
import java.util.Scanner;

public class Main {
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        int[] i = {0};
        i[0] = Linkage.getInstance().initial("COM3");
        new Thread(() -> {
            if (new Scanner(System.in).hasNext()) {
                i[0] = 1;
            }
        }).start();
        while (i[0] == 0) {
            System.out.println("connect success");
            setInventoryArea();
            getInventoryArea();
            startInventory();
        }
        Linkage.getInstance().deinit();
    }
    public static void startInventory() {
        InventoryArea inventory = new InventoryArea();
        inventory.setValue(2, 0, 6);
        Linkage.getInstance().setInventoryArea(inventory);
        InventoryDetailWith.tagCount = 0;
        Linkage.getInstance().startInventory(2, 0);
        InventoryDetailWith.startTime = System.currentTimeMillis();
        while (InventoryDetailWith.totalCount < 100) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        stopInventory();
        for (Map<String, Object> _map : InventoryDetailWith.list) {
            System.out.println(_map);
            System.out.println("USER码：" + _map.get("externalData"));
            new TCP().send(_map.get("externalData").toString().substring(0,8));
        }
    }
    public static void stopInventory() {
        Linkage.getInstance().stopInventory();
        System.out.println();
    }
    public static void getInventoryArea() {
        InventoryArea inventoryArea = new InventoryArea();
        int status = Linkage.getInstance().getInventoryArea(inventoryArea);
        if (status == 0) {
            return;
        }
        System.out.println("getInventoryArea failed");
    }
    public static void setInventoryArea() {
        InventoryArea inventoryArea = new InventoryArea();
        inventoryArea.setValue(2, 0, 6);// 2为epc+user
        int status = Linkage.getInstance().setInventoryArea(inventoryArea);
        if (status == 0) {
            return;
        }
        System.out.println("setInventoryArea failed");
    }
}
