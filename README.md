# OriginalLiveWallpaper
Android端末用のライブ壁紙です。任意の画像をフェードで切り替えながら表示できます。

## 使い方
1. `app/src/main/res/drawable`の中に端末の画面サイズに合わせた画像を入れます
2. `/app/src/main/java/com/example/originallivewallpaper/WallpaperService.kt`内の`imgPath`の要素を`R.drawable.[画像ファイル名]`に書き換えます
3. `displayX`,`displayY`を端末の画面サイズに合わせます
4. `background`をフェード時に使いたい背景色にします
5. ビルドします
