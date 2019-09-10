import csv
import os
import os.path #文件夹遍历函数
import glob

dirpath='train/neg/'
#filenames=os.listdir(dirpath)
with open("data/neg.csv","w",newline='') as csvfile:
    writer = csv.writer(csvfile)
    for files in glob.glob(dirpath + "*.txt"):
        #filepath = dirpath+'/'+filename
        f = open(files,errors='ignore')             # 返回一个文件对象
        line = f.readline()             # 调用文件的 readline()方法
        writer.writerow([line])
        #print(line, end = '')
        f.close()

dirpath='train/pos/'
#filenames=os.listdir(dirpath)
with open("data/pos.csv","w",newline='') as csvfile:
    writer = csv.writer(csvfile)
    for files in glob.glob(dirpath + "*.txt"):
        #filepath = dirpath+'/'+filename
        f = open(files,errors='ignore')             # 返回一个文件对象
        line = f.readline()             # 调用文件的 readline()方法
        writer.writerow([line])
        #print(line, end = '')
        f.close()




