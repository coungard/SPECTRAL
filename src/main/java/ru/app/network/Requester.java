package ru.app.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class Requester {

    private String url;

    public Requester(String url) {
        this.url = url;
    }

    public static void main(String[] args) throws IOException {
        Requester requester = new Requester("http://192.168.15.121:8080/ussdWww/");
        System.out.println(requester.check());
    }

    public String check() throws IOException {
        HttpURLConnection conn = HttpURLConnectionFactory.getHttpConnection(url);
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);

        conn.addRequestProperty("Content-Type", "text/plain; charset=WINDOWS-1251");

        String request = "<request>\n" +
                "  <imei>357829072172464</imei>\n" +
                "  <login>samsung</login>\n" +
                "  <sign>21da91fd6534c5c21114d820763dbf10</sign>\n" +
                "  <type>command</type>\n" +
                "</request>";
        byte[] data = request.getBytes("CP1251");
        OutputStream os = conn.getOutputStream();
        os.write(data, 0, data.length);
        os.close();

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
