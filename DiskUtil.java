import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DiskUtil {

    private static List<List<String>> diskInfoList = new ArrayList<>();
    private static List<List<String>> diskIDList = new ArrayList<>();
    private static boolean hasDiskID = false;

    /**
     * 获取硬盘ID，硬盘总容量和和实时占用率
     * @return 返回一个二维List，每个List<String>的第一个是磁盘id，第二个是总容量，第三个是占用率
     */
    public static List<List<String>> getDiskInfo()
    {
        List<List<String>> diskPartInfoList = new ArrayList<>();//直接获取的分卷信息，第一个是分卷LTR，第二个是可用空间，第三个是总大小
        if(!hasDiskID) {
            diskIDList = getDiskIDList();
            hasDiskID = true;
        }

        File[] disks = File.listRoots();
        List<String> parts;
        for(File file : disks)
        {
            parts = new ArrayList<>();
            parts.add(file.getPath().trim().substring(0, 1));
            parts.add(file.getFreeSpace() + "");
            parts.add(file.getTotalSpace() + "");
            diskPartInfoList.add(parts);
        }

        if(hasDiskID) {
            diskInfoList.clear();
            for(int i = 0; i < diskIDList.size(); i++) {
                List<String> diskInfo = new ArrayList<>();
                long totalB = 0;
                long freeB = 0;
                for(String part : diskIDList.get(i)) {
                    for(int j = 0; j < diskPartInfoList.size(); j++) {
                        if(part.trim().equals(diskPartInfoList.get(j).get(0).trim())){
                            freeB += Long.parseLong(diskPartInfoList.get(j).get(1));
                            totalB += Long.parseLong(diskPartInfoList.get(j).get(2));
                        }
                    }
                }
                diskInfo.add(diskIDList.get(i).get(0));
                diskInfo.add(totalB / 1024 / 1024 / 1024 + "G");
                String s = (1 - freeB / (totalB + 0.)) + "";
                int a = s.indexOf(".");
                diskInfo.add(s.substring(a + 1, a + 3) + "%");
                diskInfoList.add(diskInfo);
            }
        }

        return diskInfoList;
    }

    /**
     * 获取磁盘数量
     * @return
     */
    public static int getDiskNumber() {
        return diskInfoList.size();
    }

    /**
     * 调用diskpart来获取磁盘ID及磁盘分区
     * @return 返回一个二维List，每个List<String>的第一个是磁盘id，后面是磁盘分卷的LTR
     */
    private static List<List<String>> getDiskIDList(){
        try {
            File file1 = File.createTempFile("fuck", ".txt");
            file1.deleteOnExit();
            FileWriter fileWriter1 = new FileWriter(file1);
            String txt = "select disk 0\n" + "detail disk\n" + "select disk 1\n" + "detail disk\n" + "select disk 2\n"
                    + "detail disk\n" + "select disk 3\n" + "detail disk\n" + "select disk 4\n" + "detail disk\n" + "select disk 5\n" + "detail disk\n" + "exit";
            fileWriter1.write(txt);
            fileWriter1.close();

            File file2 = File.createTempFile("shit", ".os");
            file2.deleteOnExit();

            File file3 = File.createTempFile("nmsl", ".bat");
            file3.deleteOnExit();
            FileWriter fileWriter2 = new FileWriter(file3);
            String bat = "(for /f \"delims=\" %%i in ('diskpart /s " + file1.getPath() + "\n') do echo %%i)>"+ file2.getPath() +"\n" +
                    "Start "+ file2.getPath() + "\n" +
                    "REM Start \"\" \" " + file2.getPath() + "\"";
            fileWriter2.write(bat);
            fileWriter2.close();
            String[] cmd = new String[]{file3.getPath()};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            while ((input.readLine()) != null) {
                System.out.println("今天天气真好");
            }
            input.close();

            return getDiskIDString(file2);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从临时文件shit.os获取硬盘序列号及分卷，
     * @param file 临时文件shit.os
     * @return 返回一个二维List，每个List<String>的第一个是磁盘id，后面是磁盘分卷的LTR
     */
    private static List<List<String>> getDiskIDString(File file) {
        BufferedReader reader = null;
        List<List<String>> diskList = new ArrayList<>();
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "gbk"));
            String tempStr;
            boolean enterFlag = false;
            List<String> aDisk = new ArrayList<>();
            while ((tempStr = reader.readLine()) != null) {
                if(tempStr.length() > 6){
                    if(!enterFlag && tempStr.substring(0, 6).equals("磁盘 ID:")) {
                        String diskID = tempStr.substring(tempStr.indexOf("{")+1,tempStr.indexOf("}"));
                        aDisk.add(diskID);
                        continue;
                    }
                    if(!enterFlag && tempStr.trim().equals("卷 ###      LTR  标签         FS     类型        大小     状态       信息")) {
                        enterFlag = true;
                        continue;
                    }
                    if(enterFlag && tempStr.trim().startsWith("卷")) {
                        aDisk.add(tempStr.charAt(14) + "");
                        continue;
                    }
                    if(enterFlag && (tempStr.startsWith("你指定") || tempStr.startsWith("磁盘"))) {
                        enterFlag = false;
                        diskList.add(aDisk);
                        aDisk = new ArrayList<>();
                    }
                }
            }
            reader.close();
            return diskList;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }
}
