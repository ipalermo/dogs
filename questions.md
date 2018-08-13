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
This alternative is based on Reactive Programming, which in a nutshell is a paradigm oriented around data flows and the propagation of change, a mix of the Observer and Iterable Pattern.
In order to use this paradigm we have the RxJava library which is an implementation of the Reactive extensions and the RxAndroid library that provides mechanisms to communicate with the main thread.
Example of use:
```java
//---

Single.create((SingleOnSubscribe<String>) e -> e.onSuccess(makeBlockingTask()))
    .subscribeOn(Schedulers.io()) //Run on IO Thread
    .observeOn(AndroidSchedulers.mainThread()) //Run on UI Thread
    .doOnSubscribe(__ -> showLoadingIndicator())
    .doFinally(() -> hideLoadingIndicator())
    .subscribe(this::processResult,
         this::processError);

//---
```

Advantages:
* easy to use 
* doesn’t generate much overhead in the code (no boilerplate code)
* has a lot of operations that will facilitate the handling of data structures and prevent the famous Callback Hell
* has error handler

Problems:
* it doesn’t solve the problem of persistence to changes of configuration.


#### 2. How many ways do you know of persisting data and what are the advantages and disadvantages of each?

Android provides several options to persist app data. The solution chosen depends on the specific needs, such as how much space the data requires, what kind of data needs to be stored, and whether the data should be private to the app or accessible to other apps and the user.

#### Internal storage
By default, files saved to the internal storage are private to your app, and other apps cannot access them (nor can the user, unless they have root access). This makes internal storage a good place for internal app data that the user doesn't need to directly access. The system provides a private directory on the file system for each app where you can organize any files your app needs.

When the user uninstalls your app, the files saved on the internal storage are removed. Because of this behavior, internal storage  should not be used to save anything the user expects to persist independenly of your app. For example, if the app allows users to capture photos, the user would expect that they can access those photos even after they uninstall your app. So those types of files should instead be saved to the public external storage.

##### Internal cache files
If the need is to keep some data temporarily, rather than store it persistently, the special cache directory should be used to save the data. Each app has a private cache directory specifically for these kinds of files. When the device is low on internal storage space, Android may delete these cache files to recover space. However, developers should not rely on the system to clean up these files for them. Devs should always maintain the cache files by themselves and stay within a reasonable limit of space consumed, such as 1MB. When the user uninstalls your app, these files are removed.

#### External storage
Every Android device supports a shared "external storage" space that can be used to save files. This space is called external because it's not a guaranteed to be accessible—it is a storage space that users can mount to a computer as an external storage device, and it might even be physically removable (such as an SD card). Files saved to the external storage are world-readable and can be modified by the user when they enable USB mass storage to transfer files on a computer.

So before you attempt to access a file in external storage in your app, you should check for the availability of the external storage directories as well as the files you are trying to access.

Most often, you should use external storage for user data that should be accessible to other apps and saved even if the user uninstalls your app, such as captured photos or downloaded files. The system provides standard public directories for these kinds of files, so the user has one location for all their photos, ringtones, music, and such.

You can also save files to the external storage in an app-specific directory that the system deletes when the user uninstalls your app. This might be a useful alternative to internal storage if you need more space, but the files here aren't guaranteed to be accessible because the user might remove the storage SD card. And the files are still world readable; they're just saved to a location that's not shared with other apps.

#### Shared preferences
If you don't need to store a lot of data and it doesn't require structure, you should use SharedPreferences. The SharedPreferences APIs allow you to read and write persistent key-value pairs of primitive data types: booleans, floats, ints, longs, and strings.

The key-value pairs are written to XML files that persist across user sessions, even if your app is killed. You can manually specify a name for the file or use per-activity files to save your data.

You can use SharedPreferences to save any kind of simple data, such as the user's high score. However, if you do want to save user preferences for your app, then you should probably create a settings UI, which uses PreferenceActivity to build a settings screen and automatically persist the user's settings.

#### Databases
For large structured datasets that need to be persisted and queried, Android allows you to store them in a private local databases.
Android provides full support for SQLite databases. Any database you create is accessible only by your app. It's possible to use SQLite APIs directly, but it's recommended that you create and interact with your databases with a library such as Room persistence library, Realm, etc.
These libraries provides an object-mapping abstraction layer that allows fluent database access while harnessing the full power of SQLite.

Although you can still save data directly with SQLite, the SQLite APIs are fairly low-level and require a great deal of time and effort to use. For example:
* There is no compile-time verification of raw SQL queries.
* As your schema changes, you need to update the affected SQL queries manually. This process can be time consuming and error prone.
* You need to write lots of boilerplate code to convert between SQL queries and Java data objects.
The persistence libraries like Room or Realm, take care of these concerns for you while providing an abstraction layer over SQLite.

#### Remote/Cloud storage
There are also lots of cloud storage services and APIs that can be used as a remote persistence for an application. All major cloud providers offer different storage options depending on the data to be persisted. An on-premise backend server can also be used as a remote API to store data. All this options are suitable when the data can't be persisted on the local device for different business needs. The most common drawbacks are the cost of this server or services, the restrictions or difficulties to move the data to other clouds(because of the heavy coupling they introduce and the app refactoring needed).  

#### 3. How many ways do you know of reusing UI components/screens and when would you use each?

#### Reusing Layout files with <include/>
One way to efficiently re-use complete layouts, it's possible by using the <include/> and <merge/> tags to embed another layout inside the current layout. Reusing layouts is particularly powerful as it allows you create reusable complex layouts. For example, a yes/no button panel, or custom progress bar with description text. It also means that any elements of the application that are common across multiple layouts can be extracted, managed separately, then included in each layout. So while you can create individual UI components by writing a custom View, you can do it even more easily by re-using a layout file.

#### Reusing fragments
Another common way to reuse ui components is by using Fragments. A Fragment represents a behavior or a portion of user interface in a FragmentActivity. You can combine multiple fragments in a single activity to build a multi-pane UI and reuse a fragment in multiple activities. You can think of a fragment as a modular section of an activity, which has its own lifecycle, receives its own input events, and which you can add or remove while the activity is running.
When designing your application to support a wide range of screen sizes, you can reuse your fragments in different layout configurations to optimize the user experience based on the available screen space.
For example, on a handset device it might be appropriate to display just one fragment at a time for a single-pane user interface. Conversely, you may want to set fragments side-by-side on a tablet which has a wider screen size to display more information to the user.

#### Reusing custom views
If none of the prebuilt android widgets or layouts meets your needs, you can create your own View subclass. If you only need to make small adjustments to an existing widget or layout, you can simply subclass the widget or layout and override its methods.
Creating your own View subclasses gives you precise control over the appearance and function of a screen element.
Fully customized components can be used to create graphical components that appear however you wish. 
If you don't want to create a completely customized component, but instead are looking to put together a reusable component that consists of a group of existing controls, then creating a Compound Component (or Compound Control) might fit the bill. In a nutshell, this brings together a number of more atomic controls (or views) into a logical group of items that can be treated as a single thing. For example, a Combo Box can be thought of as a combination of a single line EditText field and an adjacent button with an attached PopupList.
To summarize, the use of a Layout as the basis for a Custom Control has a number of advantages, including:

* You can specify the layout using the declarative XML files just like with an activity screen, or you can create views programmatically and nest them into the layout from your code.
* The onDraw() and onMeasure() methods (plus most of the other on... methods) will likely have suitable behavior so you don't have to override them.
* In the end, you can very quickly construct arbitrarily complex compound views and re-use them as if they were a single component.

There is an even easier option for creating a custom View which is useful in certain circumstances. If there is a component that is already very similar to what you want, you can simply extend that component and just override the behavior that you want to change. You can do all of the things you would do with a fully customized component, but by starting with a more specialized class in the View hierarchy, you can also get a lot of behavior for free that probably does exactly what you want.
Creating custom components is only as complicated as you need it to be. A very sophisticated component may override even more `on...` methods and introduce some of its own helper methods, substantially customizing its properties and behavior. The only limit is your imagination and what you need the component to do.


