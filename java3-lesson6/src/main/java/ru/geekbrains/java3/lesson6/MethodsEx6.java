package ru.geekbrains.java3.lesson6;

import java.util.Arrays;

public class MethodsEx6 {
    public static void main(String[] args) {
        MethodsEx6 methods = new MethodsEx6();
        int[] incoming = {1, 4, 1, 1, 1, 1, 4, 1, 1};
        int[] result = methods.returnMethod(incoming);
        System.out.println(Arrays.toString(result));
        System.out.println(methods.checkMethod(incoming));
    }

    public int[] returnMethod (int[] incoming){
        for (int i = incoming.length - 1; i > -1; i--){
            if (incoming[i] == 4){
                int n = 0;
                int[] returning = new int[incoming.length - i - 1];
                for (int j = i+1; j < incoming.length; j++){
                        returning[n] = incoming[j];
                        n++;
                    }
                return returning;
            }
        }throw new RuntimeException();
    }

    public boolean checkMethod (int[] incoming){
        int counter1 = 0;
        int counter4 = 0;
        for (int i = 0; i < incoming.length; i++){
            if (incoming[i] == 1) {
                counter1++;
            }else if (incoming[i] == 4) {
                counter4++;
            }
        }
        if (counter1 == 0 || counter4 == 0 || counter1 + counter4 < incoming.length){
            return false;
        }else return true;
    }
}
