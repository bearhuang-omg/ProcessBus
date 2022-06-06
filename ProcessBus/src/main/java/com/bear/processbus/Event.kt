package com.bear.processbus

import android.os.Parcel
import android.os.Parcelable

class Event(var cmd: String, var content: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cmd)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun readFromParcel(source:Parcel){
        cmd = source.readString()!!
        content = source.readString()!!
    }

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }

}