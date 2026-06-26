package com.dhouse.dhsdk_v2.ui.Workout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhouse.dhsdk_v2.R
import com.dhouse.dhsdk_v2.databinding.ActivityNewMainBinding
import com.dhouse.dhsdk_v2.databinding.ActivityWorkoutTypeBinding
import com.dhouse.dhsdk_v2.ui.bean.EventBusBean
import com.example.blesdk.DHBleSdk
import com.example.blesdk.bean.sync.NewSportBean
import com.example.blesdk.callback.data.SportGetControlCallback
import com.example.blesdk.callback.status.SportControlCallback
import com.example.blesdk.utils.BleActivityMode
import com.example.blesdk.utils.WorkoutControlType
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Date

class WorkoutTypeActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityWorkoutTypeBinding.inflate(layoutInflater)
    }

    private var isItemClick = false
    private var mBleActivityMode: BleActivityMode? = BleActivityMode.BLE_ACTIVITY_RUNNING
    /**
     * 运动状态数据回调
     */
    private val sportGetCallback by lazy {
        object : SportGetControlCallback {
            override fun onSuccess() {
                Log.e("RWSDK", "SportGetControlCallback onSuccess")
            }

            override fun onFail(errorCode: Int) {

            }

            override fun onResult(data: NewSportBean) {

                val bleActivityMode = data.sportType
                val tControlType = data.status

                Log.e("RWSDK", "SportGetControlCallback onResult " + bleActivityMode + " " + tControlType)

                //0x01开始 0x03暂停 0x02继续 0x04结束
                if (tControlType?.isInRunning == true) {
                    // 在运动中，直接进入运动

                    val intent = Intent(this@WorkoutTypeActivity, WorkoutRunningActivity::class.java).apply {
                        putExtra("bleActivityMode", bleActivityMode?.value)
                        putExtra("controlType", tControlType.value)
                    }
                    startActivity(intent)
                }
                else{
                    if (isItemClick){
                        isItemClick = false

                        DHBleSdk.subscribeData(sportControlCallback)
                        DHBleSdk.controlSportJL(mBleActivityMode!!,
                            WorkoutControlType.Workout_Begin)
                    }
                }
            }
        }
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
                        WorkoutControlType.Workout_Begin -> {

                            val intent = Intent(this@WorkoutTypeActivity, WorkoutRunningActivity::class.java).apply {
                                putExtra("bleActivityMode", tSportType?.value)
                                putExtra("controlType", tControlType.value)
                            }
                            startActivity(intent)

                        }

                        WorkoutControlType.Workout_Pause -> {

                        }

                        WorkoutControlType.Workout_Continue -> {

                        }

                        WorkoutControlType.Workout_Finish -> {

                        }

                        else -> {

                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        title = "Work out type"

        EventBus.getDefault().register(this)

        initRecyclerView()

        DHBleSdk.subscribeData(sportGetCallback)
        DHBleSdk.controlGetSportJLData()
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onDestroy() {
        super.onDestroy()

        EventBus.getDefault().unregister(this)
    }


    override fun onStop() {
        super.onStop()

        DHBleSdk.dispose(sportControlCallback)


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun bleConnectEventChange(logEvent: EventBusBean.LogEvent){
        if (logEvent.type == 1){
            finish()
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = WorkoutTypeAdapter()
        binding.recyclerView.adapter = adapter

        // 从 string-array 读取数据
        val workoutTypes = resources.getStringArray(R.array.jlrunning_string_array).toMutableList()

        adapter.setList(workoutTypes)

        adapter.setOnItemClickListener { _, _, position ->
            val item = adapter.data[position]
            mBleActivityMode = BleActivityMode.fromValue(BleActivityMode.BLE_ACTIVITY_START_INDEX.value + position)
            val tControlType = WorkoutControlType.Workout_Begin

            isItemClick = true
            DHBleSdk.controlGetSportJLData()

        }
    }
}