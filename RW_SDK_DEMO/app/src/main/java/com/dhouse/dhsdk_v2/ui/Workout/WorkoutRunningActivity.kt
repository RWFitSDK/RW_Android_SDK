package com.dhouse.dhsdk_v2.ui.Workout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dhouse.dhsdk_v2.R
import com.dhouse.dhsdk_v2.databinding.ActivityWorkoutRunningBinding
import com.dhouse.dhsdk_v2.ui.bean.EventBusBean
import com.example.blesdk.DHBleSdk
import com.example.blesdk.bean.sync.NewSportBean
import com.example.blesdk.bean.sync.SportDataPushBean
import com.example.blesdk.bean.sync.SportResultBean
import com.example.blesdk.callback.data.Sport3ResultCallback
import com.example.blesdk.callback.data.SportDataPushCallback
import com.example.blesdk.callback.status.SportControlCallback
import com.example.blesdk.utils.BleActivityMode
import com.example.blesdk.utils.WorkoutControlType
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class WorkoutRunningActivity : AppCompatActivity() {

    private val binding by lazy { ActivityWorkoutRunningBinding.inflate(layoutInflater) }

    private var bleActivityMode: BleActivityMode? = null
    private var controlType: WorkoutControlType? = null


    private val sportRealPushCallback by lazy {
        object : SportDataPushCallback {
            override fun onSuccess() {
                Log.e("RWSDK", "SportDataPushCallback onSuccess")
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "SportDataPushCallback onFail: $errorCode")
            }

            override fun onResult(data: SportDataPushBean) {
                runOnUiThread {
                    // 更新运动类型和控制状态
                    updateData(data)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // 处理系统窗口边距
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        title = "Real-time sports data display"

        // 接收 Intent
        val modeValue = intent.getIntExtra("bleActivityMode", BleActivityMode.BLE_ACTIVITY_RUNNING.value)
        val controlValue = intent.getIntExtra("controlType", WorkoutControlType.Workout_Begin.value)


        // 转回 enum 对象
        bleActivityMode = BleActivityMode.fromValue(modeValue)
        controlType = WorkoutControlType.fromValue(controlValue)

        updateWorkoutType()
        updateButtons()

        // 按钮点击事件
        binding.workoutEndBt.setOnClickListener { finishWorkout() }
        binding.workoutPauseBt.setOnClickListener { pauseWorkout() }
        binding.workoutContinueBt.setOnClickListener { continueWorkout() }

        // 订阅实时运动数据
        DHBleSdk.subscribeData(sportRealPushCallback)

        DHBleSdk.subscribeData(sportControlCallback)

        DHBleSdk.subscribeData(sportResult3Callback)
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().register(this)
        // 进入运动界面
        DHBleSdk.setExerciseMore(1)
    }

    override fun onStop() {
        super.onStop()
        // 退出运动界面
        DHBleSdk.setExerciseMore(0)
    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)

        DHBleSdk.dispose(sportRealPushCallback)

        DHBleSdk.dispose(sportControlCallback)

        DHBleSdk.dispose(sportResult3Callback)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun bleConnectEventChange(logEvent: EventBusBean.LogEvent){
        if (logEvent.type == 1){
            finish()
        }
    }

    private fun updateWorkoutType() {
        val workoutTypes = resources.getStringArray(R.array.jlrunning_string_array)
        val index = bleActivityMode!!.value - BleActivityMode.BLE_ACTIVITY_START_INDEX.value
        val typeName = if (index in workoutTypes.indices) workoutTypes[index] else "Unknown"
        binding.workoutTypeLb.text = "Type: $typeName"
    }

    private fun updateButtons() {
        when (controlType) {
            WorkoutControlType.Workout_Begin,
            WorkoutControlType.Workout_Continue -> {
                binding.workoutEndBt.visibility = View.VISIBLE
                binding.workoutPauseBt.visibility = View.VISIBLE
                binding.workoutContinueBt.visibility = View.GONE
            }
            WorkoutControlType.Workout_Pause -> {
                binding.workoutEndBt.visibility = View.VISIBLE
                binding.workoutPauseBt.visibility = View.GONE
                binding.workoutContinueBt.visibility = View.VISIBLE
            }
            else -> {
                binding.workoutEndBt.visibility = View.GONE
                binding.workoutPauseBt.visibility = View.GONE
                binding.workoutContinueBt.visibility = View.GONE
            }
        }
    }

    private fun updateData(data: SportDataPushBean) {
        val duration = data.gettActivityTime()
        val totalStep = data.gettActivitySteps()
        val distance = data.gettActivityDistance()
        val calories = data.gettActivityCalorie()
        val hr = data.gettActivityHr()

        val hours = duration / 3600
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60

        binding.workoutTimeLb.text = String.format("Time: %02d:%02d:%02d", hours, minutes, seconds)
        binding.workoutStepLb.text = "Steps: $totalStep"
        binding.workoutDistanceLb.text = "Distance: %.2f Km".format(distance / 1000f)
        binding.workoutCaloriesLb.text = "Calories: %.1f KCal".format(calories / 1000f)
        binding.workoutHeartLb.text = "HeartRate: $hr bpm"
    }

    private val sportControlCallback by lazy {
        object : SportControlCallback {
            override fun onSuccess() {
                Log.e("RWSDK", "SportControlCallback onSuccess")
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "SportControlCallback onFail " + errorCode)
            }

            override fun onResult(data: NewSportBean) {
                Log.e("RWSDK", "SportControlCallback onResult " + data)

                data?.let {
                    val tControlType = it.status
                    val tSportType = it.sportType
                    when (tControlType) {
                        WorkoutControlType.Workout_Pause -> {
                            binding.workoutPauseBt.visibility = View.GONE
                            binding.workoutContinueBt.visibility = View.VISIBLE
                        }

                        WorkoutControlType.Workout_Continue -> {
                            binding.workoutPauseBt.visibility = View.VISIBLE
                            binding.workoutContinueBt.visibility = View.GONE
                        }

                        WorkoutControlType.Workout_Finish -> {
                            binding.workoutEndBt.visibility = View.GONE
                            binding.workoutPauseBt.visibility = View.GONE
                            binding.workoutContinueBt.visibility = View.GONE
                            startSyncWorkoutData()
                        }

                        else -> {

                        }
                    }
                }
            }
        }
    }

    private fun finishWorkout() {

        DHBleSdk.controlSportJL(bleActivityMode!!, WorkoutControlType.Workout_Finish)
    }

    private fun pauseWorkout() {
        DHBleSdk.controlSportJL(bleActivityMode!!, WorkoutControlType.Workout_Pause)

    }

    private fun continueWorkout() {
        DHBleSdk.controlSportJL(bleActivityMode!!, WorkoutControlType.Workout_Continue)
    }

    private val sportResult3Callback by lazy {
        object : Sport3ResultCallback {
            override fun onSuccess() {
                Log.e("RWSDK", "Sport3ResultCallback onSuccess")
            }

            override fun onFail(errorCode: Int) {
                Log.e("RWSDK", "Sport3ResultCallback onFail " + errorCode)
                finish()
            }

            override fun onResult(data: List<SportResultBean>) {
                Log.e("RWSDK", "Sport3ResultCallback onResult " + data.size)
                //Save Data
                finish()
            }
        }
    }

    private fun startSyncWorkoutData() {
        DHBleSdk.getSport3ResultJL()
    }
}
