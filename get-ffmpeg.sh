function get {
    mkdir -p ffmpeg
    (
        cd ffmpeg
        if [ ! -f "$2.tar.xz" ]; then
            curl -L -o "$2.tar.xz" "$1"
        fi

        mkdir -p "$2"
        (
            cd "$2"
            tar -xf "../$2.tar.xz" --strip-components=1

            cp ffmpeg ../../src/main/resources/ffmpeg."$2"
        )
    )
}

get https://johnvansickle.com/ffmpeg/builds/ffmpeg-git-amd64-static.tar.xz amd64
get https://johnvansickle.com/ffmpeg/builds/ffmpeg-git-arm64-static.tar.xz aarch64