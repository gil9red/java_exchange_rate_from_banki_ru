import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by ipetrash on 20.10.2016.
 */
public class Main {
/*
def exchange_rate(currency_id, timestamp=None):
    if timestamp is None:
        from datetime import datetime
        timestamp = int(datetime.today().timestamp())

    data = {
        'currency_id': currency_id,
        'date': timestamp
    }

    headers = {
        'X-Requested-With': 'XMLHttpRequest',
        'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0'
    }

    import requests
    rs = requests.post('http://www.banki.ru/products/currency/ajax/quotations/value/cbr/', json=data, headers=headers)
    return rs.json()['value']


if __name__ == '__main__':
    # 840 -- USD
    print('USD:', exchange_rate(840))

    # 978 -- EUR
    print('EUR:', exchange_rate(978))
*/

    private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0";

    // HTTP POST request
    private static double exchange_rate(int currencyId) throws Exception {
        String url = "http://www.banki.ru/products/currency/ajax/quotations/value/cbr/";
        URL obj = new URL(url);

        HttpURLConnection connection = null;

        // Почему-то в реализации java сервер часто не отдает данные и ругается 400 кодом через раз
        // поэтому долбим его запросами пока не отдаст
        int responseCode = 400;
        while (responseCode == 400) {
            connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            connection.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");

            // Manual fill json data
            long timestamp = new java.util.Date().getTime() / 1000;
            String postData = String.format("{'date': %s, 'currency_id': %s}", timestamp, currencyId);

            // Send post request
            connection.setDoOutput(true);

            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.writeBytes(postData);
            }

            responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 400) {
                throw new Exception(String.format("Response code not ok: %s", responseCode));
            }

            Thread.sleep(333);
        }

        StringBuffer response = new StringBuffer();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        JSONObject json = new JSONObject(response.toString());
        return json.getDouble("value");
    }

    static public void main(String[] args) throws Exception {
        try {
            // 840 -- USD
            System.out.println("USD: " +exchange_rate(840));

            // 978 -- EUR
            System.out.println("EUR: " +exchange_rate(978));

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            new Throwable().printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            System.out.println("ERROR: " + e + "\n\n" + stackTrace);
        }
    }
}
