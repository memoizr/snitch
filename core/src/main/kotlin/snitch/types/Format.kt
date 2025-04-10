package snitch.types

enum class Format(val type: String) {
    AudioMpeg("audio/mpeg"),
    AudioOgg("audio/ogg"),
    AudioWav("audio/wav"),

    Gzip("application/gzip"),
    JavaScript("application/javascript"),
    Json("application/json"),
    MsExcel("application/vnd.ms-excel"),
    MsPowerpoint("application/vnd.ms-powerpoint"),
    MsWord("application/msword"),
    OctetStream("application/octet-stream"),
    Pdf("application/pdf"),
    Rar("application/x-rar-compressed"),
    XWwwFormUrlencoded("application/x-www-form-urlencoded"),
    Xml("application/xml"),
    Zip("application/zip"),

    ImageBmp("image/bmp"),
    ImageGif("image/gif"),
    ImageJpeg("image/jpeg"),
    ImagePng("image/png"),

    TextCss("text/css"),
    TextCsv("text/csv"),
    TextHtml("text/html"),
    TextPlain("text/plain"),

    VideoMp4("video/mp4"),
    VideoOgg("video/ogg"),
    VideoQuicktime("video/quicktime")
}
