# Mini-CameraX-

Camera lib sdk using CameraX android. 

Support: Android v5.1 to Latest


Hassle-free implementation :)

## Implementation

Open Project.Gradle and add below line

```groovy
   allprojects {
        repositories {
            google()
            jcenter()

            maven {
                url  "https://dl.bintray.com/arulmani/Mini-Camera"
            }
       }
   }
```

And add below line in App.Gradle inside depdency section

    implementation 'com.arul.minicamera:camerax:1.0.0'

## Usage

```
class MainActivity : AppCompatActivity() {

    val camIntentCode = 100

    val imageFile: File by lazy {
        File(getExternalFilesDir(null)?.absolutePath + "/${System.currentTimeMillis()}.jpg")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun OnGetImage(view: View) {
        val camIntent = Intent(this@MainActivity, CameraXActivity::class.java)
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFile.absolutePath)
        startActivityForResult(camIntent, camIntentCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == camIntentCode) {
            if (resultCode == RESULT_OK) {
                val myBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                findViewById<ImageView>(R.id.image).setImageBitmap(myBitmap)
            }
        }
    }
}

```
