package com.suxsem.liquidnextparts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class UploadOnPastebin {

    public static String sub_paste(File log) throws IOException{
        
        String id_dev="ddae1cacf7205d6ce1516476859b2728";
        String paste_cont=URLEncoder.encode(get_text(log.getPath()),"UTF-8");
        String paste_title=URLEncoder.encode(log.getName(),"UTF-8");
                       
        URL url = new URL("http://pastebin.com/api_public.php");
URLConnection conn = url.openConnection();
conn.setDoOutput(true);
OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

wr.write("paste_code="+paste_cont+"&paste_name="+paste_title+"&dev_key="+id_dev);
wr.flush();

BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
String line;
while ((line = rd.readLine()) != null) {
    return(line);
}
wr.close();
rd.close();
return(null);
}


public static String get_text(String log)throws IOException{
       
        FileReader fr = new FileReader(log);
BufferedReader br = new BufferedReader(fr);
String line="";
String out="";
line=br.readLine();
while (line != null){
        out=out+"\n"+line;
        line=br.readLine();
}
fr.close();
br.close();
return(out);
}

	
}
