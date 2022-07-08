package com.bear.processbus.service

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

class Request(val serviceName: String) : Parcelable {
    val params: HashMap<String, String> = HashMap()

    constructor(parcel: Parcel) : this(parcel.readString() ?: "") {
        val parmsStr = parcel.readString()
        if (!parmsStr.isNullOrEmpty()) {
            val json = JSONObject(parmsStr)
            json.keys().forEach { key ->
                this.params[key] = json.getString(key)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(serviceName)
        if (params.size > 0) {
            val json = JSONObject()
            params.forEach { entry ->
                json.put(entry.key, entry.value)
            }
            parcel.writeString(json.toString())
        }
    }

    fun setParams(params: Map<String, String>) {
        this.params.putAll(params)
    }

    fun addParams(key: String, value: String) {
        this.params.put(key, value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Request> {
        override fun createFromParcel(parcel: Parcel): Request {
            return Request(parcel)
        }

        override fun newArray(size: Int): Array<Request?> {
            return arrayOfNulls(size)
        }
    }
}