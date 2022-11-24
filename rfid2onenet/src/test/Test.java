package test;

import com.uhf.linkage.Linkage;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        int[] i = {0};
        new Thread(() -> {
            if (new Scanner(System.in).hasNext()) {
                i[0] = 1;
            }
        }).start();
        while (i[0] == 0) {
            System.out.println("connect success");
            Thread.sleep(1000);
        }
        System.out.println("deinit");
    }
}
