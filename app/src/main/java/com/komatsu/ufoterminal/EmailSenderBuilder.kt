package com.komatsu.ufoterminal

import java.io.File

class EmailSenderBuilder() {
    var address: String? = null
    var title: String? = null
    var body: String? = null
    var attachment: String? = null

    fun setAddress(address: String): EmailSenderBuilder {
        // address
        return this
    }

    fun setTitle(title: String): EmailSenderBuilder {
        // title
        return this
    }

    fun setBody(body: String): EmailSenderBuilder {
        // body
        return this
    }

    fun setAttachment(attachment: File): EmailSenderBuilder {
        // file
        return this
    }

    fun send(): Boolean {
        // send Email
        return true
    }
}