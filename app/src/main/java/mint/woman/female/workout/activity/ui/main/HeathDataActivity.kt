package mint.woman.female.workout.activity.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mint.woman.female.workout.databinding.ActivityHeathDataBinding
import mint.woman.female.workout.utils.MyPreference
import java.time.Instant
import java.time.ZoneOffset

class HeathDataActivity : AppCompatActivity() {
    lateinit var binding: ActivityHeathDataBinding
    lateinit var myPreference: MyPreference;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeathDataBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        val availabilityStatus = HealthConnectClient.getSdkStatus(this)
        myPreference = MyPreference.newInstance(this)
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            binding.txtLog.setText("HEALTH SDK UNAVAILABLE");
            return  // early return as there is no viable integration
        }
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            val providerPackageName = "com.google.android.apps.healthdata"
            // Optionally redirect to package installer to find a provider, for example:
            val uriString =
                "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setPackage("com.android.vending")
            intent.setData(Uri.parse(uriString))
            intent.putExtra("overlay", true)
            intent.putExtra("callerId", packageName)
            startActivity(intent)
            return
        }
        val healthConnectClient = HealthConnectClient.getOrCreate(this)
        binding.swSyncHealthConnect.isChecked = myPreference.isSyncGoogleFit
        binding.swSyncHealthConnect.setOnCheckedChangeListener{co,b->
            if (b){
                myPreference.isSyncGoogleFit = false;
                lifecycleScope.launch {
                    checkPermissionsAndRun(healthConnectClient)
                }
            }else{
               myPreference.isSyncGoogleFit = false;
            }

        }
        var end_time = Instant.now().minusSeconds(1)
        var start_time = end_time.minusSeconds(3000);

        binding.btnAddExercise.setOnClickListener {
            lifecycleScope.launch {
                insertSection(healthConnectClient)
            }
        }

    }



    private val PERMISSIONS = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class)
    )

    // Permission launcher

    val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()

    val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(PERMISSIONS)) {
            Toast.makeText(
                this,
                "granted", Toast.LENGTH_SHORT
            ).show()
        } else {
            // Lack of required permissions
        }
    }
    suspend fun readStepsByTimeRange(
        healthConnectClient: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ) {
        try {
            val response =
                healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        ExerciseSessionRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
            var log = ""
            for (stepRecord in response.records) {
                log = log + " step: " + stepRecord.startTime + "\n";
            }
            binding!!.txtLog.setText(log)
        } catch (e: Exception) {
            // Run error handling here.
        }
    }


    suspend fun insertSection(healthConnectClient: HealthConnectClient) {
        try {
            val startTime = Instant.now().minusSeconds(600) // 1 hour ago
            val endTime = Instant.now()


            // Create a WorkoutSessionRecord
            val workoutSession = ExerciseSessionRecord(
                startTime = startTime,
                startZoneOffset = ZoneOffset.UTC,
                endTime =endTime,
                endZoneOffset =  ZoneOffset.UTC,
                exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS,
                title = "Home Workout - Day 1",
//                metadata = Metadata(UUID.randomUUID().toString())
            )

            healthConnectClient.insertRecords(listOf(workoutSession))
        } catch (e: Exception) {
            // Run error handling here
        }
    }
    // Create a set of permissions for required data types

    suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(PERMISSIONS)) {
            Toast.makeText(this, "All permissions already granted", Toast.LENGTH_SHORT).show()
            // Proceed with inserting or reading data
        } else {
            requestPermissions.launch(PERMISSIONS)
        }
    }
}