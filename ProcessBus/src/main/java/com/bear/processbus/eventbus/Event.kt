package com.bear.processbus.eventbus

import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable

class Event(
    var cmd: String,
    var content: String,
) : Parcelable {

    var fromProcess = ""
    private var attachmentBinder: IBinder? = null

    constructor(cmd: String, content: String, attachment: () -> ByteArray) : this(cmd, content) {
        setAttachment(attachment)
    }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
        fromProcess = parcel.readString() ?: ""
        attachmentBinder = parcel.readStrongBinder()
    }

    fun setAttachment(block: () -> ByteArray) {
        if (block != null) {
            attachmentBinder = object : IAttachment.Stub() {
                override fun getAttachment(): Attachment {
                    return Attachment(block())
                }
            }
        }
    }

    fun readFromParcel(parcel: Parcel) {
        cmd = parcel.readString() ?: ""
        content = parcel.readString() ?: ""
        fromProcess = parcel.readString() ?: ""
        attachmentBinder = parcel.readStrongBinder()
    }

    fun getAttachment(): Attachment? {
        if (attachmentBinder == null) {
            return null
        }
        return IAttachment.Stub.asInterface(attachmentBinder).attachment
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cmd)
        parcel.writeString(content)
        parcel.writeString(fromProcess)
        parcel.writeStrongBinder(attachmentBinder)
    }

    override fun describeContents(): Int {
        return 0
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