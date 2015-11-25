package com.example.aidltest;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {

	public int bookId;
	public String bookName;

	public Book(int bookId, String bookName) {
		this.bookId = bookId;
		this.bookName = bookName;
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(bookId);
		dest.writeString(bookName);
	}

	public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {

		public Book createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new Book(source);
		}

		public Book[] newArray(int size) {
			// TODO Auto-generated method stub
			return new Book[size];
		}

	};

	private Book(Parcel in) {
		bookId = in.readInt();
		bookName = in.readString();
	}

	@Override
	public String toString() {
		return String.format("[bookId:%s, bookName:%s]", bookId, bookName);
	}

}
