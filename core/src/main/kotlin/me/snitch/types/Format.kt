package me.snitch.types

enum class Format(val type: String) {
    OctetStream("application/octet-stream"),
    Json("application/json"),
    ImageJpeg("image/jpeg"),
    VideoMP4("video/mp4"),
    TextHTML("text/html"),
    TextPlain("text/plain"),
}