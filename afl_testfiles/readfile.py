#!/usr/bin/python3
import struct
import os
def readtxt():
    with open("/home/wj/afl_testfiles/testcase_file1.txt", mode="r", encoding="ISO-8859-1") as f:
        file_content = f.read()
        split_content = file_content.split("mem=")
        cnt = len(split_content)
    return split_content[1:]

def readHextxt():
    binfile = open("/home/wj/afl_testfiles/testcase_file", 'rb')
    size = os.path.getsize("/home/wj/afl_testfiles/testcase_file")   #字节数
    content = binfile.read()
    split_content = content.split(b'mem=')
    # print(split_content)
    # res = ''
    # for i in range(size):
    #     data = binfile.read(2)   #每次输出两个字节
    #     print(data)
    # print(res)

    binfile.close()
    return split_content[1:]

def cases(num):
    binfile = open("/home/wj/afl_testfiles/files/testcase_" + num, 'rb')
    size = os.path.getsize("/home/wj/afl_testfiles/files/testcase_" + num)   #字节数
    content = binfile.read()
    split_content = content.split(b'mem=')
    binfile.close()
    content = split_content[1:]
    case_file = open("/home/wj/afl_testfiles/res_files/case_file" + num + ".txt", mode="w")
    for c in content:
        case_file.write(str(c))
        case_file.write("\n")
    l = len(content)
    print("case=" + str(l))

def readHEX(num):
    path = []
    binfile = open("/home/wj/afl_testfiles/testfile1_" + num, 'rb')
    size = os.path.getsize("/home/wj/afl_testfiles/testfile1_" + num)   #字节数
    for i in range(size//2):
        data = binfile.read(2)   #每次输出两个字节
        num = struct.unpack('H',data)  #H -- C中的unsigned short（0～65535）2 Byte
        if num[0] != 0:
            ls_num = num[0] << 1
            path.append(ls_num)
        else:
            path.append(num[0])
    binfile.close()

    return path

def readfirstHEX():
    print("start\n")
    # with open("/home/wj/afl_testfiles/files/testfile1", 'rb') as file:
    #     data = file.read()
    #     for byte in data:
    #         print("0x{:02x}".format(byte))
    path = []
    cnt = 0;
    binfile = open("/home/wj/afl_testfiles/testfile1", 'rb')
    size = os.path.getsize("/home/wj/afl_testfiles/testfile1")   #字节数
    prev = 0
    path_cnt = 0
    stoptime = 0
    for i in range(size//2):
        data = binfile.read(2)   #每次输出两个字节
        num = struct.unpack('H',data)  #H -- C中的unsigned short（0～65535）2 Byte
        if data == b'bj' or data == b'zj':
        # if data == b'bj':
            cnt += 1
            print(path_cnt - prev, end=',')
            prev = path_cnt
            path.append(data)
            stoptime += 1
            
    

        elif num[0] != 0:
            
            ls_num = num[0] << 1
            path.append(ls_num)
        else:
            path_cnt += 1
            path.append(num[0])
    binfile.close()
    print('cnt=',cnt)
    print(path)
    print('stop_time=',stoptime)
    return path

def path(num):
    path_file = open("/home/wj/afl_testfiles/res_files/path_file" + num + ".txt", mode="w")
    path_list = readHEX(num)
    all_path = []
    path = []
    path_cnt = 1
    # print(path_list[1:])
    for p in path_list[1:]:
        if p != 0:
            path.append(p)
        else:
            path_file.write("路径：")
            l = len(path)
            for i in range(l):
                if i % 2 == 0 and i != l-1:
                    path_file.write(str(path[i]))
                    path_file.write('->')
                elif i == l-1:
                    path_file.write(str(path[i]))
            all_path.append(path)
            path_file.write("\n")
            path.clear()
            path_cnt += 1
    # print(all_path)
    # size = max(len(testcase_list),path_cnt)
  
    path_file.write("路径：")
    l = len(path)
    for i in range(l):
        if i % 2 == 0 and i != l-1:
            path_file.write(str(path[i]))
            path_file.write('->')
        elif i == l-1:
            path_file.write(str(path[i]))
    path_file.write("\n")        
    
    print("path="+str(path_cnt)+"\n")
    path_file.close()


def readtxtandHEX(num):
    res_file = open("/home/wj/afl_testfiles/res_files/res_file" + num + ".txt", mode="w")
    testcase_list = readHextxt()
    path_list = readfirstHEX()
    # 将path_list中的路径分离开
    all_path = []
    path = []
    path_cnt = 1
    # print(path_list[1:])
    for p in path_list[1:]:
        if p != 0:
            path.append(p)
        else:
            res_file.write("用例：")
            res_file.write(str(testcase_list[path_cnt-1]))
            res_file.write("; 路径：")
            l = len(path)
            for i in range(l):
                if i % 2 == 0 and i != l-1:
                    res_file.write(str(path[i]))
                    res_file.write('->')
                elif i == l-1:
                    res_file.write(str(path[i]))
            all_path.append(path)
            res_file.write("\n")
            path.clear()
            path_cnt += 1
    # print(all_path)
    # size = max(len(testcase_list),path_cnt)
    res_file.write("用例：")
    res_file.write(str(testcase_list[path_cnt-1]))
    res_file.write("; 路径：")
    l = len(path)
    for i in range(l):
        if i % 2 == 0 and i != l-1:
            res_file.write(str(path[i]))
            res_file.write('->')
        elif i == l-1:
            res_file.write(str(path[i]))
    res_file.write("\n")        
    print(len(testcase_list))
    print(path_cnt)
    res_file.close()

  
def test():
    string = "mem=123mem=sdfsmem=zfha3233ru"
    splt = string.split("mem=")
    print(len(splt))
# test()
# num = input()
# path(num)
# cases(num)
# readtxtandHEX(num)
# readHEX(list,path_list)
# readHextxt()
readfirstHEX()
 
