package org.javahelper.core.file;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Description: 文件解压与压缩工具类
 * Apache Commons Compress，Compress是ApacheCommons提供压缩、解压缩文件的类库，定义了一个用于处理
 * ar，cpio，Unix dump，tar，zip，gzip，XZ，Pack200，bzip2、7z，arj，lzma，snappy，DEFLATE，
 * lz4，Brotli，Zstandard，DEFLATE64和Z文件的API ，非常强大
 * @author shenguangyang
 * @date 2021/04/01
 */
public class FileUnpackHelper {
    private static Logger log = LoggerFactory.getLogger(FileUnpackHelper.class);

    /**
     * 7Z压缩与解压工具
     */
    public static class SevenZ {
        public static void main(String[] args) {
            try {
                compress("/opt/image", "/opt/image.7z");
                unCompress("/opt/image.7z", "/opt/test");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /**
         * 7z文件压缩
         * 
         * @param inputFile  待压缩文件夹/文件名
         * @param outputFile 生成的压缩包名字
         */
        public static void compress(String inputFile, String outputFile) throws Exception {
            File input = new File(inputFile);
            if (!input.exists()) {
                throw new Exception(input.getPath() + "待压缩文件不存在");
            }
            SevenZOutputFile out = new SevenZOutputFile(new File(outputFile));
            compress(out, input, null);
            out.close();
        }
        /**
         * 递归压缩
         * @param name 压缩文件名，可以写为null保持默认
         */
        public static void compress(SevenZOutputFile out, File input, String name) throws IOException {
            if (name == null) {
                name = input.getName();
            }
            SevenZArchiveEntry entry = null;
            //如果路径为目录（文件夹）
            if (input.isDirectory()) {
                //取出文件夹中的文件（或子文件夹）
                File[] flist = input.listFiles();

                // 如果文件夹为空，则只需在目的地.7z文件中写入一个目录进入
                if (flist.length == 0) {
                    entry = out.createArchiveEntry(input,name + "/");
                    out.putArchiveEntry(entry);
                } else {
                    // 如果文件夹不为空，则递归调用compress，文件夹中的每一个文件（或文件夹）进行压缩
                    for (int i = 0; i < flist.length; i++) {
                        compress(out, flist[i], name + "/" + flist[i].getName());
                    }
                }
            } else {
                // 如果不是目录（文件夹），即为文件，则先写入目录进入点，之后将文件写入7z文件中
                FileInputStream fos = new FileInputStream(input);
                BufferedInputStream bis = new BufferedInputStream(fos);
                entry = out.createArchiveEntry(input, name);
                out.putArchiveEntry(entry);
                int len = -1;
                //将源文件写入到7z文件中
                byte[] buf = new byte[1024 * 1024 * 5];
                while ((len = bis.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                bis.close();
                fos.close();
                out.closeArchiveEntry();
                log.info("传输完成 ==> {}",entry.getName());
            }
        }

        /**
         * 文件解压
         * @param inputFile 待解压文件名
         * @param destDirPath 解压路径
         * @throws Exception
         */
        public static void unCompress(String inputFile, String destDirPath) throws Exception {
            // 获取当前压缩文件
            File srcFile = new File(inputFile);
            // 判断源文件是否存在
            if (!srcFile.exists()) {
                throw new Exception(srcFile.getPath() + "所指文件不存在");
            }
            // 开始解压
            SevenZFile zIn = new SevenZFile(srcFile);
            SevenZArchiveEntry entry = null;
            File file = null;
            while ((entry = zIn.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    file = new File(destDirPath, entry.getName());
                    if (!file.exists()) {
                        // 创建此文件的上级目录
                        new File(file.getParent()).mkdirs();
                    }
                    OutputStream out = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(out);
                    int len = -1;
                    byte[] buf = new byte[1024 * 1024 * 5];
                    while ((len = zIn.read(buf)) != -1) {
                        bos.write(buf, 0, len);
                    }
                    // 关流顺序，先打开的后关闭
                    bos.close();
                    out.close();
                }
            }
        }
    }

    /**
     * Java操作zip压缩和解压缩文件工具类
     * 由于直接使用java.util.zip工具包下的类，会出现中文乱码问题，所以使用ant.jar中的org.apache.tools.zip下的工具类
     * <dependency>
     *    <groupId>org.apache.ant</groupId>
     *    <artifactId>ant</artifactId>
     *    <version>1.10.9</version>
     * </dependency>
     * @author shenguangyang
     */
    public static class Zip {
        private static byte[] _byte = new byte[1024];

        /**
         * 对.zip文件进行解压
         * <请替换成功能描述> <br>
         * <请替换成详细描述>
         * @param zipFile 解压缩文件
         * @param targetPath 压缩的目标地址
         * @return
         * @author caizh
         * @since [1.0.0]
         * @version [1.0.0,2017年2月6日]
         */
        @SuppressWarnings("rawtypes")
        public static List<File> decompressionZipFile(File zipFile, String targetPath){
            List<File> list = new ArrayList<File>();
            try{
                ZipFile _zipFile = new ZipFile(zipFile,"GBK");
                for(Enumeration entries = _zipFile.getEntries(); entries.hasMoreElements();){
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    File _file = new File(targetPath+ File.separator+entry.getName());
                    if(entry.isDirectory()){
                        _file.mkdirs();
                    }else{
                        File _parent = _file.getParentFile();
                        if(!_parent.exists()){
                            _parent.mkdirs();
                        }
                        InputStream _in = _zipFile.getInputStream(entry);
                        OutputStream _out = new FileOutputStream(_file);
                        int len = 0;
                        while((len=_in.read(_byte))>0){
                            _out.write(_byte, 0, len);
                        }
                        _in.close();
                        _out.flush();
                        _out.close();
                        list.add(_file);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return list;
        }

        /**
         * 压缩zip文件
         * <请替换成功能描述> <br>
         * <请替换成详细描述>
         * @param targetPath 压缩的目的地址 如：E:/xxx.zip
         * @param sourceFiles 压缩的源文件
         * @author caizh
         * @since [1.0.0]
         * @version [1.0.0,2017年2月6日]
         */
        public static void compressZipFile(String targetPath,List<File> sourceFiles){
            try{
                if(targetPath.endsWith(".zip")|| targetPath.endsWith(".ZIP")){
                    ZipOutputStream zipOutStream = new ZipOutputStream(new FileOutputStream(new File(targetPath)));
                    zipOutStream.setEncoding("GBK");
                    for (File file : sourceFiles) {
                        addSourceFile(targetPath, zipOutStream, file, "");
                    }
                    zipOutStream.close();
                }else{
                    log.error("target file["+targetPath+"] is not .zip type file");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        /**
         * 压缩文件 注：如果压缩文件是一个文件夹，那压缩包存放的位置不能在该文件夹下
         * <请替换成功能描述> <br>
         * <请替换成详细描述>
         * @param targetPath 压缩的目的地址
         * @param zipOutStream
         * @param sourceFile 被压缩的文件
         * @param path 在zip中的相对路径
         * @author caizh
         * @since [1.0.0]
         * @version [1.0.0,2017年2月6日]
         * @throws IOException
         */
        private static void addSourceFile(String targetPath,ZipOutputStream zipOutStream,File sourceFile,String path) throws IOException {
            log.debug("begin to compression file["+sourceFile.getName()+"]");
            if(!"".equals(path) && !path.endsWith(File.separator)){
                path += File.separator;
            }
            if(!sourceFile.getPath().equals(targetPath)){
                if(sourceFile.isDirectory()){
                    File[] files = sourceFile.listFiles();
                    if(files.length==0){
                        zipOutStream.putNextEntry(new ZipEntry(path+sourceFile.getName()+File.separator));
                        zipOutStream.closeEntry();
                    }else{
                        for(File f:files){
                            addSourceFile(targetPath, zipOutStream, f, path+sourceFile.getName());
                        }
                    }
                }else{
                    InputStream in = new FileInputStream(sourceFile);
                    zipOutStream.putNextEntry(new ZipEntry(path+sourceFile.getName()));
                    int len = 0;
                    while((len=in.read(_byte))>0){
                        zipOutStream.write(_byte,0,len);
                    }
                    in.close();
                    zipOutStream.closeEntry();
                }
            }
        }
    }
}
