package com.weeia.workers;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.StructuredName;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WorkerController {
    private java.net.URL URL;
    private List<WorkerModel> workers;
    private String inName;

    @RequestMapping("/")
    public String getDetailsAboutString() {
        ModelAndView mv = new ModelAndView("book/form");
        mv.addObject("work", new WorkerModel());
        return "search";
    }

    @RequestMapping(value = "/getWorkers/inName", params = "inName")
    public String getDetailsAboutString(@RequestParam String inName, Model model) {
        try {
            workers = new ArrayList<>();
            URL = new URL("https://adm.edu.p.lodz.pl/user/users.php?search="+inName);
            if (!(inName==null)){
                sendRequest();
                model.addAttribute("workersList", workers);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return "main";
    }

    private String sendRequest() {
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) URL.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            WorkerModel worker = null;
            String helpLine;
            int id=0;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("/user/profile.php?id=") && !inputLine.contains("img")){
                    worker = new WorkerModel();
                    helpLine = inputLine.substring(inputLine.indexOf("\"")+1, inputLine.indexOf("\" title"));
                    worker.setUrl(helpLine);
                    inputLine = inputLine.replace(inputLine.substring(inputLine.indexOf("<a"), inputLine.indexOf("title")),"");
                    helpLine = inputLine.substring(inputLine.indexOf("<h3>")+4, inputLine.indexOf("</a>"));
                    worker.setName(helpLine.substring(0,helpLine.indexOf(" ")));
                    worker.setSurname(helpLine.substring(helpLine.indexOf(" ")+1));
                    if (!inputLine.contains("nbsp")){
                        worker.setTitle(inputLine.substring(inputLine.indexOf("h4")+3, inputLine.indexOf(" </h4>")));
                    }

                }
                if (inputLine.contains("Afiliacja")){
                    helpLine = in.readLine();
                    if (worker!=null){
                        worker.setInstitute(helpLine.substring(helpLine.indexOf("content")+9, helpLine.indexOf("</span>")));
                        worker.setId(id++);
                    }
                    workers.add(worker);
                    System.out.println(worker.toString());
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "search";
    }


    @GetMapping("/createVCard/{id}")
    public void createVCard(@PathVariable Integer id, Model model, HttpServletResponse httpServletResponse) {
        for (WorkerModel worker : workers) {
            if (worker.getId()==id){
                VCard vcard = new VCard();
                StructuredName n = new StructuredName();
                n.setFamily(worker.getName());
                n.setGiven(worker.getSurname());
                n.getPrefixes().add(worker.getTitle());
                vcard.setStructuredName(n);
                vcard.setOrganization(worker.getInstitute());

                String str = Ezvcard.write(vcard).version(VCardVersion.V4_0).go();
                try {
                    String path = "vCard_"+worker.getSurname()+".vcf";
                    vcard.write(new File(path));

                    File fileToDownload = new File(path);
                    InputStream inputStream = new FileInputStream(fileToDownload);
                    httpServletResponse.addHeader("Content-Type", new MediaType("application", "force-download", StandardCharsets.UTF_8).toString());
                    httpServletResponse.setContentType("application/force-download");
                    httpServletResponse.setHeader("Content-Disposition", "attachment; filename="+path);
                    IOUtils.copy(inputStream, httpServletResponse.getOutputStream());
                    httpServletResponse.flushBuffer();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        model.addAttribute("workersList", workers);

    }

}
