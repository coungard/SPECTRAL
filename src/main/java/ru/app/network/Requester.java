package ru.app.network;

import org.apache.log4j.Logger;
import ru.app.util.LogCreator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class Requester {
    private static final Logger LOGGER = Logger.getLogger(Requester.class);
    private String url;
    private static boolean flag;

    public Requester(String url) {
        this.url = url;
    }

    public static void goPay() {
        flag = true;
    }

    public String sendStatus(Payment payment, Status status) throws IOException {
        LOGGER.info(LogCreator.console("send status : " + status + " for " + payment));
        HttpURLConnection conn = HttpURLConnectionFactory.getHttpConnection(url);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);

        conn.addRequestProperty("Content-Type", "text/plain; charset=WINDOWS-1251");
        boolean success = status == Status.SUCCESS;

        StringBuilder request = new StringBuilder();
        request.append("<request>\n").append("  <type>result</type>\n").append("  <login>android</login>\n").append("  <imei>745646</imei>\n")
                .append("  <result>\n")
                .append("    <command_id>").append(payment.getId()).append("</command_id>\n")
                .append("    <status>")
                .append(success ? "ok" : "error")
                .append("    </status>\n");
        if (success) {
            request.append("    <data>\n").append("    \t<answer>")
                    .append("Пополнение ").append(payment.getNumber()).append(" на ").append(payment.getSum()).append(" рублей")
                    .append("</ansver>\n").append("    </data>\n");
        } else
            request.append("    <data></data>\n");
        request.append("  </result>\n").append("  <sign>iyewtr97y66ytq65rgeorrdgh346</sign>\n").append("</request>");

        byte[] data = request.toString().getBytes("CP1251");
        OutputStream os = conn.getOutputStream();
        os.write(data, 0, data.length);
        os.close();

        return new String(read(conn.getInputStream()), StandardCharsets.UTF_8);
    }

    /**
     * //3.0 запрос об удачном результате выполнения
     * // <data> может быть пустым или содержать <answer>
     * <request>
     * <type>result</type>
     * <login>android</login>
     * <imei>745646</imei>
     * <result>
     * <command_id>444</command_id>
     * <status>ok</status>
     * <data>
     * <answer>ваш баланс 100 рублей.</ansver>
     * </data>
     * </result>
     * <sign>iyewtr97y66ytq65rgeorrdgh346</sign>
     * </request>
     */

    public String checkPayment() throws IOException {
        HttpURLConnection conn = HttpURLConnectionFactory.getHttpConnection(url);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.addRequestProperty("Content-Type", "text/plain; charset=UTF-8");

        String request = "<request>\n" +
                "  <imei>357829072172464</imei>\n" +
                "  <login>samsung</login>\n" +
                "  <sign>21da91fd6534c5c21114d820763dbf10</sign>\n" +
                "  <type>command</type>\n" +
                "</request>";
        byte[] data = request.getBytes(StandardCharsets.UTF_8);
        OutputStream os = conn.getOutputStream();
        os.write(data, 0, data.length);
        os.close();

        if (flag) {
            flag = false;
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                    "<response>\n" +
                    "    <command>*9285685445*10*подарок от людей#</command>\n" +
                    "    <command_id>165679</command_id>\n" +
                    "    <command_type>ussd</command_type>\n" +
                    "    <command_wait_incoming_sms>true</command_wait_incoming_sms>\n" +
                    "    <sign>e328e4edbce842418b436f4e1946cc84</sign>\n" +
                    "</response>\n";
        }
        return new String(read(conn.getInputStream()), StandardCharsets.UTF_8);
    }

    private byte[] read(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
