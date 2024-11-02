#!/usr/bin/python3
import struct
import os

def readHextxt():
    binfile = open("/home/wj/pofChain/AFL/afl_testfiles/window_testcases/testcase_1", 'rb')
    size = os.path.getsize("/home/wj/pofChain/AFL/afl_testfiles/window_testcases/testcase_1")   #字节数
    content = binfile.read()
    split_content = content.split(b'mem=')
    binfile.close()
    print(len(split_content)-1)
    print(split_content[1:])
    return split_content[1:]

def readHEX():
    path = []
    path_cnt = 0
    binfile = open("/home/wj/pofChain/AFL/afl_testfiles/window_paths/testfile_1", 'rb')
    size = os.path.getsize("/home/wj/pofChain/AFL/afl_testfiles/window_paths/testfile_1")   #字节数
    for i in range(size//2):
        data = binfile.read(2)   #每次输出两个字节
        num = struct.unpack('H',data)  #H -- C中的unsigned short（0～65535）2 Byte
        if num[0] != 0:
            ls_num = num[0] << 1
            path.append(ls_num)
        else:
            path.append(num[0])
            path_cnt += 1
    binfile.close()
    print(path_cnt)
    return path

def readfirstHEX():
    print("start\n")
    # with open("/home/wj/afl_testfiles/files/testfile1", 'rb') as file:
    #     data = file.read()
    #     for byte in data:
    #         print("0x{:02x}".format(byte))
    path = []
    cnt = 0
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

def path():
    path_file = open("/home/wj/pofChain/AFL/path_file.txt", mode="w")
    path_list = readHEX()
    all_path = []
    path = []
    path_cnt = 1
    # print(path_list[1:])
    for p in path_list:
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
    print(all_path)
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


def readtxtandHEX():
    res_file = open("/home/wj/pofChain/AFL/path_file.txt", mode="w")
    testcase_list = readHextxt()
    path_list = readHEX()
    print(path_list[:150])
    # 将path_list中的路径分离开
    all_path = []
    path = []
    path_cnt = 1
    for p in path_list[1:]:
        if p != 0:
            path.append(p)
        else:
            res_file.write("用例：")
            res_file.write(str(testcase_list[path_cnt-1]))
            res_file.write("; 路径：")
            l = len(path)
            for i in range(l):
                if i != l-1:
                    res_file.write(str(path[i]))
                    res_file.write('->')
                else:
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

# readHextxt()
# readHEX()
readtxtandHEX()
