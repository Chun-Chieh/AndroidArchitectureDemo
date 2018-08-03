# Android Architecture Examples

- [Room](#room) (android.arch.persistence): Implement local database to persist data.
- [LiveData](#livedata) (android.arch.lifecycle): Get Notified when there are changes in the database in order to update the UI without being constantly query the database. 
- [ViewModel](#viewmodel) (android.arch.lifecycle) Cache out LiveData objects so they survive to configuration changes.
- [Useful Links](#useful-links)

### Expected Flow
1. Save in DB
2. LiveData Updated
3. Observer Notified
4. UI Updated

### Lifecycle Interfaces
1. Lifecycle Owner: Objects with a lifecycle, like ``Activity``, ``Fragment``.
2. Lifecycle Observers: Observe LifecycleOwners and get notified on lifecycle changes, like ``LiveData``.



# Room


### Setup
1. Add the dependency in ```build.gradle```. ([link][room])
```java
dependencies {
    def room_version = "1.1.1"
    implementation "android.arch.persistence.room:runtime:$room_version"
    // Test helpers
    testImplementation "android.arch.persistence.room:testing:$room_version"
}
```

2. Create an abstract class (ususally called "AppDatabase") that extends ```RoomDatabase```.
```java
@Database(entities = {TaskEntry.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    // ...
}
```
3. Annotate the POJO model class as ```@Entity```.
```java
@Entity(tableName = "book")
public class Book {
    @PrimaryKey(autoGenerate = true)
	private int id;
	@ColumnInfo(name = "book_title")
    private String title;
    @ColumnInfo(name = "book_author")
    private int author;
    // ...
    }
 ```
4. If the model has datatypes that couldn't comply with SQLite ([SQLite datatypes][2]), create a Converter class with annotation ```@TypeConverter``` and use ```@TypeConverters``` in the class created in step 1 (usually called "AppDatabase")
```java
public class DataConverter {
    // ...
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
    // ...
}

@TypeConverters(DataConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    // ...
}
```
5. Create an interface with annotation ```@Dao```
```java
@Dao
public interface BookDao {

    @Query("SELECT * FROM book ORDER BY book_author")
	List<Book> loadAllBooks();
    
    @Insert
    void insertBook(Book book);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateBook(Book book);

    @Delete
    void deleteBook(Book book);
    
    @Query("SELECT * FROM book WHERE id = :id")
    Book loadTaskById(int id);
}
```
6. Create a public abstract method in the class created in step 1 (usually called "AppDatabase")
```java
// ...
public abstract class AppDatabase extends RoomDatabase {
    // ...
    public abstract TaskDao taskDao();
}
```
### Usage

1. Create an instance by using singleton pattern
```java
AppDatabase mDb;
// ...
mDb = AppDatabase.getInstance(getApplicationContext());
// ...
```
2. Perform operations defined in Dao
```java
mDb.bookDao().insertTask(book);
...
mAdapter.setBookList(mDb.bookDao().loadAllBooks());
...
```
### Thread-handling

Since Room operations are forbidden in Main UI thread, it's necessary to handle by creating new worker thread.
```java
new Thread(new Runnable() {
    @Override
    public void run() {
        // database operation
        // ...
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update UI
                // ...
            }
        })
    }
}).start();
```
However, this approach may cause race condition. It'd better to create a class as ```Executor``` pools for the whole application.

# LiveData
---

```LiveData``` sits between UI and database. It can monitor changes in the database and notify the observers when data changes. This design pattern is called **Observer**.

```LiveData``` will run by default outside of the main thread. Thus ```Executor``` is no longer needed. ```LiveData``` is efficient in retrieving data from the database because it avoids querying the whole database when there are only few rows has changed. However, for operations such as insert, update or delete, there is no need to observe changes in the database. In this case, ```Executor``` is still being used for those operations.

### Setup
Add the dependency ```build.gradle```. ([link][lifecycle])
```java
dependencies {
    def lifecycle_version = "1.1.1"

    // ViewModel and LiveData
    implementation "android.arch.lifecycle:extensions:$lifecycle_version"
    annotationProcessor "android.arch.lifecycle:compiler:$lifecycle_version"
}
```

### Usage
1. Wrap the return type with ```LiveData``` and remove the ```Executor```

**In BookDao:**
```java
@Dao
public interface BookDao {
    @Query("SELECT * FROM book ORDER BY book_author")
    LiveData<List<Book>> loadAllBooks();
    
    @Query("SELECT * FROM book WHERE id = :id")
    LiveData<Book> loadBookById(int id);
    // ...
}
```
**In Activity where retrieve the data:**
```java
final LiveData<List<Book>> books = mDb.bookDao().loadAllBooks();
```

2. Add ```observe(LifecycleOwner, Observer)``` and implements the 2nd parameter, **Observer**. ```onChanged()``` will handle the logic of updating the UI. Also, ```observe()``` shall be called in ```OnCreate```.
```java
books.observe(this, new Observer<List<Book>>(){
    @Override
    public void onChanged(@Nullable List<Book> books) {
        mAdapter.setBooks(books);
    }
});
```

# ViewModel

The activities are destroyed and recreated when the device is being rotated. In other words, The operations in ```onCreate()``` will be called again such as creating a object of ```LiveData```. The usual approach is to use ```onSaveInstanceState()```. However, it's only suitable for a small amount of data that can be easily **serialize** and **deserialize**. In this case, ```ViewModel``` allows data to survive to configuration changes such as *rotation*.

In some cases, asynchronous calls are triggered to retrieve data. It will casue **memory leaks** if the activity is destroyed before the calls finish. To avoid memory leaks, the asynchronous calls can be made from the ```ViewModel```. The result will be delivered back to the ```ViewModel``` instaed of the Activity.

In short, ```ViewModel``` **stores UI related data that isn't destroyed on app rotations.**


##### Parcelable vs Serializable
Don't serialize at all. If it's unavoidable, use parcelable. (Reflection is used in Serializable which results in poor performance and battery drain.)

### Usage
1. Create a class that extends ```ViewModel``` / ```AndroidViewModel```. ```ViewModel``` doesn't allow to have context, but ```AndroidViewModel``` contains the **application context** in the constructor.

```java
public class MainViewModel extends AndroidViewModel{
    public MainViewModel(@NonNull Application application) {
        super(application);
    }
}
```
2. Create private variables in the ViewModel.
```java
private LiveData<List<Book>> books;
```

3. Create public getters for the variables.
```java
public LiveData<List<Book>> getBooks() {
        return books;
}
```

4. Initialize the member variables in the constructor.
```java
public MainViewModel(@NonNull Application application) {
    super(application);
    books = AppDatabase.getInstance(this.getApplication()).bookDao().loadAllBooks();
}
```
5. Call ViewModel's providers for the Activity.
**In Activity**
```java
MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
```

6. Retrieve the data by using the getters.
```java
private void setupViewModel() {
    MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
    viewModel.getTasks().observe(this, new Observer<List<Book>>() {
        @Override
        public void onChanged(@Nullable List<Book> books) {
            // Update UI...
        }
    });
}
```

### Use Case

If you need to pass values to the ViewModel, you'll need to create a ViewModel factory class. For instance, you want to **update** a book information in an activity, then you need to load the book information by **querying the ID** first.
##### AddBookViewModelFactory
1. Create a class extends ```ViewModelProvider.NewInstanceFactory```
\* Not ViewModelProvider***s***
```java
public class AddBookViewModelFactory extends ViewModelProvider.NewInstanceFactory {
}
```

2. Add member variables.
```java
private final AppDatabase mDb;
private final int mBookId;
```

3. Initialize the member variables in the constructor with the parameters received.
```java
public AddTaskViewModelFactory(AppDatabase database, int bookId) {
    this.mDb = database;
    this.mBookId = bookId;
}
```

4. Add the following snippet with minor modifications.
```java
@Override
public <T extends ViewModel> T create(Class<T> modelClass) {
    //noinspection unchecked
    return (T) new AddBookViewModel(mDb, mBookId);
}
```

The **AddBookViewModelFactory** could look like the following.
```java
public class AddBookViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDatabase mDb;
    private final int mBookId;

    public AddBookViewModelFactory(AppDatabase database, int bookId) {
        this.mDb = database;
        this.mBookId = bookId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AddBookViewModel(mDb, mBookId);
    }
}
```
##### AddBookViewModel
1. Now create AddBookViewModel that extends ```ViewModel```. 
2. Add member variables in which wrapped in LiveData and public getters for the variables.
```java
public class AddBookViewModel extends ViewModel {
    private LiveData<Book> book;

    public LiveData<Book> getBook() {
        return book;
    }
}
```
3. Intialize the variables in the constructor with relavent calls, such as database call.
```java
public AddBookViewModel(AppDatabase database, int bookId){
    book = database.bookDao().loadBookById(bookId);
}
```
The **AddBookViewModel** could look like the following.
```java
public class AddBookViewModel extends ViewModel {
    private LiveData<Book> book;

    public AddBookViewModel(AppDatabase database, int bookId){
        book = database.bookDao().loadbookById(bookId);
    }
    
    public LiveData<Book> getBook() {
        return book;
    }
}
```
##### AddBookActivity

1. Create an instance of the factory class.
```java
AddBookViewModelFactory factory = new AddBookViewModelFactory(mDb, mBookId);
```
2. Create the ```ViewModel``` by passing the factory as a parameter.
```java
AddBookViewModel viewModel = ViewModelProviders.of(this, factory).get(AddBookViewModel.class);
```
3. Observe the ```LiveData``` object in the ```ViewModel```. Use it also when removing the ```observer```.
```java
viewModel.getBook().observe(this, new Observer<Book>() {
    @Override
    public void onChanged(@Nullable Book book) {
        viewModel.getBook().removeObserver(this);
        // update UI
    }
});
```
The onCreate() in **AddBookActivity** could look like the following.
```java
public void onCreate (Bundle savedInstanceState){
    // ...
    Intent intent = getIntent();
    if (intent != null && intent.hasExtra(EXTRA_BOOK_ID)) {
        mButton.setText(R.string.update_button);
        if (mBookId == DEFAULT_BOOK_ID) {
            mBookId = intent.getIntExtra(EXTRA_BOOK_ID, DEFAULT_BOOK_ID);
            
            AddBookViewModelFactory factory = new AddBookViewModelFactory(mDb, mBookId);
            final AddBookViewModel viewModel = ViewModelProviders.of(this, factory).get(AddBookViewModel.class);
            
            viewModel.getBook().observe(this, new Observer<Book>() {
                @Override
                public void onChanged(@Nullable Book book) {
                    viewModel.getBook().removeObserver(this);
                    populateUI(book);
                }
            });
        }
    }
    // ...
}
```

## Useful Links
- [Implement a custom LifecycleOwner][implementing-lco]
- [Use Cases for lifecycle-aware components][usecases]

[room]: https://developer.android.com/topic/libraries/architecture/adding-components#room
[lifecycle]: https://developer.android.com/topic/libraries/architecture/adding-components#lifecycle
[implementing-lco]:https://developer.android.com/topic/libraries/architecture/lifecycle#implementing-lco
[usecases]:https://developer.android.com/topic/libraries/architecture/lifecycle#use-cases
[2]:https://www.sqlite.org/datatype3.html
