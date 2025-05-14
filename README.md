<h1 align='center'>
  GeoPhoto
</h1>

<p align='center'>
  Photo, Gallery, Map
</p>

<p align='center'>
<img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android Badge" />
<img src="https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white" alt="Android Studio Badge" />
<img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" alt="OpenJDK Badge" />
<img src="https://img.shields.io/badge/Sqlite-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLite Badge" />
<img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL Badge" />
</p>
<br />

## Features

- **Take Photo:** Captures an image using the device's built-in camera application.
- **Display Photo:** Shows the captured photo in an `ImageView` within the `PhotoActivity`.
- **Save Photo:** Saves the displayed photo to the device's public image gallery (e.g., Pictures or DCIM directory) using `MediaStore`.
- **FileProvider:** Uses `FileProvider` to securely share a temporary image file URI with the camera application, adhering to Android's best practices for file sharing.
