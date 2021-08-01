package lesson4ex1;

public class WaitNotifyApp {
    private static final Object mon = new Object();
    private static String currentLetter = "A";

    public static void main(String[] args) {
        Thread threadA = new Thread(() -> {
            try {
                synchronized (mon) {
                    for (int i = 0; i < 5; i++) {
                        while (currentLetter != "A") {
                            mon.wait();
                        }
                        System.out.println(currentLetter);
                        currentLetter = "B";
                        mon.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread threadB = new Thread(() -> {
            try {
                synchronized (mon) {
                    for (int i = 0; i < 5; i++) {
                        while (currentLetter != "B") {
                            mon.wait();
                        }
                        System.out.println(currentLetter);
                        currentLetter = "C";
                        mon.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread threadC = new Thread(() -> {
            try {
                synchronized (mon) {
                    for (int i = 0; i < 5; i++) {
                        while (currentLetter != "C") {
                            mon.wait();
                        }
                        System.out.println(currentLetter);
                        currentLetter = "A";
                        mon.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadA.start();
        threadB.start();
        threadC.start();
    }
}