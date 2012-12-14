package maximsblog.blogspot.com.example2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class Main extends Activity implements OnClickListener, Callback {

	private EditText mEditor;
	Handler mHandlerOfCaller = new Handler(this);;
	private String mNumberString;
	private ArrayList<String> mAnswers;
	private SendThread mThread;
	private ListView mList;
	private Button mSend;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// views
		mEditor = (EditText) findViewById(R.id.editor);
		mSend = (Button) findViewById(R.id.send_button);
		mList = (ListView) findViewById(R.id.list);
		// autoscroll to last item
		mList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mSend.setOnClickListener(this);
		ArrayAdapter<String> adapter;
		if (savedInstanceState != null) {
			// restore list and send data
			mNumberString = savedInstanceState.getString("numberString");
			mAnswers = savedInstanceState.getStringArrayList("answers");
			mEditor.setText(mNumberString);
			adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, mAnswers);
		} else {
			// empty list and send data
			mAnswers = new ArrayList<String>();
			adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, mAnswers);
		}
		mList.setAdapter(adapter);
		if (getLastNonConfigurationInstance() != null) {
			// restore thread
			mThread = (SendThread) getLastNonConfigurationInstance();

			mThread.HandlerOfCaller = mHandlerOfCaller;
			if (mThread.getStatus() == mThread.STATE_RUNNING) {
				mSend.setEnabled(false);
			} else if (mThread.getStatus() == mThread.STATE_NOT_STARTED) {
			} else if (mThread.getStatus() == mThread.STATE_DONE) {
				if (mThread.getResponse() != null) {
					mAnswers.add(mThread.getResponse());
					((ArrayAdapter<String>) mList.getAdapter())
							.notifyDataSetChanged();

				}
				mSend.setEnabled(true);
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("numberString", mEditor.getText().toString());
		outState.putStringArrayList("answers", mAnswers);
		super.onSaveInstanceState(outState);
	};

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mThread != null && mThread.getStatus() == mThread.STATE_RUNNING) {
			mThread.HandlerOfCaller = null;
			return mThread;
		}
		return null;

	};

	@Override
	public void onClick(View v) {
		String numberTextv = mEditor.getText().toString();

		if (numberTextv.length() > 0) {
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			StringBuilder timeText = new StringBuilder();
			timeText.append(c.getTimeInMillis() / 1000);
			mSend.setEnabled(false);
			mThread = new SendThread(numberTextv, timeText.toString(),
					mHandlerOfCaller);
			mThread.start();
			
		}
	}

	@Override
	public boolean handleMessage(Message arg0) {
		if (arg0.what == SendThread.MESSAGE_COMPLETE) {
			mAnswers.add(mThread.getResponse());
			((ArrayAdapter<String>) mList.getAdapter()).notifyDataSetChanged();
		}
		mSend.setEnabled(true);
		return false;
	}

}
