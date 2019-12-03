package com.weeia.workers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WorkerController {
    private java.net.URL URL;
    private List<WorkerModel> workers;



    @RequestMapping("/getWorkers/{name}")
    public String getDetailsAboutString(@PathVariable String name, Model model) {
        try {
            workers = new ArrayList<>();
            URL = new URL("https://adm.edu.p.lodz.pl/user/users.php?search="+name);
            sendRequest();
            model.addAttribute("workersList", workers);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return "main";
    }

    private void sendRequest() {
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
    }
}
