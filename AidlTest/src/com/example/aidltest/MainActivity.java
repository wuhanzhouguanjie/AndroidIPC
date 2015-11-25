package com.example.aidltest;


import java.util.List;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener{
	
	private Button mGetList,mAddBook,mReg,munReg;

	private IBookManager mRemoteBookManager;
	
	private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;
	
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_NEW_BOOK_ARRIVED:
                Log.e("-------->", "receive new book :" + msg.obj);
                break;
            default:
                super.handleMessage(msg);
            }
        }
    };
    
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d("---------->", "binder died. tname:" + Thread.currentThread().getName());
            if (mRemoteBookManager == null)
                return;
            mRemoteBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mRemoteBookManager = null;
            // TODO:这里重新绑定远程Service
            Intent intent = new Intent(MainActivity.this, BookManagerService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
        mGetList = (Button) findViewById(R.id.getList);
        mAddBook = (Button) findViewById(R.id.addbook);
        mReg = (Button) findViewById(R.id.register);
        munReg = (Button) findViewById(R.id.unregister);
        mGetList.setOnClickListener(this);
        mAddBook.setOnClickListener(this);
        mReg.setOnClickListener(this);
        munReg.setOnClickListener(this);
        
	}
	
    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {

        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook)
                    .sendToTarget();
            Log.e("-------------->", "onNewBookArrived");
        }
    };
	
	private ServiceConnection  mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			 mRemoteBookManager = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			 IBookManager bookManager = IBookManager.Stub.asInterface(service);
			 mRemoteBookManager = bookManager;
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.getList:
			try {
				List<Book> books = mRemoteBookManager.getBookList();
				for( int loop=0;loop<books.size();loop++ ){
					Log.e("---------------->", books.get(loop).bookName + " " + books.get(loop).bookId);
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.addbook:
			try {
				mRemoteBookManager.addBook(new Book(1111, "11111"));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.register:
			try {
				mRemoteBookManager.registerListener(mOnNewBookArrivedListener);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;
		case R.id.unregister:
			try {
				mRemoteBookManager.unregisterListener(mOnNewBookArrivedListener);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (mRemoteBookManager != null
                && mRemoteBookManager.asBinder().isBinderAlive()) {
            try {
                Log.i("------->", "unregister listener:" + mOnNewBookArrivedListener);
                mRemoteBookManager
                        .unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
	}

}
