package ru.app.network;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.app.main.Settings;
import ru.app.util.LogCreator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Helper {
    private static final Logger LOGGER = Logger.getLogger(Helper.class);

    public static Payment createPayment(String response) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource src = new InputSource();
        src.setCharacterStream(new StringReader(response));

        Document doc = builder.parse(src);
        String cmd1 = doc.getElementsByTagName("command").item(0).getTextContent();
        String cmd2 = doc.getElementsByTagName("command_id").item(0).getTextContent();

        String[] data = cmd1.split("\\*");

        Payment payment = new Payment();
        payment.setId(Long.parseLong(cmd2));
        payment.setNumber(data[1]);
        payment.setSum(Double.parseDouble(data[2]));
        payment.setProvider(data[3]);
        payment.setText(data[4].substring(0, data[4].length() - 1)); // w/o # symbol

        return payment;
    }

    public static Map<String, String> loadProp(File payFile) {
        String text = null;
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(payFile.toURI()));
            text = new String(encoded);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return JsonHelper.jsonToMapStringString(text);
    }

    public static void saveProp(Map<String, String> prms, File payFile) {
        try {
            String prop = JsonHelper.mapStringStringToJson(prms);
            PrintWriter printWriter = new PrintWriter(payFile, "cp1251");
            printWriter.write(prop);
            printWriter.close();
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.getMessage(), ex);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(Payment payment, Status status) throws IOException {
        long id = payment.getId();
        Path payFile = Paths.get(Settings.paymentPath);
        if (!Files.exists(payFile)) {
            LOGGER.error(LogCreator.console("PAYMENT FILE NOT EXISTS!"));
            return;
        }
        long timestamp = System.currentTimeMillis();
        Path target = Paths.get((status == Status.SUCCESS ? Settings.successDir : Settings.errorDir) + "payment_" + id + "_t" + timestamp);
        Files.copy(payFile, target);
        Files.delete(payFile);
        LOGGER.info(LogCreator.console("payment file: " + target.toString() + " successfully saved"));
    }

    /**
     *
     *  String yes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
     *                 "<response>\n" +
     *                 "    <command>*9285685445*10*text#</command>\n" +
     *                 "    <command_id>165679</command_id>\n" +
     *                 "    <command_type>ussd</command_type>\n" +
     *                 "    <command_wait_incoming_sms>true</command_wait_incoming_sms>\n" +
     *                 "    <sign>29d25982a4dc5b8361c578ad78fbf749</sign>\n" +
     *                 "</response>";
     */
}
