package com.example.aidltest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

public class BookManagerService extends Service {

	private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<Book>();

	private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<>();

	private AtomicBoolean mIsServiceDestoryed = new AtomicBoolean(false);

	private Binder mBinder = new IBookManager.Stub() {

		@Override
		public List<Book> getBookList() throws RemoteException {
			// TODO Auto-generated method stub
			return mBookList;
		}

		@Override
		public void addBook(Book book) throws RemoteException {
			// TODO Auto-generated method stub
			mBookList.add(book);
		}

		@Override
		public void registerListener(IOnNewBookArrivedListener listener)
				throws RemoteException {
			// TODO Auto-generated method stub
			mListenerList.register(listener);

			final int N = mListenerList.beginBroadcast();
			mListenerList.finishBroadcast();
			Log.d("---------->", "registerListener, current size:" + N);
		}

		@Override
		public void unregisterListener(IOnNewBookArrivedListener listener)
				throws RemoteException {
			// TODO Auto-generated method stub
			boolean success = mListenerList.unregister(listener);

			if (success) {
				Log.d("---------->", "unregister success.");
			} else {
				Log.d("---------->", "not found, can not unregister.");
			}
			final int N = mListenerList.beginBroadcast();
			mListenerList.finishBroadcast();
			Log.d("---------->", "unregisterListener, current size:" + N);

		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		mBookList.add(new Book(1, "Android"));
		mBookList.add(new Book(2, "Ios"));
		new Thread(new ServiceWorker()).start();
	}

	@Override
	public void onDestroy() {
		mIsServiceDestoryed.set(true);
		super.onDestroy();
	}
	
	private void onNewBookArrived(Book book) throws RemoteException {
        mBookList.add(book);
        final int N = mListenerList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnNewBookArrivedListener l = mListenerList.getBroadcastItem(i);
            if (l != null) {
                try {
                    l.onNewBookArrived(book);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mListenerList.finishBroadcast();
    }

    private class ServiceWorker implements Runnable {
        @Override
        public void run() {
            // do background processing here.....
            while (!mIsServiceDestoryed.get()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId = mBookList.size() + 1;
                Book newBook = new Book(bookId, "new book#" + bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		// 权限检查
		int check = checkCallingOrSelfPermission("com.example.aidltest.permission.ACCESS_BOOK_SERVICE");
		if (check == PackageManager.PERMISSION_DENIED) {
			return null;
		}
		return mBinder;
	}

}
