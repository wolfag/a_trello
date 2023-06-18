package com.example.a_trello.models

import android.os.Parcel
import android.os.Parcelable

data class Card(
    val name: String = "",
    val createBy: String = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    val labelColor: String = "",
    val dueDate: Long = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readLong()!!
    ) {
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
        writeString(createBy)
        writeStringList(assignedTo)
        writeString(labelColor)
        writeLong(dueDate)
    }

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }
}