package utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

public class HDFSUtil {
    private static String hdfsURL = "hdfs://localhost:9000";
    private static Configuration conf;

    static {
        conf = new Configuration();
        conf.set("fs.defaultFS", hdfsURL);
    }

    // 从HDFS上下载文件
    public static void downloadFromHDFS(String remoteFile, String localFile) throws Exception {
        FileSystem fs = FileSystem.get(conf);
        Path remotePath = new Path(remoteFile);
        boolean b = FileUtil.copy(fs, remotePath, new File(localFile), false, conf);
        if (b){
            System.out.println("文件下载成功！");
        }else{
            System.out.println("文件下载失败！");
        }

        fs.close();
    }

    // 上传文件到HDFS
    public static void uploadToHDFS(String localfile, String remotefile) throws Exception {
        FileSystem fs = FileSystem.get(conf);
        Path remotePath = new Path(remotefile);
        Path localPath = new Path(localfile);
        fs.copyFromLocalFile(localPath, remotePath);
        fs.close();
    }

    // 创建HDFS文件夹
    public static void createDirFromHDFS(String remoteDir) throws Exception {
        FileSystem fs = FileSystem.get(conf);
        Path remotePath = new Path(remoteDir);
        //调用mkdirs函数创建目录
        fs.mkdirs(remotePath);
        fs.close();
    }

    // 删除文件和文件夹
    public static void deleteFromHDFS(String remoteDir) throws Exception {
        FileSystem fs = FileSystem.get(conf);
        // 构建完整的HDFS路径
        Path filePath = new Path(remoteDir);
        System.out.println(filePath);
        // 调用delete函数删除文件或目录
        boolean success = fs.delete(filePath, true); // true表示递归删除
        fs.close();
        if (success){
            System.out.println("删除文件/文件夹成功!");
        }else
            System.out.println("删除文件/文件夹失败!");

    }

    // 重命名文件
    public static void renameFromHDFS(String oldName, String newName) throws Exception {
        FileSystem fs = FileSystem.get(conf);
        Path hdfsOldName = new Path(oldName);
        Path hdfsNewName = new Path(newName);
        boolean b = fs.rename(hdfsOldName, hdfsNewName);
        if (b){
            System.out.println("重命名成功!");
        }else
            System.out.println("重命名失败！");
        fs.close();
    }

    // 查看文件夹
    public static void ListHDFSDir(String remoteDir) throws Exception {
        FileSystem fs = FileSystem.get(conf);
        Path dirPath = new Path(remoteDir);
        /*递归获取目录下的所有文件*/
        RemoteIterator<LocatedFileStatus> remoteIterator = fs.listFiles(dirPath, true);
        /*输出每个文件的信息*/
        while (remoteIterator.hasNext()) {
            FileStatus s = remoteIterator.next();
            System.out.println("路径: " + s.getPath().toString());
            System.out.println("权限: " + s.getPermission().toString());
            System.out.println("大小: " + s.getLen());
            /*返回的是时间戳,转化为时间日期格式*/
            Long timeStamp = s.getModificationTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = format.format(timeStamp);
            System.out.println("时间: " + date);
            System.out.println();
        }
        fs.close();
    }

    // 列出文件
    public static List<String> listRemoteFiles(String remoteDir) throws Exception {
        List<String> remoteFileNameList = new ArrayList<>();
        FileSystem fs = FileSystem.get(conf);
        Path dirPath = new Path(remoteDir);
        /*递归获取目录下的所有文件*/
        RemoteIterator<LocatedFileStatus> remoteIterator = fs.listFiles(dirPath, true);
        /*输出每个文件的信息*/
        while (remoteIterator.hasNext()) {
            FileStatus s = remoteIterator.next();
            String fileName = s.getPath().getName();
            remoteFileNameList.add(fileName);
        }
        fs.close();
        return remoteFileNameList;
    }

    // 列出文件夹
    public static List<String> listRemoteDir(String remoteDir) throws Exception {
        List<String> remoteDirList = new ArrayList<>();
        FileSystem fs = FileSystem.get(conf);
        Path dirPath = new Path(remoteDir);
        FileStatus[] fileStatus = fs.listStatus(dirPath);
        for (FileStatus file : fileStatus) {
            if (file.isDirectory()) {
                // 递归获取子目录，并将结果添加到 remoteDirList
                remoteDirList.add(file.getPath().toString());
                remoteDirList.addAll(listRemoteDir(file.getPath().toString()));
            } else {
                remoteDirList.add(file.getPath().toString());
            }
        }
        fs.close();
        return remoteDirList;
    }


    // 移动文件夹
    public static void moveDirFromHDFS(String oldPath, String newPath) throws Exception {
        FileSystem fs = FileSystem.get(conf);
        Path hdfsOldName = new Path(oldPath);
        Path hdfsNewName = new Path(newPath);
        fs.rename(hdfsOldName, hdfsNewName);
        fs.close();
    }

    // 测试
//    public static void main(String[] args) {
//        try {
////            1、列出文件
//            List<String> list = HDFSUtil.listRemoteFiles("/");
//            for (int i = 0; i < list.size(); i++) {
//                System.out.println(list.get(i));
//            }
//
////            2、重名文件
////            HDFSUtil.renameFromHDFS("/start-dfs.cmd", "/start-dfs.sh");
////
//////            3、创建文件夹
////            HDFSUtil.createDirFromHDFS("/hive");
////
//////            4、移动文件夹（把 / 路径下的hive文件夹，放到 / user路径）
////            HDFSUtil.moveDirFromHDFS("/hive", "/user");
////
//////            5、删除文件或者文件夹
////            HDFSUtil.deleteFromHDFS("/user");
////            HDFSUtil.deleteFromHDFS("/user/hive/start-dfs.sh");
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
