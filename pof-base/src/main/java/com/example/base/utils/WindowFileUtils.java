package com.example.base.utils;

import com.example.base.Exception.WindowFileException;
import com.example.base.entities.Payload;
import org.apache.commons.lang3.tuple.Triple;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
* 处理窗口文件testcase和testfile, 生成元组(input, path, iscrash)
* */
public class WindowFileUtils {

    static List<Payload> payloads = new ArrayList<>();
    static List<String> cases_list = new ArrayList<>();
    static List<List<Integer>> paths_list = new ArrayList<>();
//    上一个窗口最后一个case和它的path
    static String lastWindowcase = "";
    static List<Integer> lastWindowpath = new ArrayList<>();

    public static List<Payload> windowFilesToTriple(String testcase, String paths) throws WindowFileException{

        StringBuilder stringBuilder = new StringBuilder();

        File file = new File(testcase); // 创建文件对象
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file); // 初始化FileInputStream
            byte[] fileBytes = new byte[(int) file.length()]; // 创建字节数组
            int bytesRead = fis.read(fileBytes); // 读取文件内容
//            System.out.println("读取的字节数: " + bytesRead); // 打印读取的字节数
//            for (int i = 0; i < file.length(); i++) {
//                System.out.print(i + ":" + fileBytes[i]+" ");
//            }
            String str = new String(fileBytes);
            String[] split = str.split("mem=");
//            System.out.println("case:" + (split.length-1));
            cases_list = new ArrayList<>(Arrays.asList(split).subList(1,split.length));
//            for (int i = 0; i < cases_list.size(); i++) {
//                System.out.println(i + ":" + cases_list.get(i));
//            }
        } catch (IOException e) {
            e.printStackTrace(); // 捕获并处理异常
        } finally {
            try {
                if (fis != null) {
                    fis.close(); // 确保输入流关闭
                }
            } catch (IOException e) {
                e.printStackTrace(); // 捕获关闭时可能发生的异常
            }
        }
        try {
            FileInputStream pathFile = new FileInputStream(paths);

            byte[] pathsBuffer = new byte[1024]; // 定义缓冲区
            int bytesRead;
            while ((bytesRead = pathFile.read(pathsBuffer)) != -1) {
                // 将读取的字节转换为字符串
                for (int i = 0; i < bytesRead; i++) {
                    stringBuilder.append(String.format("%02X", pathsBuffer[i])); // 转换为十六进制表示
                }
            }
            String pathstr = stringBuilder.toString();
            System.out.println("path length: " + pathstr.length());
            String[] pathsplits = pathstr.split("0000");
//            for (int i = 1; i < pathsplits.length; i++) {
//                System.out.println(i + ":" + pathsplits[i]);
//            }
            String firstpath = pathsplits[0];
//            System.out.println("firstpath=" + firstpath);
            //如果上个window的最后一条path未结束,将它加到pathList，总path数加1。将上一个窗口的最后一个case加到caselist第一个
            if (firstpath.length() != 0){
                //从record文件中读取上一轮的case和path
                readFromRecordFile();
                int index = 0;
                ArrayList<Integer> path = new ArrayList<>();
                while (index < firstpath.length()) {
                    String hex = firstpath.substring(index, index + 4);
                    String newHex = hex.substring(2,4) + hex.substring(0,2);
                    int path_num = Integer.parseInt(newHex, 16);
                    path.add(path_num << 1);
                    index += 4;
                }
                // 这时paths_list的第一条路径就是这条残余路径
                paths_list.add(path);
                cases_list.add(0,lastWindowcase);
//                System.out.println("path:" + pathsplits.length);
//                System.out.println("case:" + cases_list.size());
            } else {
//                System.out.println("path:" + (pathsplits.length - 1));
            }
            for (int i = 1; i < pathsplits.length; i++) {
                String Hexpath = pathsplits[i];
                int index = 0;
                ArrayList<Integer> path = new ArrayList<>();
                while (index < Hexpath.length()) {
                    String hex = Hexpath.substring(index, index + 4);
                    String newHex = hex.substring(2,4) + hex.substring(0,2);
//                    System.out.print(newHex + " ");
                    int path_num = Integer.parseInt(newHex, 16);
                    path.add(path_num << 1);
                    index += 4;
                }
                paths_list.add(path);
//                for (int j = 0; j < path.size(); j++) {
//                    System.out.print(path.get(j) + " ");
//                }
//                System.out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

//        if (cases_list.size() != paths_list.size()) {
//            throw new WindowFileException("case和path不匹配");
//        }
//     存入triple中
        // @TODO: 为什么两者会不相等？
        if(cases_list.isEmpty() || paths_list.isEmpty()) {
            throw new WindowFileException("cases_list或者paths_list为空");
        }
        for (int i = 0; i < Math.min(cases_list.size(), paths_list.size()); i++) {
            String s = cases_list.get(i);
            boolean is_crash;
            if (s.contains("crash")) {
                is_crash = true;
            } else {
                is_crash = false;
            }
            Payload payload = new Payload(cases_list.get(i), paths_list.get(i), is_crash);
            payloads.add(payload);
        }

        lastWindowcase = cases_list.get(cases_list.size()-1);
        lastWindowpath = paths_list.get(paths_list.size()-1);
        recordLastWindowcaseandpath();
        return payloads;
    }

    // 记录上个窗口的最后一个case和path
    public static void recordLastWindowcaseandpath(){
        String fileName = "/home/wj/pofChain/AFL/recordFile";
        try (FileOutputStream fos = new FileOutputStream(fileName);
             DataOutputStream dos = new DataOutputStream(fos)) {
            dos.writeInt(lastWindowcase.length());
            dos.writeChars(lastWindowcase);
            for (int p : lastWindowpath) {
                dos.writeInt(p);
            }
//            System.out.println("数据已写入文件: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //从record文件中读取
    public static void readFromRecordFile() {
        String fileName = "/home/wj/pofChain/AFL/recordFile"; // 文件名
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("文件创建成功: " + file);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            byte[] pathsBuffer = new byte[1024]; // 定义缓冲区
            int bytesRead;
            try {
                StringBuilder stringBuilder = new StringBuilder();
                FileInputStream recordFile = new FileInputStream(fileName);
                while ((bytesRead = recordFile.read(pathsBuffer)) != -1) {
                    // 将读取的字节转换为字符串
                    for (int i = 0; i < bytesRead; i++) {
                        stringBuilder.append(String.format("%02X", pathsBuffer[i])); // 转换为十六进制表示
                    }
                }
                String string = stringBuilder.toString();
                int len = Integer.parseInt(string.substring(0, 8), 16);
                String lastcase = string.substring(8, len * 4 + 8);
                lastWindowcase = new String(lastcase.getBytes(), 0, lastcase.length());
//                @TODO:二进制转换成字符
                String lastwindowcaseStr = hexToBinary(lastWindowcase);
//                System.out.println("lastcase:" + lastwindowcaseStr);

                String paths = string.substring(len * 4 + 8, string.length());
//                处理paths，拆分为整数，赋值给lastWindowpath
                for (int i = 0; i < paths.length(); i+=8) {
                    int path = Integer.parseInt(paths.substring(i, i + 8), 16);
//                    System.out.println(path);
                    lastWindowpath.add(path);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static String hexToBinary(String hex) {
        StringBuilder binaryStringBuilder = new StringBuilder();

        // 遍历每个十六进制字符
        for (char hexChar : hex.toCharArray()) {
            // 将十六进制字符转换为整数
            int decimalValue = Character.digit(hexChar, 16);
            // 将整数转换为四位二进制字符串
            String binary = String.format("%4s", Integer.toBinaryString(decimalValue)).replace(' ', '0');
            binaryStringBuilder.append(binary); // 拼接到结果中
        }

        return binaryStringBuilder.toString(); // 返回最终的二进制字符串
    }

    public static void main(String[] args) throws WindowFileException {
        int num = 6;
//        while (num <= 20){
            System.out.println("num=" + num);
            List<Payload> triples =
                    WindowFileUtils.windowFilesToTriple("/home/wj/pofChain/AFL/afl_testfiles/window_testcases/testcase_" + num,
                            "/home/wj/pofChain/AFL/afl_testfiles/window_paths/testfile_" + num);
            num ++;
            for (int i = 0; i < triples.size(); i++) {
                Payload triple = triples.get(i);
                String left = triple.getInput();
                List<Integer> middle = triple.getPath();
                Boolean right = triple.isCrash();
                System.out.print("[" + left + ":");
                for (int j = 0; j < middle.size(); j++) {
                    System.out.print(middle.get(j) + "->");
                }
                System.out.print(right + "]");
                System.out.println();
            }
//        }


    }

}
