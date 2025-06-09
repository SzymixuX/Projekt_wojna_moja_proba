package pl.wojna.config;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;

import java.io.File;

public class ConfigLoader
{
    private static int serverPort;
    private static String dbUrl;
    private static String dbUser;
    private static String dbPass;

    public static void load()
    {
        try {
            File xmlFile = new File("config.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            doc.getDocumentElement().normalize();

            serverPort = Integer.parseInt(doc.getElementsByTagName("serverPort").item(0).getTextContent());
            dbUrl = doc.getElementsByTagName("url").item(0).getTextContent();
            dbUser = doc.getElementsByTagName("user").item(0).getTextContent();
            dbPass = doc.getElementsByTagName("password").item(0).getTextContent();

            //System.out.println("Zaladowano konfigurację z pliku config.xml");

        }
        catch (Exception e)
        {
            System.err.println(" Blad podczas wczytywania config.xml:");
            e.printStackTrace();
        }
    }

    public static int getServerPort()
    {
        return serverPort;
    }

    public static String getDbUrl()
    {
        return dbUrl;
    }

    public static String getDbUser()
    {
        return dbUser;
    }

    public static String getDbPass()
    {
        return dbPass;
    }
}
