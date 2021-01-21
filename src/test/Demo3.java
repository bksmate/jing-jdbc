package test;

import org.jing.core.util.FileUtil;
import org.jing.core.util.GenericUtil;
import org.jing.core.util.StringUtil;
import org.jing.jdbc.lang.JingJDBC;

import java.lang.Exception;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description: <br>
 *
 * @author: bks <br>
 * @createDate: 2020-09-07 <br>
 */
public class Demo3 {
    private Demo3() throws Exception {
        Pattern pattern = Pattern.compile("<img.*?src=\"(.+?hnnhwz.+?upload.+?image.+?\\..+?)\"");
        Matcher matcher;
        JingJDBC jdbc = JingJDBC.getJDBC("mysql");
        List<HashMap<String, String>> qryList = jdbc.qry("select t.id, t.content_details from cms_content t");
        int size = GenericUtil.countList(qryList);
        HashMap<String, String> row;
        String content;
        String fileName;
        List<String> imageList = new ArrayList<String>();
        for (int i$ = 0; i$ < size; i$++) {
            row = qryList.get(i$);
            content = StringUtil.getMapString(row, "content_details");
            matcher = pattern.matcher(content);
            if (matcher.find()) {
                content = matcher.group(1);
                fileName = content.substring(content.lastIndexOf("/") + 1);
                imageList.add(String.format("mv \"image_bak/%s\" \"image/%s\"", fileName, fileName));
            }
        }
        FileUtil.writeFile("C:\\Users\\kcb-lxd\\Desktop\\mvList.txt", imageList);
    }

    public static void main(String[] args) throws Exception {
        new Demo3();
    }
}
