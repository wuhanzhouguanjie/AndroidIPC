package com.example.aidltest;

import com.example.aidltest.Book;

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}