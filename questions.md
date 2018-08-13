## Questions

#### 1. How many ways do you know of running code in a background thread and what are the advantages and disadvantages of each?

#### Thread & Handler
Create a Thread with a Runnable and do the heavy operation, when the heavy operation ends call the handler created earlier on the UI Thread.
By default the views have a handler, so you can do View.post(). The Handler is the means of communication between the heavy task running on the created thread and the UI Thread.
```java
//...

Thread thread = new Thread(new Runnable() {
	@Override
	public void run() {
		final String result = performBlockingTask();

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTextView.setText(result);
			}
		});
	}
});

thread.start();

//...
```

Other way,

```java
//...

final Handler handler = new Handler();
final Runnable uiRunnable = new Runnable() {
	@Override
	public void run() {
		 mTextView.setText(mResult);
	}
};

Thread thread = new Thread(new Runnable() {
	public void run() {
		mResult = performBlockingTask();
		handler.post(uiRunnable);
	}
});

thread.start();

//...
```
Problems:
* No support for configuration changes, in other words if our application supports both orientations (landscape and portrait) we have to handle the states of the Threads and Handlers when the user rotates his device.
* Boilerplate code and un-friendly read.
* No error handler.

#### IntentService

Useful when it’s necessary to run tasks in the background that don’t affect the UI. They are completely decoupled from view, ie they are not affected by the life cycle of the Activity.
```java
public class BackgroundService extends IntentService {
    private static final String TAG = BackgroundService.class.getSimpleName();

    public BackgroundService() {
        super(TAG );
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        blockingTask();
    }
    
    //...
}
```
It must be declared in the AndroidManifest.xml just like any other Service. Then it can then be started by sending an Intent (that can include any parameters).

Problems:
* Doesn’t have direct communication with the UI.
* The way to send results to the view is through Broadcast Receivers or some event bus strategy.

#### AsyncTask
Run instructions in the background and synchronize again with the Main Thread. Useful for short background operations.
```java
//...

new AsyncTask<Void, Void, String>() {
	@Override
	protected void onPreExecute() {
		//...
	}

	@Override
	protected String doInBackground(Void... voids) {
		return performBlockingTask();
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		mProgessBar.setProgress(progress);
	}

	@Override
	protected void onPostExecute(String result) {
		mTextView.setText(result);
	}
   
}.execute();

//...
```
How it works ?

* onPreExecute(), onPostExecute() and onProgressUpdate() are all invoked on the UI Thread.
* The value returned by doInBackground() is sent to onPostExecute().
* You can call publishProgress() at anytime in doInBackground() to execute onProgressUpdate() on the UI thread.

Problems:

* No support for configuration changes.
* No error handler, so we have to ask onPostExecute whether the result is an error or not.
* Complex chain of asynchronous operations, so it is advisable that the operations performed in the background are all synchronized to avoid the famous Callback Hell.
* Each AsyncTask instance can be executed only/just one time.

#### Loader & Cursor Loader

Allows you to load data asynchronously in an Activity or Fragment. They can monitor the source of the data and deliver new results when the content changes.

Loaders persist and cache results across configuration changes to prevent duplicate queries.
Loaders can implement an observer to monitor for changes in the underlying data source. For example, Android provides a default Loader implementation to handle SQlite database connections, the CursorLoader class, which automatically registers a ContentObserver to trigger a reload when data changes.

```java
public class ExampleTaskLoader extends AsyncTaskLoader<String> {
  
	public ExampleTaskLoader(Context context) {
		super(context);
		onContentChanged();
	}

	@Override
	public String loadInBackground() {
		return performBlockingTask();
	}
	
	//...
}
```
```java
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {
	private int LOADER_ID = 1;
	//...

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//...
		
		if (getLoaderManager().getLoader(LOADER_ID) == null) {
			getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
		}
		else 
			getLoaderManager().initLoader(LOADER_ID, null, this);
		}
	}

	@Override
	public Loader onCreateLoader(int i, Bundle bundle) 
		return new ExampleTaskLoader(this);
	}

	@Override
	public void onLoadFinished(Loader loader, Object o) 
		mTextView.setText((String) o);
	}

	@Override
	public void onLoaderReset(Loader loader) 
		loader.reset();
	}
}
```
Problems:

* This implementation takes time compared to other alternatives.
* No error handler.

#### RxAndroid


#### 2. How many ways do you know of persisting data and what are the advantages and disadvantages of each?




#### 3. How many ways do you know of reusing UI components/screens and when would you use each?
