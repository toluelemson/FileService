package com.example.fileservice.rest

    constructor() {}

    constructor(message: String?) {
        this.message = message
    }

    constructor(message: String, code: String) {
        this.message = message
        this.code = code
    }
}
