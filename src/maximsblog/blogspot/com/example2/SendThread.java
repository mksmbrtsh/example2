package maximsblog.blogspot.com.example2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Handler;

public class SendThread extends Thread {

	public final static byte STATE_NOT_STARTED = 0;
	public final static byte STATE_RUNNING = 1;
	public final static byte STATE_DONE = 2;

	public final static int MESSAGE_COMPLETE = 20;
	public final static int MESSAGE_ERROR = 30;
	public final static int MESSAGE_CANCEL = 50;
	
	
	public byte mbytStatus = STATE_NOT_STARTED;

	public Handler HandlerOfCaller;
	private String mNumberText;
	private String mTimeText;
	private String mContentFull;
	private int mResult;
	
	private static final String BASE_URL = "http://inyo.ru/droid.php/test?id=%s&time=%s";


	public byte getStatus() {
		return mbytStatus;
	}
	
	public String getResponse(){
		return mContentFull;
	}

	public SendThread(String numberText, String timeText, Handler handlerOfCaller) {
		HandlerOfCaller = handlerOfCaller;
		mNumberText = numberText;
		mTimeText = timeText;
	}

	@Override
	public void run() {
		mbytStatus = STATE_RUNNING;
		InputStream input;
		BufferedReader bufferReader = null;
		HttpURLConnection connection = null;
		try {
			URL urlWeb = new URL(String.format(BASE_URL, mNumberText, mTimeText));
			connection = (HttpURLConnection) urlWeb.openConnection();
			connection.setRequestProperty("Accept-Language", "ru");
			connection
					.setRequestProperty(
							"User-Agent",
							"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; InfoPath.2)");

			
			connection.connect();
			if (connection.getResponseCode() != 200) {
				connection.disconnect();
				return;
			}
			bufferReader = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), "windows-1251"));
			String inputLine = "";
			StringBuilder websiteContent = new StringBuilder();
			while ((inputLine = bufferReader.readLine()) != null) {
				websiteContent.append(inputLine);
				websiteContent.append('\n');
			}
			mContentFull = websiteContent.toString();
		} catch (MalformedURLException e) {
			HandlerOfCaller.sendEmptyMessage(mResult =MESSAGE_ERROR);
			e.printStackTrace();
			return;
		} catch (IOException e) {
			HandlerOfCaller.sendEmptyMessage(mResult = MESSAGE_ERROR);
			e.printStackTrace();

			return;
		}
		 finally {
				if (bufferReader != null) {
					try {
						bufferReader.close();

					} catch (IOException e) {
						return;
					}
				}
				if (connection != null) {
					((HttpURLConnection) connection).disconnect();
				}
			}
		mbytStatus = STATE_DONE;
		HandlerOfCaller.sendEmptyMessage(mResult = MESSAGE_COMPLETE);

		return;
	}

	public int getMessage() {
		
		return mResult;
	}
}