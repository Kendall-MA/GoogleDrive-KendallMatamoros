package kendall.matamoros

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ListView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kendall.matamoros.adapter.DriveAdapter
import kendall.matamoros.entity.File
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    val fileList: MutableList<File> = mutableListOf()
    lateinit var fileAdapter: DriveAdapter

    companion object {
        private const val REQUEST_SIGN_IN = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestSignIn()

        val fileListView = findViewById<ListView>(R.id.files_list)
        fileAdapter = applicationContext?.let { DriveAdapter(it, fileList) }!!

        fileListView.adapter = fileAdapter
    }

    private fun handleSignInResult(result: Intent) {

        var newFile: File

        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleAccount ->
                // Use the authenticated account to sign in to the Drive service.
                val credential = GoogleAccountCredential.usingOAuth2(
                    this, listOf(DriveScopes.DRIVE, DriveScopes.DRIVE_FILE)
                )
                credential.selectedAccount = googleAccount.account
                val googleDriveService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential
                )
                    .setApplicationName(getString(R.string.app_name))
                    .build()

                // https://developers.google.com/drive/api/v3/search-files
                // https://developers.google.com/drive/api/v3/search-parameters
                // https://developers.google.com/drive/api/v3/mime-types
                launch(Dispatchers.Default) {
                    var pageToken: String? = null
                    do {
                        val result = googleDriveService.files().list().apply {
                            //q = "mimeType='application/vnd.google-apps.spreadsheet'"
                            spaces = "drive"
                            fields = "nextPageToken, files(id, name)"
                            this.pageToken = pageToken
                        }.execute()
                        val mainThread = Handler(Looper.getMainLooper())
                        for (file in result.files) {
                            mainThread.post {
                                newFile = File(file.id.toString(), file.name)
                                fileList.add(newFile)
                                fileAdapter?.notifyDataSetChanged()
                                Log.e("Archivo", "name=${file.name}, id=${file.id}")
                            }
                        }
                    } while (pageToken != null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(e.stackTraceToString(), "Signin error")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e("mensage", "onActivityResult=$requestCode")
        when (requestCode) {
            REQUEST_SIGN_IN -> {
                if (resultCode == RESULT_OK && data != null) {
                    handleSignInResult(data)
                } else {
                    Log.e("ERROR", "Fallo en la petici??n de autenticaci??n")
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun requestSignIn() {
        val client = buildGoogleSignInClient()
        startActivityForResult(client.signInIntent, REQUEST_SIGN_IN)
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE))
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, signInOptions)
    }
}